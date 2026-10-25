package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.config.RegrasFinanceiras;
import br.edu.ifs.finanse.dto.TransacaoDTO;
import br.edu.ifs.finanse.entity.Categoria;
import br.edu.ifs.finanse.entity.SugestaoIa;
import br.edu.ifs.finanse.entity.Transacao;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.CategoriaRepository;
import br.edu.ifs.finanse.repository.SugestaoIaRepository;
import br.edu.ifs.finanse.repository.TransacaoRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SugestaoIaRepository sugestaoIaRepository;
    private final CategorizacaoService categorizacaoService;

    /**
     * RF14 — Visualizar histórico com filtros. Todos os filtros são
     * opcionais (null = sem restrição naquele campo); filtragem feita em
     * memória, o que é suficiente para o volume de dados de um TCC — se a
     * base crescer muito, o próximo passo é mover isso para uma query.
     */
    public List<TransacaoDTO.Response> listarPorUsuario(
            Long usuarioId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Long categoriaId,
            Transacao.TipoTransacao tipo
    ) {
        return transacaoRepository.findByUsuarioId(usuarioId).stream()
                .filter(t -> dataInicio == null || !t.getData().isBefore(dataInicio))
                .filter(t -> dataFim == null || !t.getData().isAfter(dataFim))
                .filter(t -> categoriaId == null || (t.getCategoria() != null && categoriaId.equals(t.getCategoria().getId())))
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .map(this::toResponse)
                .toList();
    }

    public TransacaoDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    /**
     * Edita uma transação já registrada — usado principalmente para
     * confirmar ou corrigir a categoria sugerida pelo RF10, mas permite
     * editar qualquer campo.
     *
     * Recalcula o limite do cartão (RF16) corretamente: estorna a
     * contribuição antiga da transação antes de aplicar a nova, evitando
     * duplicar ou perder valor no limiteCreditoUtilizado do usuário.
     */
    @Transactional
    public TransacaoDTO.Response editar(Long id, TransacaoDTO.Request dto) {
        Transacao transacao = buscarEntidade(id);
        Usuario usuario = transacao.getUsuario();

        BigDecimal contribuicaoAntiga = contribuicaoParaLimite(transacao);

        Categoria categoria;
        if (dto.categoriaId() != null) {
            categoria = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + dto.categoriaId()));
        } else {
            categoria = null;
        }

        transacao.setCategoria(categoria);
        transacao.setDescricao(dto.descricao());
        transacao.setValor(dto.valor());
        transacao.setTipo(dto.tipo());
        transacao.setData(dto.data());
        transacao.setViaCartaoCredito(dto.viaCartaoCredito());
        transacao = transacaoRepository.save(transacao);

        // Se essa transação tinha uma sugestão de IA (RF10), registra se o
        // usuário confirmou (manteve a categoria sugerida) ou corrigiu (trocou).
        sugestaoIaRepository.findByTransacaoId(id).ifPresent(sugestao -> {
            boolean confirmouSugestao = categoria != null
                    && sugestao.getCategoriaSugerida() != null
                    && categoria.getId().equals(sugestao.getCategoriaSugerida().getId());
            sugestao.setAceita(confirmouSugestao);
            sugestaoIaRepository.save(sugestao);
        });

        BigDecimal contribuicaoNova = contribuicaoParaLimite(transacao);
        ajustarLimiteUtilizado(usuario, contribuicaoNova.subtract(contribuicaoAntiga));

        return toResponse(transacao);
    }

    /**
     * Registra uma transação. Quando é uma despesa via cartão de crédito,
     * atualiza o limite utilizado do usuário (RF16) — é esse valor que o
     * dashboard e o RF17 usam para decidir o alerta de rotativo e a
     * recomendação de conteúdo educativo.
     *
     * Quando a despesa chega sem categoriaId, aciona o RF10: tenta
     * classificar automaticamente pela descrição e grava a sugestão em
     * SugestaoIa para o usuário confirmar ou editar depois (RF10 do Quadro 1).
     */
    @Transactional
    public TransacaoDTO.Response criar(TransacaoDTO.Request dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + dto.usuarioId()));

        Categoria categoria = null;
        if (dto.categoriaId() != null) {
            categoria = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + dto.categoriaId()));
        }

        Transacao transacao = Transacao.builder()
                .usuario(usuario)
                .categoria(categoria)
                .descricao(dto.descricao())
                .valor(dto.valor())
                .tipo(dto.tipo())
                .data(dto.data())
                .viaCartaoCredito(dto.viaCartaoCredito())
                .build();
        transacao = transacaoRepository.save(transacao);

        // RF10 — só tenta classificar automaticamente se o usuário não informou a categoria.
        if (categoria == null && dto.tipo() == Transacao.TipoTransacao.DESPESA) {
            Optional<CategorizacaoService.Resultado> resultado = categorizacaoService.classificar(dto.descricao());
            if (resultado.isPresent()) {
                transacao.setCategoria(resultado.get().categoria());
                transacao = transacaoRepository.save(transacao);

                SugestaoIa sugestao = SugestaoIa.builder()
                        .transacao(transacao)
                        .categoriaSugerida(resultado.get().categoria())
                        .confianca(resultado.get().confianca())
                        .aceita(false) // usuário ainda não confirmou explicitamente
                        .build();
                sugestaoIaRepository.save(sugestao);
            }
        }

        ajustarLimiteUtilizado(usuario, contribuicaoParaLimite(transacao));

        return toResponse(transacao);
    }

    public void excluir(Long id) {
        Transacao transacao = buscarEntidade(id);
        // Estorna o limite do cartão se a despesa excluída contribuía para ele (mantém RF16 consistente).
        ajustarLimiteUtilizado(transacao.getUsuario(), contribuicaoParaLimite(transacao).negate());
        transacaoRepository.deleteById(id);
    }

    private Transacao buscarEntidade(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada: " + id));
    }

    /** Quanto essa transação contribui para o limite utilizado do cartão (RF16). Zero se não for despesa no cartão. */
    private BigDecimal contribuicaoParaLimite(Transacao t) {
        if (t.isViaCartaoCredito() && t.getTipo() == Transacao.TipoTransacao.DESPESA) {
            return t.getValor();
        }
        return BigDecimal.ZERO;
    }

    /** Aplica um delta (positivo ou negativo) ao limite utilizado do usuário, nunca deixando ficar negativo. */
    private void ajustarLimiteUtilizado(Usuario usuario, BigDecimal delta) {
        if (delta.signum() == 0) return;
        BigDecimal atual = usuario.getLimiteCreditoUtilizado() == null ? BigDecimal.ZERO : usuario.getLimiteCreditoUtilizado();
        usuario.setLimiteCreditoUtilizado(atual.add(delta).max(BigDecimal.ZERO));
        usuarioRepository.save(usuario);
    }

    private TransacaoDTO.Response toResponse(Transacao t) {
        Usuario usuario = t.getUsuario();
        boolean alertaRotativo = t.isViaCartaoCredito()
                && usuario.getPercentualLimiteUtilizado().compareTo(RegrasFinanceiras.LIMIAR_ALERTA_ROTATIVO) >= 0;

        return new TransacaoDTO.Response(
                t.getId(),
                usuario.getId(),
                t.getCategoria() != null ? t.getCategoria().getId() : null,
                t.getCategoria() != null ? t.getCategoria().getNome() : null,
                t.getDescricao(),
                t.getValor(),
                t.getTipo(),
                t.getData(),
                t.isViaCartaoCredito(),
                alertaRotativo
        );
    }
}

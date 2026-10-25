package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.MetaDTO;
import br.edu.ifs.finanse.entity.Meta;
import br.edu.ifs.finanse.entity.TipoMeta;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.MetaRepository;
import br.edu.ifs.finanse.repository.TipoMetaRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoMetaRepository tipoMetaRepository;

    public List<MetaDTO.Response> listarPorUsuario(Long usuarioId) {
        return metaRepository.findAll().stream()
                .filter(m -> m.getUsuario().getId().equals(usuarioId))
                .map(this::toResponse)
                .toList();
    }

    public MetaDTO.Response criar(MetaDTO.Request dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + dto.usuarioId()));
        TipoMeta tipoMeta = tipoMetaRepository.findById(dto.tipoMetaId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de meta não encontrado: " + dto.tipoMetaId()));

        Meta meta = Meta.builder()
                .usuario(usuario)
                .tipoMeta(tipoMeta)
                .nome(dto.nome())
                .valorAlvo(dto.valorAlvo())
                .valorAtual(BigDecimal.ZERO)
                .prazoMeses(dto.prazoMeses())
                .dataCriacao(LocalDate.now())
                .status(Meta.StatusMeta.ATIVA)
                .build();

        return toResponse(metaRepository.save(meta));
    }

    public MetaDTO.Response editar(Long id, MetaDTO.Request dto) {
        Meta meta = buscarEntidade(id);
        meta.setNome(dto.nome());
        meta.setValorAlvo(dto.valorAlvo());
        meta.setPrazoMeses(dto.prazoMeses());
        return toResponse(metaRepository.save(meta));
    }

    /**
     * Registra uma movimentação na meta: aporte (soma) ou retirada (subtrai).
     * Usado quando o usuário precisa usar parte do dinheiro guardado antes
     * de atingir o valor alvo — por exemplo, uma reserva de emergência
     * sendo efetivamente usada em uma emergência.
     */
    public MetaDTO.Response registrarMovimentacao(Long id, BigDecimal valor, Meta.TipoMovimentacao tipo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor deve ser positivo");
        }
        Meta meta = buscarEntidade(id);

        if (tipo == Meta.TipoMovimentacao.RETIRADA) {
            if (valor.compareTo(meta.getValorAtual()) > 0) {
                throw new IllegalArgumentException("valor da retirada não pode ser maior que o valor atual guardado");
            }
            meta.setValorAtual(meta.getValorAtual().subtract(valor));
            // Se a meta havia sido concluída e agora caiu abaixo do alvo, volta a ficar ativa.
            if (meta.getStatus() == Meta.StatusMeta.CONCLUIDA
                    && meta.getValorAtual().compareTo(meta.getValorAlvo()) < 0) {
                meta.setStatus(Meta.StatusMeta.ATIVA);
            }
        } else {
            meta.setValorAtual(meta.getValorAtual().add(valor));
            if (meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                meta.setStatus(Meta.StatusMeta.CONCLUIDA);
            }
        }

        return toResponse(metaRepository.save(meta));
    }

    public MetaDTO.Response pausar(Long id) {
        Meta meta = buscarEntidade(id);
        meta.setStatus(Meta.StatusMeta.PAUSADA);
        return toResponse(metaRepository.save(meta));
    }

    public MetaDTO.Response reativar(Long id) {
        Meta meta = buscarEntidade(id);
        meta.setStatus(Meta.StatusMeta.ATIVA);
        return toResponse(metaRepository.save(meta));
    }

    public void excluir(Long id) {
        if (!metaRepository.existsById(id)) {
            throw new EntityNotFoundException("Meta não encontrada: " + id);
        }
        metaRepository.deleteById(id);
    }

    private Meta buscarEntidade(Long id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meta não encontrada: " + id));
    }

    private MetaDTO.Response toResponse(Meta m) {
        return new MetaDTO.Response(
                m.getId(),
                m.getUsuario().getId(),
                m.getTipoMeta().getId(),
                m.getTipoMeta().getDescricao(),
                m.getNome(),
                m.getValorAlvo(),
                m.getValorAtual(),
                m.getProgresso(),
                m.getPrazoMeses(),
                m.getDataCriacao(),
                m.getStatus()
        );
    }
}

package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.TransacaoDTO;
import br.edu.ifs.finanse.entity.Categoria;
import br.edu.ifs.finanse.entity.SugestaoIa;
import br.edu.ifs.finanse.entity.Transacao;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.CategoriaRepository;
import br.edu.ifs.finanse.repository.SugestaoIaRepository;
import br.edu.ifs.finanse.repository.TransacaoRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testa o cálculo do limite do cartão (RF16) ao criar, editar e excluir
 * transações — garante que editar/excluir uma despesa no cartão sempre
 * estorna corretamente a contribuição antiga antes de aplicar a nova.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService — RF16 (limite do cartão) e edição")
class TransacaoServiceTest {

    @Mock private TransacaoRepository transacaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private SugestaoIaRepository sugestaoIaRepository;
    @Mock private CategorizacaoService categorizacaoService;

    private TransacaoService service;

    @BeforeEach
    void setUp() {
        service = new TransacaoService(transacaoRepository, usuarioRepository, categoriaRepository,
                sugestaoIaRepository, categorizacaoService);
    }

    private Usuario usuarioComLimite(BigDecimal utilizado) {
        return Usuario.builder().id(1L).limiteCreditoTotal(new BigDecimal("2000")).limiteCreditoUtilizado(utilizado).build();
    }

    @Test
    @DisplayName("criar despesa via cartão soma o valor ao limite utilizado")
    void criarDespesaViaCartao_somaAoLimite() {
        Usuario usuario = usuarioComLimite(new BigDecimal("500"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categorizacaoService.classificar(any())).thenReturn(Optional.empty());

        TransacaoDTO.Request dto = new TransacaoDTO.Request(1L, null, "compra", new BigDecimal("300"),
                Transacao.TipoTransacao.DESPESA, LocalDate.now(), true);

        service.criar(dto);

        assertThat(usuario.getLimiteCreditoUtilizado()).isEqualByComparingTo("800");
    }

    @Test
    @DisplayName("criar despesa que NÃO é no cartão não altera o limite")
    void criarDespesaSemCartao_naoAlteraLimite() {
        Usuario usuario = usuarioComLimite(new BigDecimal("500"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categorizacaoService.classificar(any())).thenReturn(Optional.empty());

        TransacaoDTO.Request dto = new TransacaoDTO.Request(1L, null, "compra", new BigDecimal("300"),
                Transacao.TipoTransacao.DESPESA, LocalDate.now(), false);

        service.criar(dto);

        assertThat(usuario.getLimiteCreditoUtilizado()).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("excluir despesa do cartão estorna o valor do limite")
    void excluirDespesaDoCartao_estornaLimite() {
        Usuario usuario = usuarioComLimite(new BigDecimal("800"));
        Transacao transacao = Transacao.builder()
                .id(10L).usuario(usuario).descricao("compra").valor(new BigDecimal("300"))
                .tipo(Transacao.TipoTransacao.DESPESA).data(LocalDate.now()).viaCartaoCredito(true).build();
        when(transacaoRepository.findById(10L)).thenReturn(Optional.of(transacao));

        service.excluir(10L);

        assertThat(usuario.getLimiteCreditoUtilizado()).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("editar valor de uma despesa no cartão ajusta o limite pela diferença, não soma tudo de novo")
    void editarValorDespesaCartao_ajustaPelaDiferenca() {
        Usuario usuario = usuarioComLimite(new BigDecimal("800")); // já inclui os 300 da transação original
        Categoria categoria = Categoria.builder().id(1L).nome("Alimentação").build();
        Transacao transacao = Transacao.builder()
                .id(10L).usuario(usuario).descricao("compra").valor(new BigDecimal("300"))
                .tipo(Transacao.TipoTransacao.DESPESA).data(LocalDate.now()).viaCartaoCredito(true).build();
        when(transacaoRepository.findById(10L)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(sugestaoIaRepository.findByTransacaoId(10L)).thenReturn(Optional.empty());

        // Corrige o valor de 300 para 500 (mesma transação, mesma forma de pagamento)
        TransacaoDTO.Request dto = new TransacaoDTO.Request(1L, 1L, "compra corrigida", new BigDecimal("500"),
                Transacao.TipoTransacao.DESPESA, LocalDate.now(), true);

        service.editar(10L, dto);

        // 800 (atual) - 300 (contribuição antiga) + 500 (contribuição nova) = 1000
        assertThat(usuario.getLimiteCreditoUtilizado()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("editar e manter a categoria sugerida pela IA marca a sugestão como aceita")
    void editarMantendoCategoriaSugerida_marcaSugestaoComoAceita() {
        Usuario usuario = usuarioComLimite(BigDecimal.ZERO);
        Categoria categoriaSugerida = Categoria.builder().id(2L).nome("Transporte").build();
        Transacao transacao = Transacao.builder()
                .id(20L).usuario(usuario).descricao("uber").valor(new BigDecimal("50"))
                .categoria(categoriaSugerida)
                .tipo(Transacao.TipoTransacao.DESPESA).data(LocalDate.now()).viaCartaoCredito(false).build();
        SugestaoIa sugestao = SugestaoIa.builder().id(1L).transacao(transacao).categoriaSugerida(categoriaSugerida).aceita(false).build();

        when(transacaoRepository.findById(20L)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaSugerida));
        when(sugestaoIaRepository.findByTransacaoId(20L)).thenReturn(Optional.of(sugestao));

        // Usuário confirma a mesma categoria (id=2) que a IA sugeriu
        TransacaoDTO.Request dto = new TransacaoDTO.Request(1L, 2L, "uber", new BigDecimal("50"),
                Transacao.TipoTransacao.DESPESA, LocalDate.now(), false);

        service.editar(20L, dto);

        assertThat(sugestao.isAceita()).isTrue();
    }

    @Test
    @DisplayName("editar TROCANDO a categoria sugerida pela IA marca a sugestão como não aceita")
    void editarTrocandoCategoriaSugerida_marcaSugestaoComoNaoAceita() {
        Usuario usuario = usuarioComLimite(BigDecimal.ZERO);
        Categoria categoriaSugerida = Categoria.builder().id(2L).nome("Transporte").build();
        Categoria categoriaCorreta = Categoria.builder().id(3L).nome("Lazer").build();
        Transacao transacao = Transacao.builder()
                .id(20L).usuario(usuario).descricao("uber pra festa").valor(new BigDecimal("50"))
                .categoria(categoriaSugerida)
                .tipo(Transacao.TipoTransacao.DESPESA).data(LocalDate.now()).viaCartaoCredito(false).build();
        SugestaoIa sugestao = SugestaoIa.builder().id(1L).transacao(transacao).categoriaSugerida(categoriaSugerida).aceita(false).build();

        when(transacaoRepository.findById(20L)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoriaCorreta));
        when(sugestaoIaRepository.findByTransacaoId(20L)).thenReturn(Optional.of(sugestao));

        // Usuário corrige para categoria diferente (id=3) da que a IA sugeriu (id=2)
        TransacaoDTO.Request dto = new TransacaoDTO.Request(1L, 3L, "uber pra festa", new BigDecimal("50"),
                Transacao.TipoTransacao.DESPESA, LocalDate.now(), false);

        service.editar(20L, dto);

        assertThat(sugestao.isAceita()).isFalse();
    }
}

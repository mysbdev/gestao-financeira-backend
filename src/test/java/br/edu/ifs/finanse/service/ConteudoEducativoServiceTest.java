package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.ConteudoEducativoDTO;
import br.edu.ifs.finanse.entity.Meta;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.enums.CategoriaConteudo;
import br.edu.ifs.finanse.repository.MetaRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes do RF17 — Recomendar conteúdo educativo (Quadro 16 do TCC).
 *
 * Cobre as três condições descritas no fluxo principal e no fluxo
 * alternativo do caso de uso: uso alto do rotativo, meta em andamento,
 * e nenhuma das duas (fallback só para conteúdo geral).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF17 — ConteudoEducativoService")
class ConteudoEducativoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MetaRepository metaRepository;

    private ConteudoEducativoService service;

    @BeforeEach
    void setUp() {
        // Catálogo real (não mockado) — garante que os testes também validam
        // que cada categoria tem conteúdo de verdade cadastrado.
        service = new ConteudoEducativoService(usuarioRepository, metaRepository, new ConteudoEducativoCatalogo());
    }

    private Usuario usuarioComUso(BigDecimal limiteTotal, BigDecimal limiteUtilizado) {
        return Usuario.builder()
                .id(1L)
                .nome("Usuário Teste")
                .limiteCreditoTotal(limiteTotal)
                .limiteCreditoUtilizado(limiteUtilizado)
                .build();
    }

    private Meta metaAtivaComProgresso(BigDecimal valorAlvo, BigDecimal valorAtual) {
        return Meta.builder()
                .id(1L)
                .valorAlvo(valorAlvo)
                .valorAtual(valorAtual)
                .status(Meta.StatusMeta.ATIVA)
                .dataCriacao(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("Fluxo principal: uso alto do rotativo")
    class UsoAltoRotativo {

        @Test
        @DisplayName("com 78% do limite usado (cenário da Maria), inclui ROTATIVO + GERAL")
        void usuarioComSetentaEOitoPorCentoDeUso_recebeRotativoEGeral() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("2000.00"), new BigDecimal("1560.00"))));
            when(metaRepository.findByUsuarioIdAndStatus(1L, Meta.StatusMeta.ATIVA)).thenReturn(List.of());

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(1L);

            assertThat(resultado)
                    .extracting(ConteudoEducativoDTO::categoria)
                    .contains(CategoriaConteudo.ROTATIVO)
                    .contains(CategoriaConteudo.GERAL)
                    .doesNotContain(CategoriaConteudo.POUPANCA);
        }

        @Test
        @DisplayName("exatamente no limiar de 50%, já dispara o alerta (>=, não >)")
        void usuarioExatamenteNoLimiar_disparaRotativo() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("1000.00"), new BigDecimal("500.00"))));
            when(metaRepository.findByUsuarioIdAndStatus(1L, Meta.StatusMeta.ATIVA)).thenReturn(List.of());

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(1L);

            assertThat(resultado).extracting(ConteudoEducativoDTO::categoria).contains(CategoriaConteudo.ROTATIVO);
        }

        @Test
        @DisplayName("com 49% do limite usado, NÃO dispara o alerta de rotativo")
        void usuarioAbaixoDoLimiar_naoRecebeRotativo() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("1000.00"), new BigDecimal("490.00"))));
            when(metaRepository.findByUsuarioIdAndStatus(1L, Meta.StatusMeta.ATIVA)).thenReturn(List.of());

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(1L);

            assertThat(resultado).extracting(ConteudoEducativoDTO::categoria).doesNotContain(CategoriaConteudo.ROTATIVO);
        }
    }

    @Nested
    @DisplayName("Fluxo principal: meta em andamento")
    class MetaEmAndamento {

        @Test
        @DisplayName("com meta ativa e progresso < 100%, inclui POUPANCA + GERAL")
        void metaAtivaEmAndamento_recebePoupancaEGeral() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("1000.00"), BigDecimal.ZERO)));
            when(metaRepository.findByUsuarioIdAndStatus(1L, Meta.StatusMeta.ATIVA))
                    .thenReturn(List.of(metaAtivaComProgresso(new BigDecimal("5000.00"), new BigDecimal("3200.00"))));

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(1L);

            assertThat(resultado)
                    .extracting(ConteudoEducativoDTO::categoria)
                    .contains(CategoriaConteudo.POUPANCA)
                    .contains(CategoriaConteudo.GERAL)
                    .doesNotContain(CategoriaConteudo.ROTATIVO);
        }

        @Test
        @DisplayName("com meta já 100% concluída, NÃO conta como 'em andamento'")
        void metaJaConcluida_naoRecebePoupanca() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("1000.00"), BigDecimal.ZERO)));
            when(metaRepository.findByUsuarioIdAndStatus(1L, Meta.StatusMeta.ATIVA))
                    .thenReturn(List.of(metaAtivaComProgresso(new BigDecimal("5000.00"), new BigDecimal("5000.00"))));

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(1L);

            assertThat(resultado).extracting(ConteudoEducativoDTO::categoria).doesNotContain(CategoriaConteudo.POUPANCA);
        }
    }

    @Nested
    @DisplayName("Fluxo alternativo 3a: nenhuma condição específica")
    class FluxoAlternativo {

        @Test
        @DisplayName("sem uso alto do rotativo e sem meta ativa, recebe SÓ conteúdo geral")
        void usuarioJoao_semSinaisEspecificos_recebeApenasGeral() {
            when(usuarioRepository.findById(2L))
                    .thenReturn(java.util.Optional.of(usuarioComUso(new BigDecimal("3000.00"), new BigDecimal("200.00"))));
            when(metaRepository.findByUsuarioIdAndStatus(2L, Meta.StatusMeta.ATIVA)).thenReturn(List.of());

            List<ConteudoEducativoDTO> resultado = service.recomendarPara(2L);

            assertThat(resultado)
                    .extracting(ConteudoEducativoDTO::categoria)
                    .containsOnly(CategoriaConteudo.GERAL);
        }
    }

    @Test
    @DisplayName("usuário inexistente lança EntityNotFoundException")
    void usuarioInexistente_lancaExcecao() {
        when(usuarioRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.recomendarPara(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }
}

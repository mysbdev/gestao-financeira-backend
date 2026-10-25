package br.edu.ifs.finanse.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o cálculo de percentual de uso do limite do cartão — base do RF16
 * (alerta de rotativo) e do RF17 (recomendação de conteúdo).
 */
@DisplayName("Usuario — getPercentualLimiteUtilizado (base do RF16)")
class UsuarioTest {

    @Test
    @DisplayName("calcula o percentual corretamente (cenário da Maria: 1560/2000 = 78%)")
    void calculaPercentualCorretamente() {
        Usuario usuario = Usuario.builder()
                .limiteCreditoTotal(new BigDecimal("2000.00"))
                .limiteCreditoUtilizado(new BigDecimal("1560.00"))
                .build();

        assertThat(usuario.getPercentualLimiteUtilizado()).isEqualByComparingTo("0.78");
    }

    @Test
    @DisplayName("limite total zero não gera divisão por zero — retorna 0")
    void limiteTotalZero_retornaZero() {
        Usuario usuario = Usuario.builder()
                .limiteCreditoTotal(BigDecimal.ZERO)
                .limiteCreditoUtilizado(new BigDecimal("100.00"))
                .build();

        assertThat(usuario.getPercentualLimiteUtilizado()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("limite total nulo (usuário que nunca configurou cartão) retorna 0, sem lançar exceção")
    void limiteTotalNulo_retornaZeroSemExcecao() {
        Usuario usuario = Usuario.builder().limiteCreditoTotal(null).limiteCreditoUtilizado(null).build();

        assertThat(usuario.getPercentualLimiteUtilizado()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("uso maior que o limite (estourado) retorna percentual acima de 1")
    void usoAcimaDoLimite_retornaAcimaDeCem() {
        Usuario usuario = Usuario.builder()
                .limiteCreditoTotal(new BigDecimal("1000.00"))
                .limiteCreditoUtilizado(new BigDecimal("1200.00"))
                .build();

        assertThat(usuario.getPercentualLimiteUtilizado()).isEqualByComparingTo("1.2");
    }
}

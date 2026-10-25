package br.edu.ifs.finanse.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Testa o cálculo de progresso da meta — base do RF12 (acompanhar progresso). */
@DisplayName("Meta — getProgresso (base do RF12)")
class MetaTest {

    @Test
    @DisplayName("calcula o progresso corretamente (cenário da Maria: 3200/5000 = 64%)")
    void calculaProgressoCorretamente() {
        Meta meta = Meta.builder()
                .valorAlvo(new BigDecimal("5000.00"))
                .valorAtual(new BigDecimal("3200.00"))
                .build();

        assertThat(meta.getProgresso()).isEqualByComparingTo("0.64");
    }

    @Test
    @DisplayName("progresso nunca ultrapassa 100%, mesmo se valorAtual > valorAlvo")
    void progressoNuncaUltrapassaCemPorCento() {
        Meta meta = Meta.builder()
                .valorAlvo(new BigDecimal("1000.00"))
                .valorAtual(new BigDecimal("1500.00"))
                .build();

        assertThat(meta.getProgresso()).isEqualByComparingTo("1");
    }

    @Test
    @DisplayName("valor alvo zero não gera divisão por zero — retorna 0")
    void valorAlvoZero_retornaZero() {
        Meta meta = Meta.builder().valorAlvo(BigDecimal.ZERO).valorAtual(new BigDecimal("100.00")).build();

        assertThat(meta.getProgresso()).isEqualByComparingTo("0");
    }
}

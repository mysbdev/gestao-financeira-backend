package br.edu.ifs.finanse.config;

import java.math.BigDecimal;

/**
 * Constantes de regras de negócio usadas em mais de um lugar (RF16, RF17),
 * centralizadas para evitar duplicação/valores divergentes entre services.
 */
public final class RegrasFinanceiras {

    private RegrasFinanceiras() {}

    /** Percentual do limite do cartão a partir do qual o alerta de rotativo (RF16) dispara. */
    public static final BigDecimal LIMIAR_ALERTA_ROTATIVO = new BigDecimal("0.50");
}

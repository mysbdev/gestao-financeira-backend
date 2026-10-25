package br.edu.ifs.finanse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class OnboardingDTO {

    /**
     * Informar renda mensal + escolher/criar meta inicial (User Flow: "Informar renda mensal"
     * -> "Tipo de meta?" -> "Selecionar meta pre-definida" / "Criar meta personalizada"
     * -> "Confirmar plano de economia").
     */
    public record Request(
            @NotNull @Positive(message = "rendaMensal deve ser positiva") BigDecimal rendaMensal,
            @NotNull(message = "tipoMetaId é obrigatório") Long tipoMetaId,
            @NotNull(message = "nomeMeta é obrigatório") String nomeMeta,
            @NotNull @Positive(message = "valorAlvoMeta deve ser positivo") BigDecimal valorAlvoMeta,
            Integer prazoMeses
    ) {}

    public record Response(
            Long usuarioId,
            boolean concluido,
            Long metaCriadaId
    ) {}
}

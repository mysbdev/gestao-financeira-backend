package br.edu.ifs.finanse.dto;

import br.edu.ifs.finanse.entity.Meta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MetaDTO {

    public record Request(
            @NotNull(message = "usuarioId é obrigatório") Long usuarioId,
            @NotNull(message = "tipoMetaId é obrigatório") Long tipoMetaId,
            @NotBlank(message = "nome é obrigatório") String nome,
            @NotNull @Positive(message = "valorAlvo deve ser positivo") BigDecimal valorAlvo,
            Integer prazoMeses
    ) {}

    public record Response(
            Long id,
            Long usuarioId,
            Long tipoMetaId,
            String tipoMetaDescricao,
            String nome,
            BigDecimal valorAlvo,
            BigDecimal valorAtual,
            BigDecimal progresso,
            Integer prazoMeses,
            LocalDate dataCriacao,
            Meta.StatusMeta status
    ) {}

    /** Corpo de POST /api/metas/{id}/aportes — aceita aporte (soma) ou retirada (subtrai). */
    public record MovimentacaoRequest(
            @NotNull @Positive(message = "valor deve ser positivo") BigDecimal valor,
            @NotNull(message = "tipo é obrigatório (APORTE ou RETIRADA)") Meta.TipoMovimentacao tipo
    ) {}
}

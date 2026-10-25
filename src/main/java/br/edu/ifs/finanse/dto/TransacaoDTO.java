package br.edu.ifs.finanse.dto;

import br.edu.ifs.finanse.entity.Transacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoDTO {

    public record Request(
            @NotNull(message = "usuarioId é obrigatório") Long usuarioId,
            Long categoriaId,
            @NotBlank(message = "descricao é obrigatória") String descricao,
            @NotNull @Positive(message = "valor deve ser positivo") BigDecimal valor,
            @NotNull(message = "tipo é obrigatório (RECEITA ou DESPESA)") Transacao.TipoTransacao tipo,
            @NotNull(message = "data é obrigatória") LocalDate data,
            boolean viaCartaoCredito
    ) {}

    public record Response(
            Long id,
            Long usuarioId,
            Long categoriaId,
            String categoriaNome,
            String descricao,
            BigDecimal valor,
            Transacao.TipoTransacao tipo,
            LocalDate data,
            boolean viaCartaoCredito,
            /** true quando essa despesa via cartão empurrou o usuário para o alerta de rotativo (RF16). */
            boolean alertaRotativo
    ) {}
}

package br.edu.ifs.finanse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UsuarioDTO {

    /** RF15 — Editar perfil: nome, e-mail (opcional), renda mensal e dados do cartão. */
    public record AtualizarRequest(
            @NotBlank(message = "nome é obrigatório") String nome,
            @NotBlank @Email(message = "email inválido") String email,
            LocalDate dataNascimento,
            @PositiveOrZero(message = "rendaMensal não pode ser negativa") BigDecimal rendaMensal,
            @PositiveOrZero(message = "limiteCreditoTotal não pode ser negativo") BigDecimal limiteCreditoTotal
    ) {}

    public record Response(
            Long id,
            String nome,
            String email,
            LocalDate dataNascimento,
            BigDecimal rendaMensal,
            BigDecimal limiteCreditoTotal,
            BigDecimal limiteCreditoUtilizado
    ) {}
}

package br.edu.ifs.finanse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AuthDTO {

    /** RF (Quadro 3) — Realizar cadastro: nome, e-mail, senha e data de nascimento. */
    public record CadastroRequest(
            @NotBlank(message = "nome é obrigatório") String nome,
            @NotBlank @Email(message = "email inválido") String email,
            @NotBlank @Size(min = 6, message = "senha deve ter ao menos 6 caracteres") String senha,
            LocalDate dataNascimento
    ) {}

    public record LoginRequest(
            @NotBlank @Email(message = "email inválido") String email,
            @NotBlank(message = "senha é obrigatória") String senha
    ) {}

    public record LoginResponse(
            String token,
            Long usuarioId,
            String nome,
            String email,
            boolean onboardingCompleto
    ) {}
}

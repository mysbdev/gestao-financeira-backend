package br.edu.ifs.finanse.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoriaDTO {

    public record Request(
            @NotBlank(message = "nome é obrigatório") String nome,
            String icone
    ) {}

    public record Response(
            Long id,
            String nome,
            String icone
    ) {}
}

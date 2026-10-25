package br.edu.ifs.finanse.dto;

import br.edu.ifs.finanse.enums.CategoriaConteudo;

/**
 * Representa um conteúdo educativo recomendado ao usuário (RF17).
 */
public record ConteudoEducativoDTO(
        CategoriaConteudo categoria,
        String tituloCategoria,
        String motivo,
        String titulo,
        String fonte,
        String url
) {
}

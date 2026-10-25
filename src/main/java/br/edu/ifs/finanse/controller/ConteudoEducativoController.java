package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.ConteudoEducativoDTO;
import br.edu.ifs.finanse.service.ConteudoEducativoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/educacao")
@RequiredArgsConstructor
public class ConteudoEducativoController {

    private final ConteudoEducativoService conteudoEducativoService;

    /**
     * RF17 — retorna os conteúdos educativos recomendados para o usuário,
     * segmentados conforme uso do crédito rotativo e progresso das metas.
     */
    @GetMapping("/recomendacoes")
    public List<ConteudoEducativoDTO> recomendacoes(@PathVariable Long usuarioId) {
        return conteudoEducativoService.recomendarPara(usuarioId);
    }
}

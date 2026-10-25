package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.GraficoDTO;
import br.edu.ifs.finanse.service.GraficoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/graficos")
@RequiredArgsConstructor
public class GraficoController {

    private final GraficoService graficoService;

    /** RF11 — Visualizar gráficos de distribuição de despesas por categoria. */
    @GetMapping
    public GraficoDTO.Resumo resumo(@PathVariable Long usuarioId) {
        return graficoService.resumoPorUsuario(usuarioId);
    }
}

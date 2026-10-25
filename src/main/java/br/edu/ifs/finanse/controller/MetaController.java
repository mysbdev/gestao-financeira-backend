package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.MetaDTO;
import br.edu.ifs.finanse.service.MetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    /** RF12 — Acompanhar progresso da meta. */
    @GetMapping
    public List<MetaDTO.Response> listarPorUsuario(@RequestParam Long usuarioId) {
        return metaService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetaDTO.Response criar(@Valid @RequestBody MetaDTO.Request dto) {
        return metaService.criar(dto);
    }

    @PutMapping("/{id}")
    public MetaDTO.Response editar(@PathVariable Long id, @Valid @RequestBody MetaDTO.Request dto) {
        return metaService.editar(id, dto);
    }

    @PostMapping("/{id}/aportes")
    public MetaDTO.Response registrarMovimentacao(@PathVariable Long id, @RequestBody MetaDTO.MovimentacaoRequest body) {
        return metaService.registrarMovimentacao(id, body.valor(), body.tipo());
    }

    @PatchMapping("/{id}/pausar")
    public MetaDTO.Response pausar(@PathVariable Long id) {
        return metaService.pausar(id);
    }

    @PatchMapping("/{id}/reativar")
    public MetaDTO.Response reativar(@PathVariable Long id) {
        return metaService.reativar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        metaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

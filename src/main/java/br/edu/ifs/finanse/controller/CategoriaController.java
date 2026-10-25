package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.CategoriaDTO;
import br.edu.ifs.finanse.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDTO.Response> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public CategoriaDTO.Response buscarPorId(@PathVariable Long id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDTO.Response criar(@Valid @RequestBody CategoriaDTO.Request dto) {
        return categoriaService.criar(dto);
    }

    @PutMapping("/{id}")
    public CategoriaDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO.Request dto) {
        return categoriaService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

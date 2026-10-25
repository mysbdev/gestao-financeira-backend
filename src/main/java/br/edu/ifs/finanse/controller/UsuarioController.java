package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.UsuarioDTO;
import br.edu.ifs.finanse.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{id}")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public UsuarioDTO.Response buscarPerfil(@PathVariable Long id) {
        return usuarioService.buscarPerfil(id);
    }

    /** RF15 — Editar perfil. */
    @PutMapping
    public UsuarioDTO.Response atualizarPerfil(@PathVariable Long id, @Valid @RequestBody UsuarioDTO.AtualizarRequest dto) {
        return usuarioService.atualizarPerfil(id, dto);
    }
}

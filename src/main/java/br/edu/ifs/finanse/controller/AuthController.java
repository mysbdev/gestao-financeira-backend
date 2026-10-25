package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.AuthDTO;
import br.edu.ifs.finanse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Realizar cadastro (Quadro 3). */
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDTO.LoginResponse cadastrar(@Valid @RequestBody AuthDTO.CadastroRequest dto) {
        return authService.cadastrar(dto);
    }

    /** Realizar login. */
    @PostMapping("/login")
    public AuthDTO.LoginResponse login(@Valid @RequestBody AuthDTO.LoginRequest dto) {
        return authService.login(dto);
    }
}

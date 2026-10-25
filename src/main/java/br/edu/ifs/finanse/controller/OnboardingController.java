package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.OnboardingDTO;
import br.edu.ifs.finanse.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping
    public Map<String, Boolean> status(@PathVariable Long usuarioId) {
        return Map.of("concluido", onboardingService.statusConcluido(usuarioId));
    }

    /** "Confirmar plano de economia" no User Flow — conclui o onboarding e cria a meta inicial. */
    @PostMapping
    public OnboardingDTO.Response concluir(@PathVariable Long usuarioId, @Valid @RequestBody OnboardingDTO.Request dto) {
        return onboardingService.concluir(usuarioId, dto);
    }
}

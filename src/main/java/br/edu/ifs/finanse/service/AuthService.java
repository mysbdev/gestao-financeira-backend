package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.AuthDTO;
import br.edu.ifs.finanse.entity.Onboarding;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.OnboardingRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import br.edu.ifs.finanse.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final OnboardingRepository onboardingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthDTO.LoginResponse cadastrar(AuthDTO.CadastroRequest dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senhaHash(passwordEncoder.encode(dto.senha()))
                .dataNascimento(dto.dataNascimento())
                .limiteCreditoTotal(BigDecimal.ZERO)
                .limiteCreditoUtilizado(BigDecimal.ZERO)
                .criadoEm(LocalDateTime.now())
                .build();
        usuario = usuarioRepository.save(usuario);

        Onboarding onboarding = Onboarding.builder()
                .usuario(usuario)
                .concluido(false)
                .build();
        onboardingRepository.save(onboarding);

        String token = jwtUtil.gerarToken(usuario.getId(), usuario.getEmail());
        return new AuthDTO.LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), false);
    }

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
            );
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        boolean onboardingCompleto = onboardingRepository.findByUsuarioId(usuario.getId())
                .map(Onboarding::isConcluido)
                .orElse(false);

        String token = jwtUtil.gerarToken(usuario.getId(), usuario.getEmail());
        return new AuthDTO.LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), onboardingCompleto);
    }
}

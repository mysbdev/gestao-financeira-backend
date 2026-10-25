package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.OnboardingDTO;
import br.edu.ifs.finanse.entity.Meta;
import br.edu.ifs.finanse.entity.Onboarding;
import br.edu.ifs.finanse.entity.TipoMeta;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.MetaRepository;
import br.edu.ifs.finanse.repository.OnboardingRepository;
import br.edu.ifs.finanse.repository.TipoMetaRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoMetaRepository tipoMetaRepository;
    private final MetaRepository metaRepository;

    @Transactional
    public OnboardingDTO.Response concluir(Long usuarioId, OnboardingDTO.Request dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + usuarioId));
        TipoMeta tipoMeta = tipoMetaRepository.findById(dto.tipoMetaId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de meta não encontrado: " + dto.tipoMetaId()));

        usuario.setRendaMensal(dto.rendaMensal());
        usuarioRepository.save(usuario);

        Meta meta = Meta.builder()
                .usuario(usuario)
                .tipoMeta(tipoMeta)
                .nome(dto.nomeMeta())
                .valorAlvo(dto.valorAlvoMeta())
                .valorAtual(BigDecimal.ZERO)
                .prazoMeses(dto.prazoMeses())
                .dataCriacao(LocalDate.now())
                .status(Meta.StatusMeta.ATIVA)
                .build();
        meta = metaRepository.save(meta);

        Onboarding onboarding = onboardingRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> Onboarding.builder().usuario(usuario).build());
        onboarding.setConcluido(true);
        onboarding.setConcluidoEm(LocalDateTime.now());
        onboardingRepository.save(onboarding);

        return new OnboardingDTO.Response(usuarioId, true, meta.getId());
    }

    public boolean statusConcluido(Long usuarioId) {
        return onboardingRepository.findByUsuarioId(usuarioId)
                .map(Onboarding::isConcluido)
                .orElse(false);
    }
}

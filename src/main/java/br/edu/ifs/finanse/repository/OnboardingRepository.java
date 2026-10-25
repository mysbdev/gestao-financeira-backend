package br.edu.ifs.finanse.repository;

import br.edu.ifs.finanse.entity.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {

    Optional<Onboarding> findByUsuarioId(Long usuarioId);
}

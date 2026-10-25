package br.edu.ifs.finanse.repository;

import br.edu.ifs.finanse.entity.SugestaoIa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SugestaoIaRepository extends JpaRepository<SugestaoIa, Long> {

    Optional<SugestaoIa> findByTransacaoId(Long transacaoId);
}

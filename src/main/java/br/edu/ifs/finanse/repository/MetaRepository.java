package br.edu.ifs.finanse.repository;

import br.edu.ifs.finanse.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioIdAndStatus(Long usuarioId, Meta.StatusMeta status);
}

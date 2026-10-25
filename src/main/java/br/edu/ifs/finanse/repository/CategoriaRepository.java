package br.edu.ifs.finanse.repository;

import br.edu.ifs.finanse.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}

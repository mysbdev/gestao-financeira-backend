package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.CategoriaDTO;
import br.edu.ifs.finanse.entity.Categoria;
import br.edu.ifs.finanse.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaDTO.Response> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoriaDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public CategoriaDTO.Response criar(CategoriaDTO.Request dto) {
        Categoria categoria = Categoria.builder()
                .nome(dto.nome())
                .icone(dto.icone())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Request dto) {
        Categoria categoria = buscarEntidade(id);
        categoria.setNome(dto.nome());
        categoria.setIcone(dto.icone());
        return toResponse(categoriaRepository.save(categoria));
    }

    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + id));
    }

    private CategoriaDTO.Response toResponse(Categoria c) {
        return new CategoriaDTO.Response(c.getId(), c.getNome(), c.getIcone());
    }
}

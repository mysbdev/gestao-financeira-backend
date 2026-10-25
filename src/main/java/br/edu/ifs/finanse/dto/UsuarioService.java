package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.UsuarioDTO;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDTO.Response buscarPerfil(Long id) {
        return toResponse(buscarEntidade(id));
    }

    /**
     * RF15 — Editar perfil. Note que limiteCreditoUtilizado NÃO é editável
     * por aqui: ele é derivado das despesas registradas via cartão (RF16),
     * só o limite total é um dado que o usuário pode ajustar diretamente.
     */
    public UsuarioDTO.Response atualizarPerfil(Long id, UsuarioDTO.AtualizarRequest dto) {
        Usuario usuario = buscarEntidade(id);

        if (!usuario.getEmail().equalsIgnoreCase(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail");
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setDataNascimento(dto.dataNascimento());
        usuario.setRendaMensal(dto.rendaMensal());
        usuario.setLimiteCreditoTotal(dto.limiteCreditoTotal());

        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }

    private UsuarioDTO.Response toResponse(Usuario u) {
        return new UsuarioDTO.Response(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getDataNascimento(),
                u.getRendaMensal(),
                u.getLimiteCreditoTotal(),
                u.getLimiteCreditoUtilizado()
        );
    }
}

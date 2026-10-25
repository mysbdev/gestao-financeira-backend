package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.config.RegrasFinanceiras;
import br.edu.ifs.finanse.dto.ConteudoEducativoDTO;
import br.edu.ifs.finanse.entity.Meta;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.enums.CategoriaConteudo;
import br.edu.ifs.finanse.repository.MetaRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementa RF17 — Recomendar conteúdo educativo (Quadro 16 do TCC).
 *
 * Regra de negócio (v1, regras fixas):
 *  - percentual de uso do limite do cartão >= RegrasFinanceiras.LIMIAR_ALERTA_ROTATIVO -> categoria ROTATIVO
 *  - existe meta ATIVA com progresso < 100%                                             -> categoria POUPANCA
 *  - nenhuma das condições acima                                                        -> apenas GERAL (fluxo alternativo 3a)
 *  - GERAL é sempre incluída ao final, como reforço de base
 */
@Service
@RequiredArgsConstructor
public class ConteudoEducativoService {

    private final UsuarioRepository usuarioRepository;
    private final MetaRepository metaRepository;
    private final ConteudoEducativoCatalogo catalogo;

    public List<ConteudoEducativoDTO> recomendarPara(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + usuarioId));

        List<Meta> metasAtivas = metaRepository.findByUsuarioIdAndStatus(usuarioId, Meta.StatusMeta.ATIVA);

        boolean usoAltoRotativo = usuario.getPercentualLimiteUtilizado()
                .compareTo(RegrasFinanceiras.LIMIAR_ALERTA_ROTATIVO) >= 0;
        boolean temMetaEmAndamento = metasAtivas.stream()
                .anyMatch(m -> m.getProgresso().compareTo(BigDecimal.ONE) < 0);

        List<ConteudoEducativoDTO> recomendacoes = new ArrayList<>();

        if (usoAltoRotativo) {
            recomendacoes.addAll(catalogo.porCategoria(CategoriaConteudo.ROTATIVO));
        }
        if (temMetaEmAndamento) {
            recomendacoes.addAll(catalogo.porCategoria(CategoriaConteudo.POUPANCA));
        }

        // Fluxo alternativo 3a: nenhuma condição específica -> só conteúdo geral.
        // Caso contrário, GERAL entra como reforço, sempre por último.
        recomendacoes.addAll(catalogo.porCategoria(CategoriaConteudo.GERAL));

        return recomendacoes;
    }
}

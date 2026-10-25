package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.entity.Categoria;
import br.edu.ifs.finanse.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementa RF10 — Categorização automática de despesas.
 *
 * v1: heurística por palavras-chave, roda localmente, sem custo e sem
 * dependência de rede — ideal para o TCC2 e para desenvolvimento/demo.
 *
 * O ponto de extensão é único: trocar apenas o método {@link #classificar},
 * mantendo a mesma assinatura (descrição -> categoria + confiança), para
 * futuramente chamar uma API externa de NLP (conforme já previsto na seção
 * 3.2 do TCC), sem precisar alterar TransacaoService nem os controllers.
 */
@Service
@RequiredArgsConstructor
public class CategorizacaoService {

    private final CategoriaRepository categoriaRepository;

    /** Categoria -> palavras-chave associadas (tudo em minúsculo, sem acento). */
    private static final Map<String, List<String>> PALAVRAS_CHAVE = new LinkedHashMap<>();
    static {
        PALAVRAS_CHAVE.put("Alimentação", List.of(
                "mercado", "supermercado", "restaurante", "lanchonete", "ifood", "padaria",
                "acougue", "feira", "almoco", "jantar", "lanche", "pizza", "hamburguer", "cafe"));
        PALAVRAS_CHAVE.put("Transporte", List.of(
                "uber", "99", "combustivel", "gasolina", "onibus", "passagem", "pedagio",
                "estacionamento", "metro", "taxi", "oficina", "mecanico"));
        PALAVRAS_CHAVE.put("Lazer", List.of(
                "cinema", "show", "netflix", "spotify", "jogo", "bar", "viagem", "streaming",
                "parque", "ingresso", "festa"));
        PALAVRAS_CHAVE.put("Saúde", List.of(
                "farmacia", "remedio", "medico", "consulta", "exame", "plano de saude",
                "academia", "dentista", "hospital"));
        PALAVRAS_CHAVE.put("Moradia", List.of(
                "aluguel", "condominio", "luz", "energia", "agua", "internet", "gas",
                "iptu", "reforma", "moveis"));
        PALAVRAS_CHAVE.put("Educação", List.of(
                "curso", "faculdade", "mensalidade", "livro", "material escolar", "apostila"));
    }

    public record Resultado(Categoria categoria, double confianca) {}

    /**
     * Classifica a descrição de uma despesa e retorna a categoria mais provável.
     * Retorna Optional.empty() quando nenhuma palavra-chave bate — nesse caso o
     * TransacaoService mantém a transação sem categoria, para o usuário confirmar
     * manualmente (fluxo alternativo do RF10).
     */
    public Optional<Resultado> classificar(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return Optional.empty();
        }

        String textoNormalizado = normalizar(descricao);

        String melhorCategoria = null;
        int melhorScore = 0;

        for (Map.Entry<String, List<String>> entrada : PALAVRAS_CHAVE.entrySet()) {
            int score = 0;
            for (String palavra : entrada.getValue()) {
                if (textoNormalizado.contains(normalizar(palavra))) {
                    score++;
                }
            }
            if (score > melhorScore) {
                melhorScore = score;
                melhorCategoria = entrada.getKey();
            }
        }

        if (melhorCategoria == null) {
            return Optional.empty();
        }

        // Confiança simples: 1 palavra-chave batida = 0.75; cada palavra extra soma até 0.95.
        double confianca = Math.min(0.75 + (melhorScore - 1) * 0.10, 0.95);

        final String nomeCategoria = melhorCategoria;
        return categoriaRepository.findAll().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nomeCategoria))
                .findFirst()
                .map(categoria -> new Resultado(categoria, confianca));
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase();
    }
}

package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.entity.Categoria;
import br.edu.ifs.finanse.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes do RF10 — Categorização automática de despesas (heurística por
 * palavras-chave). Cobre casos de acerto, não-acerto (fluxo alternativo:
 * transação fica sem categoria) e a normalização de acentos/maiúsculas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF10 — CategorizacaoService")
class CategorizacaoServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategorizacaoService service;

    private void comCategoriasCadastradas() {
        service = new CategorizacaoService(categoriaRepository);
        when(categoriaRepository.findAll()).thenReturn(List.of(
                Categoria.builder().id(1L).nome("Alimentação").build(),
                Categoria.builder().id(2L).nome("Transporte").build(),
                Categoria.builder().id(3L).nome("Lazer").build(),
                Categoria.builder().id(4L).nome("Saúde").build(),
                Categoria.builder().id(5L).nome("Moradia").build(),
                Categoria.builder().id(6L).nome("Educação").build()
        ));
    }

    @ParameterizedTest(name = "\"{0}\" deveria cair em {1}")
    @DisplayName("descrições com palavras-chave conhecidas classificam na categoria certa")
    @CsvSource({
            "ifood lanche, Alimentação",
            "Mercado Extra, Alimentação",
            "corrida de uber, Transporte",
            "abastecer com gasolina, Transporte",
            "assinatura Netflix, Lazer",
            "farmácia remédio, Saúde",
            "aluguel do apê, Moradia",
            "mensalidade da faculdade, Educação"
    })
    void descricaoComPalavraChave_classificaNaCategoriaCorreta(String descricao, String categoriaEsperada) {
        comCategoriasCadastradas();

        Optional<CategorizacaoService.Resultado> resultado = service.classificar(descricao);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().categoria().getNome()).isEqualTo(categoriaEsperada);
    }

    @Test
    @DisplayName("descrição sem nenhuma palavra-chave reconhecida não classifica (fica sem categoria)")
    void descricaoSemPalavraChave_naoClassifica() {
        // Não usamos comCategoriasCadastradas() aqui de propósito: quando nenhuma
        // palavra-chave bate, o método retorna antes mesmo de consultar o banco de
        // categorias — então não há stub nenhum a configurar neste caso.
        service = new CategorizacaoService(categoriaRepository);

        Optional<CategorizacaoService.Resultado> resultado = service.classificar("xpto consultoria empresarial");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("é insensível a maiúsculas/minúsculas e acentuação")
    void classificacaoIgnoraAcentosEMaiusculas() {
        comCategoriasCadastradas();

        Optional<CategorizacaoService.Resultado> resultado = service.classificar("PADARIA São José");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().categoria().getNome()).isEqualTo("Alimentação");
    }

    @Test
    @DisplayName("descrição em branco não classifica")
    void descricaoEmBranco_naoClassifica() {
        service = new CategorizacaoService(categoriaRepository);

        assertThat(service.classificar("")).isEmpty();
        assertThat(service.classificar(null)).isEmpty();
    }

    @Test
    @DisplayName("confiança fica entre 0.75 e 0.95, proporcional ao número de palavras-chave batidas")
    void confiancaFicaDentroDoIntervaloEsperado() {
        comCategoriasCadastradas();

        Optional<CategorizacaoService.Resultado> resultado = service.classificar("ifood");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().confianca()).isBetween(0.75, 0.95);
    }
}
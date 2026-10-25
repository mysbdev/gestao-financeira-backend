package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.ConteudoEducativoDTO;
import br.edu.ifs.finanse.enums.CategoriaConteudo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Catálogo estático de conteúdos educativos usados pela recomendação (RF17).
 *
 * Implementado como lista fixa no código (decisão de escopo do TCC 2 — ver
 * Quadro 16 do documento). Em uma evolução futura poderia migrar para uma
 * tabela no banco (ex.: CONTEUDO_EDUCATIVO) sem alterar o contrato do
 * ConteudoEducativoService, que depende apenas deste catálogo.
 */
@Component
public class ConteudoEducativoCatalogo {

    private final Map<CategoriaConteudo, List<ConteudoEducativoDTO>> conteudos = Map.of(

            CategoriaConteudo.ROTATIVO, List.of(
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.ROTATIVO, CategoriaConteudo.ROTATIVO.getTitulo(),
                            "Uso elevado do limite do cartão de crédito",
                            "Como funciona o crédito rotativo e por que ele é tão caro",
                            "Banco Central · Cidadania Financeira",
                            "https://www.bcb.gov.br/cidadaniafinanceira"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.ROTATIVO, CategoriaConteudo.ROTATIVO.getTitulo(),
                            "Uso elevado do limite do cartão de crédito",
                            "Passo a passo para renegociar dívidas em atraso",
                            "consumidor.gov.br",
                            "https://www.consumidor.gov.br/"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.ROTATIVO, CategoriaConteudo.ROTATIVO.getTitulo(),
                            "Uso elevado do limite do cartão de crédito",
                            "Calculadora do cidadão: simule quitação e parcelamento",
                            "Banco Central",
                            "https://www3.bcb.gov.br/CALCIDADAO/publico/exibirFormCartaoCredito.do?method=exibirFormCartaoCredito")
            ),

            CategoriaConteudo.POUPANCA, List.of(
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.POUPANCA, CategoriaConteudo.POUPANCA.getTitulo(),
                            "Meta financeira ativa em andamento",
                            "Reserva de emergência: quanto guardar e onde deixar o dinheiro",
                            "Meu Bolso em Dia · Febraban",
                            "https://www.meubolsoemdia.com.br/"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.POUPANCA, CategoriaConteudo.POUPANCA.getTitulo(),
                            "Meta financeira ativa em andamento",
                            "Método 50-30-20 para organizar o orçamento mensal",
                            "Vida e Dinheiro · Anbima",
                            "https://www.vidaedinheiro.gov.br/"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.POUPANCA, CategoriaConteudo.POUPANCA.getTitulo(),
                            "Meta financeira ativa em andamento",
                            "Como criar o hábito de poupar com renda variável",
                            "Serasa Ensina",
                            "https://www.serasa.com.br/ensina/")
            ),

            CategoriaConteudo.GERAL, List.of(
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.GERAL, CategoriaConteudo.GERAL.getTitulo(),
                            "Base geral de educação financeira",
                            "Trilhas gratuitas de educação financeira por perfil",
                            "Banco Central · Cidadania Financeira",
                            "https://www.bcb.gov.br/cidadaniafinanceira"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.GERAL, CategoriaConteudo.GERAL.getTitulo(),
                            "Base geral de educação financeira",
                            "Curso \"Cuidando do meu dinheiro\"",
                            "Febraban · Meu Bolso em Dia",
                            "https://www.meubolsoemdia.com.br/"),
                    new ConteudoEducativoDTO(
                            CategoriaConteudo.GERAL, CategoriaConteudo.GERAL.getTitulo(),
                            "Base geral de educação financeira",
                            "Glossário: termos financeiros explicados sem economês",
                            "Serasa Ensina",
                            "https://www.serasa.com.br/ensina/")
            )
    );

    public List<ConteudoEducativoDTO> porCategoria(CategoriaConteudo categoria) {
        return conteudos.getOrDefault(categoria, List.of());
    }
}

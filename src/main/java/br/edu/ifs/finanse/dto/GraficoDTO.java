package br.edu.ifs.finanse.dto;

import java.math.BigDecimal;
import java.util.List;

public class GraficoDTO {

    /** Fatia do gráfico de pizza/barras — total de despesas por categoria. */
    public record CategoriaFatia(
            String categoria,
            BigDecimal total,
            double percentual
    ) {}

    /** Ponto do gráfico de linha/barras — total por mês (formato yyyy-MM). */
    public record MesPonto(
            String mes,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas
    ) {}

    public record Resumo(
            List<CategoriaFatia> porCategoria,
            List<MesPonto> porMes,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas
    ) {}
}

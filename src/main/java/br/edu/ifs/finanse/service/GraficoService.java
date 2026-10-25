package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.GraficoDTO;
import br.edu.ifs.finanse.entity.Transacao;
import br.edu.ifs.finanse.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementa RF11 — Visualizar gráficos de distribuição de despesas por categoria.
 *
 * Agrega as transações do usuário de duas formas: total por categoria
 * (para gráfico de pizza/barras) e total por mês (para gráfico de evolução).
 * Tudo calculado em memória a partir do histórico já carregado — para o
 * volume de dados de um TCC isso é suficiente; se a base crescer muito,
 * o próximo passo natural é mover essa agregação para uma query SQL.
 */
@Service
@RequiredArgsConstructor
public class GraficoService {

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransacaoRepository transacaoRepository;

    public GraficoDTO.Resumo resumoPorUsuario(Long usuarioId) {
        List<Transacao> transacoes = transacaoRepository.findByUsuarioId(usuarioId);

        BigDecimal totalDespesas = somarPorTipo(transacoes, Transacao.TipoTransacao.DESPESA);
        BigDecimal totalReceitas = somarPorTipo(transacoes, Transacao.TipoTransacao.RECEITA);

        List<GraficoDTO.CategoriaFatia> porCategoria = agruparPorCategoria(transacoes, totalDespesas);
        List<GraficoDTO.MesPonto> porMes = agruparPorMes(transacoes);

        return new GraficoDTO.Resumo(porCategoria, porMes, totalReceitas, totalDespesas);
    }

    private BigDecimal somarPorTipo(List<Transacao> transacoes, Transacao.TipoTransacao tipo) {
        return transacoes.stream()
                .filter(t -> t.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<GraficoDTO.CategoriaFatia> agruparPorCategoria(List<Transacao> transacoes, BigDecimal totalDespesas) {
        Map<String, BigDecimal> somaPorCategoria = new LinkedHashMap<>();

        for (Transacao t : transacoes) {
            if (t.getTipo() != Transacao.TipoTransacao.DESPESA) continue;
            String nomeCategoria = t.getCategoria() != null ? t.getCategoria().getNome() : "Sem categoria";
            somaPorCategoria.merge(nomeCategoria, t.getValor(), BigDecimal::add);
        }

        return somaPorCategoria.entrySet().stream()
                .map(e -> new GraficoDTO.CategoriaFatia(
                        e.getKey(),
                        e.getValue(),
                        calcularPercentual(e.getValue(), totalDespesas)
                ))
                .sorted(Comparator.comparing(GraficoDTO.CategoriaFatia::total).reversed())
                .toList();
    }

    private List<GraficoDTO.MesPonto> agruparPorMes(List<Transacao> transacoes) {
        Map<String, BigDecimal[]> somaPorMes = new LinkedHashMap<>(); // [receitas, despesas]

        for (Transacao t : transacoes) {
            String mes = t.getData().format(FORMATO_MES);
            BigDecimal[] atual = somaPorMes.computeIfAbsent(mes, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t.getTipo() == Transacao.TipoTransacao.RECEITA) {
                atual[0] = atual[0].add(t.getValor());
            } else {
                atual[1] = atual[1].add(t.getValor());
            }
        }

        return somaPorMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new GraficoDTO.MesPonto(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    private double calcularPercentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return valor.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }
}

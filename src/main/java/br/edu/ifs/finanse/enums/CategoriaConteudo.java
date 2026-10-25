package br.edu.ifs.finanse.enums;

public enum CategoriaConteudo {

    /** Usuário com uso alto do crédito rotativo (prioridade máxima). */
    ROTATIVO("Sair do crédito rotativo"),

    /** Usuário com metas de poupança/reserva em andamento. */
    POUPANCA("Como poupar todo mês"),

    /** Fallback: nenhuma condição específica identificada. */
    GERAL("Educação financeira geral");

    private final String titulo;

    CategoriaConteudo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}

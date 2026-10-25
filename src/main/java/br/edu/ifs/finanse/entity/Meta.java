package br.edu.ifs.finanse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_meta_id", nullable = false)
    private TipoMeta tipoMeta;

    @Column(nullable = false)
    private String nome;

    @Column(name = "valor_alvo", nullable = false)
    private BigDecimal valorAlvo;

    @Column(name = "valor_atual", nullable = false)
    private BigDecimal valorAtual;

    @Column(name = "prazo_meses")
    private Integer prazoMeses;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMeta status;

    public enum StatusMeta { ATIVA, PAUSADA, CONCLUIDA }

    public enum TipoMovimentacao { APORTE, RETIRADA }

    /** Progresso da meta (0 a 1). Segundo sinal usado pela recomendação de conteúdo (RF17). */
    @Transient
    public BigDecimal getProgresso() {
        if (valorAlvo == null || valorAlvo.compareTo(BigDecimal.ZERO) == 0 || valorAtual == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal p = valorAtual.divide(valorAlvo, 4, RoundingMode.HALF_UP);
        return p.min(BigDecimal.ONE);
    }
}

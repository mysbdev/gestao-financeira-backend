package br.edu.ifs.finanse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    @Column(name = "data_nascimento")
    private java.time.LocalDate dataNascimento;

    /** Renda mensal declarada no onboarding. Usada para dar contexto às recomendações. */
    @Column(name = "renda_mensal")
    private BigDecimal rendaMensal;

    @Column(name = "limite_credito_total")
    private BigDecimal limiteCreditoTotal;

    @Column(name = "limite_credito_utilizado")
    private BigDecimal limiteCreditoUtilizado;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    /**
     * Percentual do limite de crédito rotativo em uso (0 a 1).
     * É o principal sinal usado pela recomendação de conteúdo educativo (RF17).
     */
    @Transient
    public BigDecimal getPercentualLimiteUtilizado() {
        if (limiteCreditoTotal == null || limiteCreditoTotal.compareTo(BigDecimal.ZERO) == 0
                || limiteCreditoUtilizado == null) {
            return BigDecimal.ZERO;
        }
        return limiteCreditoUtilizado.divide(limiteCreditoTotal, 4, java.math.RoundingMode.HALF_UP);
    }
}

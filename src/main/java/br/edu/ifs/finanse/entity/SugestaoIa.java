package br.edu.ifs.finanse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sugestao_ia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugestaoIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id", nullable = false, unique = true)
    private Transacao transacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_sugerida_id")
    private Categoria categoriaSugerida;

    private Double confianca;

    private boolean aceita;
}

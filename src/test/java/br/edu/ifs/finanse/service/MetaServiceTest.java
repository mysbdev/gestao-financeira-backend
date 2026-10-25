package br.edu.ifs.finanse.service;

import br.edu.ifs.finanse.dto.MetaDTO;
import br.edu.ifs.finanse.entity.Meta;
import br.edu.ifs.finanse.entity.TipoMeta;
import br.edu.ifs.finanse.entity.Usuario;
import br.edu.ifs.finanse.repository.MetaRepository;
import br.edu.ifs.finanse.repository.TipoMetaRepository;
import br.edu.ifs.finanse.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes da movimentação de metas (aporte/retirada) — funcionalidade
 * adicionada por cima do RF13, permitindo usar parte do dinheiro guardado
 * (ex.: reserva de emergência sendo efetivamente usada) sem excluir a meta.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetaService — aporte e retirada")
class MetaServiceTest {

    @Mock
    private MetaRepository metaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoMetaRepository tipoMetaRepository;

    private MetaService service;

    @BeforeEach
    void setUp() {
        service = new MetaService(metaRepository, usuarioRepository, tipoMetaRepository);
    }

    private Meta metaComValores(BigDecimal valorAlvo, BigDecimal valorAtual, Meta.StatusMeta status) {
        return Meta.builder()
                .id(1L)
                .usuario(Usuario.builder().id(1L).build())
                .tipoMeta(TipoMeta.builder().id(1L).descricao("Reserva de emergência").build())
                .nome("Minha reserva")
                .valorAlvo(valorAlvo)
                .valorAtual(valorAtual)
                .dataCriacao(LocalDate.now())
                .status(status)
                .build();
    }

    @Test
    @DisplayName("aporte soma ao valor atual da meta")
    void aporte_somaAoValorAtual() {
        Meta meta = metaComValores(new BigDecimal("5000"), new BigDecimal("1000"), Meta.StatusMeta.ATIVA);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaDTO.Response resultado = service.registrarMovimentacao(1L, new BigDecimal("500"), Meta.TipoMovimentacao.APORTE);

        assertThat(resultado.valorAtual()).isEqualByComparingTo("1500");
        assertThat(resultado.status()).isEqualTo(Meta.StatusMeta.ATIVA);
    }

    @Test
    @DisplayName("aporte que atinge o valor alvo marca a meta como CONCLUIDA")
    void aporte_atingindoValorAlvo_marcaConcluida() {
        Meta meta = metaComValores(new BigDecimal("5000"), new BigDecimal("4800"), Meta.StatusMeta.ATIVA);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaDTO.Response resultado = service.registrarMovimentacao(1L, new BigDecimal("200"), Meta.TipoMovimentacao.APORTE);

        assertThat(resultado.valorAtual()).isEqualByComparingTo("5000");
        assertThat(resultado.status()).isEqualTo(Meta.StatusMeta.CONCLUIDA);
    }

    @Test
    @DisplayName("retirada subtrai do valor atual da meta")
    void retirada_subtraiDoValorAtual() {
        Meta meta = metaComValores(new BigDecimal("5000"), new BigDecimal("3200"), Meta.StatusMeta.ATIVA);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaDTO.Response resultado = service.registrarMovimentacao(1L, new BigDecimal("1200"), Meta.TipoMovimentacao.RETIRADA);

        assertThat(resultado.valorAtual()).isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("retirada que esvazia uma meta concluída volta o status para ATIVA")
    void retirada_deMetaConcluida_voltaParaAtiva() {
        Meta meta = metaComValores(new BigDecimal("5000"), new BigDecimal("5000"), Meta.StatusMeta.CONCLUIDA);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaDTO.Response resultado = service.registrarMovimentacao(1L, new BigDecimal("1000"), Meta.TipoMovimentacao.RETIRADA);

        assertThat(resultado.valorAtual()).isEqualByComparingTo("4000");
        assertThat(resultado.status()).isEqualTo(Meta.StatusMeta.ATIVA);
    }

    @Test
    @DisplayName("retirada maior que o valor guardado é rejeitada")
    void retirada_maiorQueValorAtual_lancaExcecao() {
        Meta meta = metaComValores(new BigDecimal("5000"), new BigDecimal("300"), Meta.StatusMeta.ATIVA);
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        assertThatThrownBy(() -> service.registrarMovimentacao(1L, new BigDecimal("500"), Meta.TipoMovimentacao.RETIRADA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que o valor atual");
    }

    @Test
    @DisplayName("valor zero ou negativo é rejeitado, tanto para aporte quanto retirada")
    void valorNaoPositivo_lancaExcecao() {
        assertThatThrownBy(() -> service.registrarMovimentacao(1L, BigDecimal.ZERO, Meta.TipoMovimentacao.APORTE))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.registrarMovimentacao(1L, new BigDecimal("-10"), Meta.TipoMovimentacao.RETIRADA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

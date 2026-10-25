package br.edu.ifs.finanse.controller;

import br.edu.ifs.finanse.dto.TransacaoDTO;
import br.edu.ifs.finanse.entity.Transacao;
import br.edu.ifs.finanse.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    /**
     * RF14 — Visualizar histórico de transações, com filtros opcionais por
     * período, categoria e tipo (receita/despesa).
     */
    @GetMapping
    public List<TransacaoDTO.Response> listarPorUsuario(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Transacao.TipoTransacao tipo
    ) {
        return transacaoService.listarPorUsuario(usuarioId, dataInicio, dataFim, categoriaId, tipo);
    }

    @GetMapping("/{id}")
    public TransacaoDTO.Response buscarPorId(@PathVariable Long id) {
        return transacaoService.buscarPorId(id);
    }

    /** RF08 — Registrar receita/despesa. Atualiza automaticamente o uso do rotativo (RF16) quando via cartão. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoDTO.Response criar(@Valid @RequestBody TransacaoDTO.Request dto) {
        return transacaoService.criar(dto);
    }

    /** Edita uma transação — usado principalmente para confirmar/corrigir a categoria sugerida pelo RF10. */
    @PutMapping("/{id}")
    public TransacaoDTO.Response editar(@PathVariable Long id, @Valid @RequestBody TransacaoDTO.Request dto) {
        return transacaoService.editar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        transacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

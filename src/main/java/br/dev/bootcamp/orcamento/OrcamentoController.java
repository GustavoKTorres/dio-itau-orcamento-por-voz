package br.dev.bootcamp.orcamento;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/lancamentos")
class OrcamentoController {
    private final Orcamento orcamento;

    OrcamentoController(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    @PostMapping("/propostas")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> propor(@RequestBody Lancamento lancamento) {
        return Map.of("id", orcamento.propor(lancamento));
    }

    @PostMapping("/propostas/{id}/confirmar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void confirmar(@PathVariable UUID id) {
        if (!orcamento.confirmar(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposta ausente ou já confirmada.");
        }
    }

    @GetMapping
    List<Lancamento> listar(@RequestParam String categoria) {
        return orcamento.listar(categoria);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> entradaInvalida(IllegalArgumentException erro) {
        return Map.of("erro", erro.getMessage());
    }
}

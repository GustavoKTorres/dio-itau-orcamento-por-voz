package br.dev.bootcamp.orcamento;

import java.math.BigDecimal;
import java.util.Locale;

public record Lancamento(String descricao, String categoria, BigDecimal valor) {
    public Lancamento {
        if (descricao == null || descricao.isBlank() || descricao.strip().length() > 120) {
            throw new IllegalArgumentException("Informe uma descrição com até 120 caracteres.");
        }
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2
                || valor.compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new IllegalArgumentException("Informe um valor positivo com até duas casas decimais.");
        }
        descricao = descricao.strip();
        categoria = normalizarCategoria(categoria);
        valor = valor.setScale(2);
    }

    static String normalizarCategoria(String categoria) {
        if (categoria == null || !categoria.strip().matches("[\\p{L}][\\p{L} -]{0,39}")) {
            throw new IllegalArgumentException("Informe uma categoria válida com até 40 caracteres.");
        }
        return categoria.strip().toLowerCase(Locale.ROOT);
    }
}

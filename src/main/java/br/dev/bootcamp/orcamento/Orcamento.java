package br.dev.bootcamp.orcamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class Orcamento {
    private final JdbcClient banco;

    Orcamento(JdbcClient banco) {
        this.banco = banco;
    }

    UUID propor(Lancamento lancamento) {
        UUID id = UUID.randomUUID();
        banco.sql("""
                INSERT INTO lancamento (id, descricao, categoria, valor, confirmado)
                VALUES (:id, :descricao, :categoria, :valor, false)
                """)
                .param("id", id)
                .param("descricao", lancamento.descricao())
                .param("categoria", lancamento.categoria())
                .param("valor", lancamento.valor())
                .update();
        return id;
    }

    @Transactional
    boolean confirmar(UUID id) {
        return banco.sql("UPDATE lancamento SET confirmado = true WHERE id = :id AND confirmado = false")
                .param("id", id)
                .update() == 1;
    }

    List<Lancamento> listar(String categoria) {
        return banco.sql("""
                SELECT descricao, categoria, valor FROM lancamento
                WHERE confirmado = true AND categoria = :categoria
                ORDER BY criado_em DESC LIMIT 100
                """)
                .param("categoria", Lancamento.normalizarCategoria(categoria))
                .query((rs, row) -> new Lancamento(
                        rs.getString("descricao"), rs.getString("categoria"), rs.getBigDecimal("valor")))
                .list();
    }

    BigDecimal total(String categoria) {
        return banco.sql("""
                SELECT COALESCE(SUM(valor), 0) FROM lancamento
                WHERE confirmado = true AND categoria = :categoria
                """)
                .param("categoria", Lancamento.normalizarCategoria(categoria))
                .query(BigDecimal.class)
                .single();
    }
}

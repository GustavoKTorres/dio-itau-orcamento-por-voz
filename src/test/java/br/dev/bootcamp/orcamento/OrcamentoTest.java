package br.dev.bootcamp.orcamento;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:orcamento;DB_CLOSE_DELAY=-1")
class OrcamentoTest {
    @Autowired
    Orcamento orcamento;

    @Test
    void propostaSoEntraNoOrcamentoUmaVezAposConfirmacao() {
        var id = orcamento.propor(new Lancamento("Almoço", "Alimentação", new BigDecimal("24.50")));
        assertThat(orcamento.listar("alimentação")).isEmpty();
        assertThat(orcamento.total("alimentação")).isEqualByComparingTo("0.00");

        assertThat(orcamento.confirmar(id)).isTrue();
        assertThat(orcamento.confirmar(id)).isFalse();
        assertThat(orcamento.listar("ALIMENTAÇÃO")).hasSize(1);
        assertThat(orcamento.total("alimentação")).isEqualByComparingTo("24.50");
        assertThat(orcamento.listar("transporte")).isEmpty();
    }

    @Test
    void rejeitaValorSemPrecisaoMonetariaOuNaoPositivo() {
        assertThatThrownBy(() -> new Lancamento("Ônibus", "Transporte", new BigDecimal("1.234")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lancamento("Ônibus", "Transporte", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

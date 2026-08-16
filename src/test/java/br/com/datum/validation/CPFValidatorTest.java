package br.com.datum.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o algoritmo Módulo 11 isoladamente (sem contexto Spring, sem
 * banco, sem rede).
 */
class CPFValidatorTest {

    private final CPFValidator validator = new CPFValidator();

    @ParameterizedTest
    @ValueSource(strings = {"11604567805", "111.444.777-35", "52998224725"})
    void cpfValido_retornaTrue(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "00000000000",      // todos os dígitos iguais
            "111.111.111-11",   // todos os dígitos iguais, com máscara
            "12345678900",      // dígitos verificadores errados
            "123",               // tamanho inválido
            "123456789012"       // tamanho inválido (12 dígitos)
    })
    void cpfInvalido_retornaFalse(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }
}

package br.com.datum.serializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusConverterTest {

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "active", " Active "})
    void active_convertePraTrue(String value) {
        assertThat(StatusConverter.toBoolean(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE", "inactive", " Inactive "})
    void inactive_convertePraFalse(String value) {
        assertThat(StatusConverter.toBoolean(value)).isFalse();
    }

    @Test
    void valorInvalido_lancaIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> StatusConverter.toBoolean("FOO"))
                .withMessageContaining("FOO");
    }

    @Test
    void valorNulo_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> StatusConverter.toBoolean(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

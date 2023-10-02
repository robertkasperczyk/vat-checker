package rk.vatchecker.api.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rk.vatchecker.api.InvalidVatFormatException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VatOrderValidatorUnitTest {

    private final VatOrderValidator validator = new VatOrderValidator();

    @ParameterizedTest
    @ValueSource(strings = {"PL12", "DE123456789101"})
    public void validatesValid(String vatNumber) {
        assertThatCode(() -> validator.validate(vatNumber)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"P12", "DES123456789101", "PL1", "DE1234567891011"})
    public void validatesInvalid(String vatNumber) {
        assertThatThrownBy(() -> validator.validate(vatNumber))
                .isInstanceOf(InvalidVatFormatException.class)
                .hasMessage("Vat number must be specified in the following format: [A-Z]{2}[0-9A-Za-z+*.]{2,12}");
    }

    @Test
    public void validatesNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidVatFormatException.class)
                .hasMessage("Vat number is required");
    }

}
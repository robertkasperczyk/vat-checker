package rk.vatchecker.api.validation;

import org.springframework.stereotype.Service;
import rk.vatchecker.api.InvalidVatFormatException;

import java.util.regex.Pattern;

@Service
public class VatOrderValidator {

    private static final Pattern VAT_FORMAT = Pattern.compile("[A-Z]{2}[0-9A-Za-z+*.]{2,12}");

    public void validate(String vatNumber) {
        if (vatNumber == null) {
            throw new InvalidVatFormatException("Vat number is required");
        }
        if (!VAT_FORMAT.matcher(vatNumber).matches()) {
            throw new InvalidVatFormatException("Vat number must be specified in the following format: " + VAT_FORMAT);
        }
    }

}

package rk.vatchecker.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class VatCheckOrderNotFoundException extends RuntimeException {

    public VatCheckOrderNotFoundException(long orderId) {
        super("Unable to find vat check order with id: " + orderId);
    }
}

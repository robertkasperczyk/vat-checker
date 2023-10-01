package rk.vatchecker.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidVatFormatException extends RuntimeException {

    public InvalidVatFormatException(String message) {
        super(message);
    }
}

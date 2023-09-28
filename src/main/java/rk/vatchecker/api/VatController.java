package rk.vatchecker.api;

import org.springframework.web.bind.annotation.*;

@RestController("/vat")
public class VatController {

    @GetMapping("/check/{vatNumber}")
    public VatResponse checkVat(@PathVariable("vatNumber") String vatNumber) {
        return new VatResponse(true);
    }

}

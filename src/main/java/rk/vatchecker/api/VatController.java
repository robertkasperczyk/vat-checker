package rk.vatchecker.api;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rk.vatchecker.VatService;

@RestController("/vat-check")
@AllArgsConstructor
public class VatController {

    private final VatService vatService;
    private final VatOrderValidator validator;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<GetVatOrderResponse> getOrder(@PathVariable("orderId") long orderId) {
        OrderWithResult orderWithResult = vatService.getOrder(orderId);
        return ResponseEntity.ok(new GetVatOrderResponse(
                orderWithResult.order().status(),
                orderWithResult.result()));
    }

    @PostMapping("/order/{vatNumber}")
    public ResponseEntity<CreateVatOrderResponse> checkVat(@PathVariable("vatNumber") String vatNumber) {
        validator.validate(vatNumber);
        long orderId = vatService.createOrder(vatNumber);
        return ResponseEntity.ok(new CreateVatOrderResponse(orderId));
    }

}

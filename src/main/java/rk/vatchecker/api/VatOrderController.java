package rk.vatchecker.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rk.vatchecker.api.validation.VatOrderValidator;
import rk.vatchecker.vatorder.OrderWithResult;
import rk.vatchecker.vatorder.VatOrderService;

@RestController
@AllArgsConstructor
public class VatOrderController {

    private final VatOrderService vatOrderService;
    private final VatOrderValidator validator;

    @GetMapping("/vat-check/order/{orderId}")
    public ResponseEntity<GetVatOrderResponse> getOrder(@PathVariable("orderId") long orderId) {
        OrderWithResult orderWithResult = vatOrderService.getOrder(orderId);
        return ResponseEntity.ok(new GetVatOrderResponse(
                orderWithResult.order().status(),
                orderWithResult.result()));
    }

    @PostMapping("/vat-check/order")
    public ResponseEntity<CreateVatOrderResponse> checkVat(@RequestParam("vatNumber") String vatNumber) {
        validator.validate(vatNumber);
        long orderId = vatOrderService.createOrder(vatNumber);
        return ResponseEntity.status(HttpStatus.ACCEPTED.value())
                .body(new CreateVatOrderResponse(orderId));
    }
}

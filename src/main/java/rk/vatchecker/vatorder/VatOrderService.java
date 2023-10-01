package rk.vatchecker.vatorder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rk.vatchecker.api.VatCheckOrderNotFoundException;
import rk.vatchecker.db.*;
import rk.vatchecker.vies.ViesCaller;

import java.util.Optional;

@Service
@AllArgsConstructor
public class VatOrderService {

    private final VatOrderRepository vatOrderRepository;
    private final ViesCaller viesCaller;
    private final VatCheckOrderFactory vatCheckOrderFactory;

    public long createOrder(String vatNumber) {
        VatCheckOrder toCreate = vatCheckOrderFactory.createInitial(vatNumber);
        long id = vatOrderRepository.createOrder(toCreate);
        viesCaller.add(id, vatNumber);
        return id;
    }

    public OrderWithResult getOrder(long orderId) {
        Optional<VatCheckOrder> maybeOrder = vatOrderRepository.getOrder(orderId);
        VatCheckOrder order = maybeOrder.orElseThrow(() -> new VatCheckOrderNotFoundException(orderId));
        if (order.status() == VatOrderStatus.COMPLETED) {
            Optional<VatOrderResult> maybeResult = vatOrderRepository.getOrderResult(orderId);
            VatOrderResult result = maybeResult.orElseThrow(() -> new IllegalStateException("Result not found for completed order."));
            return new OrderWithResult(order, result);
        } else {
            return new OrderWithResult(order, null);
        }
    }

}

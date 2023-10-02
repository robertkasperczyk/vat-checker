package rk.vatchecker.vatorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rk.vatchecker.api.VatCheckOrderNotFoundException;
import rk.vatchecker.db.*;
import rk.vatchecker.vies.ViesCaller;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VatOrderServiceUnitTest {

    @Mock
    private VatOrderRepository vatOrderRepository;
    @Mock
    private ViesCaller viesCaller;
    @Mock
    private VatCheckOrderFactory vatCheckOrderFactory;

    @InjectMocks
    private VatOrderService service;

    @Test
    public void createsOrder() {
        String vatNumber = "PL121213121";
        VatCheckOrder toCreate = new VatCheckOrder(null, LocalDateTime.parse("2022-01-11T10:12"), VatOrderStatus.SUBMITTED, vatNumber);
        when(vatCheckOrderFactory.createInitial(vatNumber))
                .thenReturn(toCreate);
        long id = 1L;
        when(vatOrderRepository.createOrder(toCreate)).thenReturn(id);

        service.createOrder(vatNumber);

        verify(viesCaller).add(id, vatNumber);
    }

    @Test
    public void getsOrderInProgress() {
        long id = 1L;
        VatCheckOrder order = new VatCheckOrder(1L, LocalDateTime.parse("2022-01-11T10:12"), VatOrderStatus.IN_PROGRESS, "PL121213121");
        when(vatOrderRepository.getOrder(id)).thenReturn(Optional.of(order));

        OrderWithResult actual = service.getOrder(id);

        assertThat(actual).isEqualTo(new OrderWithResult(order, null));
    }

    @Test
    public void getsCompletedOrder() {
        long id = 1L;
        VatCheckOrder order = new VatCheckOrder(1L, LocalDateTime.parse("2022-01-11T10:12"), VatOrderStatus.COMPLETED, "PL121213121");
        when(vatOrderRepository.getOrder(id)).thenReturn(Optional.of(order));
        VatOrderResult result = new VatOrderResult(true, "Some company", "Some address");
        when(vatOrderRepository.getOrderResult(id)).thenReturn(Optional.of(result));

        OrderWithResult actual = service.getOrder(id);

        assertThat(actual).isEqualTo(new OrderWithResult(order, result));
    }

    @Test
    public void getsOrderWithWrongId() {
        long id = 1L;
        when(vatOrderRepository.getOrder(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(id))
                .isInstanceOf(VatCheckOrderNotFoundException.class)
                .hasMessage("Unable to find vat check order with id: 1");
    }

    @Test
    public void getsCompletedOrderWithoutResult() {
        long id = 1L;
        VatCheckOrder order = new VatCheckOrder(1L, LocalDateTime.parse("2022-01-11T10:12"), VatOrderStatus.COMPLETED, "PL121213121");
        when(vatOrderRepository.getOrder(id)).thenReturn(Optional.of(order));
        when(vatOrderRepository.getOrderResult(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Result not found for completed order.");
    }
}
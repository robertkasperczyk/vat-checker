package rk.vatchecker.vatorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rk.vatchecker.db.VatCheckOrder;
import rk.vatchecker.db.VatOrderStatus;
import rk.vatchecker.util.TimeProvider;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VatCheckOrderFactoryUnitTest {

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private VatCheckOrderFactory factory;

    @Test
    void createsInitial() {
        LocalDateTime time = LocalDateTime.parse("2022-01-13T11:33");
        when(timeProvider.now()).thenReturn(time);

        String vatNumber = "PL12311232";
        VatCheckOrder actual = factory.createInitial(vatNumber);

        assertThat(actual).isEqualTo(new VatCheckOrder(null, time, VatOrderStatus.SUBMITTED, vatNumber));
    }
}
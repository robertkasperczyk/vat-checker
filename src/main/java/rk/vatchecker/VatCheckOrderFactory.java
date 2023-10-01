package rk.vatchecker;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rk.vatchecker.db.VatCheckOrder;
import rk.vatchecker.db.VatOrderStatus;
import rk.vatchecker.util.TimeProvider;

@Service
@AllArgsConstructor
public class VatCheckOrderFactory {

    private final TimeProvider time;

    public VatCheckOrder createInitial(String vatNumber) {
        return new VatCheckOrder(null, time.now(), VatOrderStatus.SUBMITTED, vatNumber);
    }

}

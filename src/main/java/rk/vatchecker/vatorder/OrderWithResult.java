package rk.vatchecker.vatorder;

import rk.vatchecker.vatorder.VatOrderResult;
import rk.vatchecker.db.VatCheckOrder;

public record OrderWithResult(VatCheckOrder order, VatOrderResult result) {
}

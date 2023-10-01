package rk.vatchecker.api;

import rk.vatchecker.VatOrderResult;
import rk.vatchecker.db.VatCheckOrder;

public record OrderWithResult(VatCheckOrder order, VatOrderResult result) {
}

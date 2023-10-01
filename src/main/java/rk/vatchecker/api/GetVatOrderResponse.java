package rk.vatchecker.api;

import rk.vatchecker.vatorder.VatOrderResult;
import rk.vatchecker.db.VatOrderStatus;

public record GetVatOrderResponse(VatOrderStatus status, VatOrderResult data) {
}

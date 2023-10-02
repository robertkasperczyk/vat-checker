package rk.vatchecker.api;

import rk.vatchecker.db.VatOrderStatus;
import rk.vatchecker.vatorder.VatOrderResult;

public record GetVatOrderResponse(VatOrderStatus status, VatOrderResult data) {
}

package rk.vatchecker.api;

import rk.vatchecker.VatOrderResult;
import rk.vatchecker.db.VatOrderStatus;

public record GetVatOrderResponse(VatOrderStatus status, VatOrderResult data) {
}

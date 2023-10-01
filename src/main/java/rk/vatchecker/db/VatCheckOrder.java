package rk.vatchecker.db;

import java.time.LocalDateTime;

public record VatCheckOrder(Long id, LocalDateTime submitted, VatOrderStatus status, String vatNumber) {
}

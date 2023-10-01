package rk.vatchecker.db;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import rk.vatchecker.vies.VatRegistryData;
import rk.vatchecker.vatorder.VatOrderResult;

import java.util.List;
import java.util.Optional;

import static rk.vatchecker.db.Tables.VAT_CHECK_RESULT;
import static rk.vatchecker.db.VatOrderStatus.IN_PROGRESS;
import static rk.vatchecker.db.VatOrderStatus.SUBMITTED;
import static rk.vatchecker.db.tables.VatCheckOrder.VAT_CHECK_ORDER;

@Repository
@AllArgsConstructor
public class VatOrderRepository {

    private final DSLContext dsl;

    public long createOrder(VatCheckOrder toCreate) {
        Long id = dsl.insertInto(VAT_CHECK_ORDER)
                .columns(VAT_CHECK_ORDER.SUBMITTED, VAT_CHECK_ORDER.STATUS, VAT_CHECK_ORDER.VAT_NUMBER)
                .values(toCreate.submitted(), toCreate.status().name(), toCreate.vatNumber())
                .returning(VAT_CHECK_ORDER.ID)
                .fetchOne(VAT_CHECK_ORDER.ID);
        if (id == null) {
            throw new RuntimeException("Unable to insert data: " + toCreate);
        }
        return id;
    }

    public void updateStatus(long orderId, VatOrderStatus status) {
        dsl.update(VAT_CHECK_ORDER)
                .set(VAT_CHECK_ORDER.STATUS, status.name())
                .where(VAT_CHECK_ORDER.ID.eq(orderId))
                .execute();
    }

    public void updateStatusAndData(long orderId, VatOrderStatus status, VatRegistryData data) {
        Long resultId = dsl.insertInto(VAT_CHECK_RESULT)
                .columns(VAT_CHECK_RESULT.VALID, VAT_CHECK_RESULT.NAME, VAT_CHECK_RESULT.ADDRESS)
                .values((byte) 1, data.name(), data.address())
                .returning(VAT_CHECK_RESULT.ID)
                .fetchOne(VAT_CHECK_RESULT.ID);
        if (resultId == null) {
            throw new RuntimeException("Unable to insert data: " + data);
        }
        dsl.update(VAT_CHECK_ORDER)
                .set(VAT_CHECK_ORDER.STATUS, status.name())
                .set(VAT_CHECK_ORDER.RESULT_ID, resultId)
                .where(VAT_CHECK_ORDER.ID.eq(orderId))
                .execute();
    }

    public void updateStatusAndData(long orderId, VatOrderStatus status) {
        Long resultId = dsl.insertInto(VAT_CHECK_RESULT)
                .columns(VAT_CHECK_RESULT.VALID, VAT_CHECK_RESULT.NAME, VAT_CHECK_RESULT.ADDRESS)
                .values((byte) 0, null, null)
                .returning(VAT_CHECK_ORDER.ID)
                .fetchOne(VAT_CHECK_ORDER.ID);
        if (resultId == null) {
            throw new RuntimeException("Unable to insert data.");
        }
        dsl.update(VAT_CHECK_ORDER)
                .set(VAT_CHECK_ORDER.STATUS, status.name())
                .set(VAT_CHECK_ORDER.RESULT_ID, resultId)
                .where(VAT_CHECK_ORDER.ID.eq(orderId))
                .execute();
    }

    public Optional<VatCheckOrder> getOrder(long orderId) {
        return dsl.selectFrom(VAT_CHECK_ORDER)
                .where(VAT_CHECK_ORDER.ID.eq(orderId))
                .fetchOptional()
                .map(row -> new VatCheckOrder(row.getId(), row.getSubmitted(), VatOrderStatus.valueOf(row.getStatus()), row.getVatNumber()));
    }

    public Optional<VatOrderResult> getOrderResult(long orderId) {
        return dsl.selectFrom(VAT_CHECK_ORDER.join(VAT_CHECK_RESULT).on(VAT_CHECK_ORDER.RESULT_ID.eq(VAT_CHECK_RESULT.ID)))
                .where(VAT_CHECK_ORDER.ID.eq(orderId))
                .fetchOptional()
                .map(row -> new VatOrderResult(row.get(VAT_CHECK_RESULT.VALID) > 0, row.get(VAT_CHECK_RESULT.NAME), row.get(VAT_CHECK_RESULT.ADDRESS)));
    }

    public List<VatCheckOrder> getNotFinished() {
        return dsl.selectFrom(VAT_CHECK_ORDER)
                .where(VAT_CHECK_ORDER.STATUS.in(List.of(SUBMITTED, IN_PROGRESS)))
                .fetch()
                .map(row -> new VatCheckOrder(row.getId(), row.getSubmitted(), VatOrderStatus.valueOf(row.getStatus()), row.getVatNumber()));
    }

}

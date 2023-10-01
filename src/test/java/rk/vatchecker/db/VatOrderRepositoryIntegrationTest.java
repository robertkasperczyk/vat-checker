package rk.vatchecker.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import rk.vatchecker.util.TestUtils;
import rk.vatchecker.vatorder.VatOrderResult;
import rk.vatchecker.vies.VatRegistryData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static rk.vatchecker.db.VatOrderStatus.COMPLETED;
import static rk.vatchecker.db.VatOrderStatus.SUBMITTED;


@SpringBootTest
class VatOrderRepositoryIntegrationTest {

    @Autowired
    private VatOrderRepository vatOrderRepository;

    @Autowired
    private JdbcTemplate db;

    @BeforeEach
    void clearDatabase() throws IOException {
        TestUtils.extractSqlStatements("clear_database.sql").forEach(db::execute);
    }

    @Test
    public void createsOrder() {
        long id = vatOrderRepository.createOrder(new VatCheckOrder(null, LocalDateTime.parse("2022-02-11T11:33"), SUBMITTED, "PL1231212"));

        List<Map<String, Object>> actual = db.queryForList("SELECT id, submitted, vat_number, status FROM vat_check_order");
        assertThat(actual)
                .containsExactly(Map.ofEntries(
                        entry("id", id),
                        entry("submitted", LocalDateTime.parse("2022-02-11T11:33")),
                        entry("vat_number", "PL1231212"),
                        entry("status", "SUBMITTED")));
    }

    @Test
    public void updatesStatus() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED')
                """);

        vatOrderRepository.updateStatus(11L, COMPLETED);

        List<Map<String, Object>> actual = db.queryForList("SELECT id, status FROM vat_check_order");
        assertThat(actual)
                .containsExactly(
                        Map.ofEntries(entry("id", 10L), entry("status", "IN_PROGRESS")),
                        Map.ofEntries(entry("id", 11L), entry("status", "COMPLETED")));
    }

    @Test
    public void updatesStatusAndDataWhenDataEmpty() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED')
                """);

        vatOrderRepository.updateStatusAndData(11L, COMPLETED);

        List<Map<String, Object>> actual = db.queryForList("SELECT id, status FROM vat_check_order");
        assertThat(actual)
                .containsExactly(
                        Map.ofEntries(entry("id", 10L), entry("status", "IN_PROGRESS")),
                        Map.ofEntries(entry("id", 11L), entry("status", "COMPLETED")));

        actual = db.queryForList("SELECT valid FROM vat_check_result");
        assertThat(actual)
                .containsExactly(
                        Map.ofEntries(entry("valid", false)));
    }

    @Test
    public void updatesStatusAndData() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED')
                """);

        vatOrderRepository.updateStatusAndData(11L, COMPLETED, new VatRegistryData("Some name", "Some address"));

        List<Map<String, Object>> actual = db.queryForList("SELECT id, status FROM vat_check_order");
        assertThat(actual)
                .containsExactly(
                        Map.ofEntries(entry("id", 10L), entry("status", "IN_PROGRESS")),
                        Map.ofEntries(entry("id", 11L), entry("status", "COMPLETED")));

        actual = db.queryForList("SELECT valid, name, address FROM vat_check_result");
        assertThat(actual)
                .containsExactly(
                        Map.ofEntries(entry("valid", true), entry("name", "Some name"), entry("address", "Some address")));
    }

    @Test
    public void getsOrder() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED'),
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED')
                """);

        Optional<VatCheckOrder> actual = vatOrderRepository.getOrder(11);

        assertThat(actual).contains(new VatCheckOrder(11L, LocalDateTime.parse("2022-02-11T11:33"), SUBMITTED, "vat_nr2"));
    }

    @Test
    public void getsNotExistingOrder() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED'),
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED')
                """);

        Optional<VatCheckOrder> actual = vatOrderRepository.getOrder(13);

        assertThat(actual).isEmpty();
    }

    @Test
    public void getsOrderResult() {
        db.execute("""
                INSERT INTO vat_check_result(id, valid, name, address) VALUE
                    (1, true, 'Some company', 'Some address');
                """);
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status, result_id) 
                VALUE
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED', 1)
                """);

        Optional<VatOrderResult> actual = vatOrderRepository.getOrderResult(12);

        assertThat(actual).contains(new VatOrderResult(true, "Some company", "Some address"));
    }

    @Test
    public void getsOrderResultForNotExistingOrder() {
        db.execute("""
                INSERT INTO vat_check_result(id, valid, name, address) VALUE
                    (1, true, 'Some company', 'Some address');
                """);
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status, result_id) VALUE
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED', 1);
                """);

        Optional<VatOrderResult> actual = vatOrderRepository.getOrderResult(13);

        assertThat(actual).isEmpty();
    }

    @Test
    public void getsOrderResultForNotExistingOrderResult() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status, result_id) 
                VALUE
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED', null);
                """);
        db.execute("""                     
                INSERT INTO vat_check_result(id, valid, name, address) VALUE
                    (1, true, 'Some company', 'Some address');
                """);

        Optional<VatOrderResult> actual = vatOrderRepository.getOrderResult(12);

        assertThat(actual).isEmpty();
    }

    @Test
    public void getsNotFinished() {
        db.execute("""
                INSERT INTO vat_check_order (id, submitted, vat_number, status) 
                VALUES 
                    (10, '2022-02-11 11:33', 'vat_nr1', 'IN_PROGRESS'),
                    (11, '2022-02-11 11:33', 'vat_nr2', 'SUBMITTED'),
                    (12, '2022-02-11 11:33', 'vat_nr2', 'COMPLETED')
                """);

        List<VatCheckOrder> actual = vatOrderRepository.getNotFinished();

        assertThat(actual).extracting(VatCheckOrder::id).containsOnly(10L, 11L);
    }

}
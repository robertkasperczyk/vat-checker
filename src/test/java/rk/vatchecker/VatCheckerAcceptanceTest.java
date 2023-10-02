package rk.vatchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import rk.vatchecker.api.CreateVatOrderResponse;
import rk.vatchecker.api.GetVatOrderResponse;
import rk.vatchecker.db.VatOrderStatus;
import rk.vatchecker.util.TestUtils;
import rk.vatchecker.vatorder.VatOrderResult;
import rk.vatchecker.vies.*;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VatCheckerAcceptanceTest {

    @MockBean
    private ViesFacade vies;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private JdbcTemplate db;

    @BeforeEach
    void clearDatabase() throws IOException {
        TestUtils.extractSqlStatements("clear_database.sql").forEach(db::execute);
    }

    @Test
    public void vatCheckFlowWhenNumberIsCorrect() throws InterruptedException {
        when(vies.checkVat(new VatNumber("PL", "1231232")))
                .thenReturn(Optional.of(new VatRegistryData("Some company", "Some address")));

        ResponseEntity<CreateVatOrderResponse> response1 = rest.postForEntity(
                "http://localhost:%s/vat-check/order?vatNumber=PL1231232".formatted(port),
                null,
                CreateVatOrderResponse.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response1.getBody()).isNotNull();

        Thread.sleep(2 * 1000);

        long orderId = response1.getBody().id();
        ResponseEntity<GetVatOrderResponse> response2 = rest.getForEntity(
                "http://localhost:%s/vat-check/order/".formatted(port) + orderId,
                GetVatOrderResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody())
                .isEqualTo(new GetVatOrderResponse(
                        VatOrderStatus.COMPLETED,
                        new VatOrderResult(true, "Some company", "Some address")));
    }

    @Test
    public void vatCheckFlowWhenNumberIsNotCorrect() throws InterruptedException {
        when(vies.checkVat(new VatNumber("PL", "1231232")))
                .thenReturn(Optional.empty());

        ResponseEntity<CreateVatOrderResponse> response1 = rest.postForEntity(
                "http://localhost:%s/vat-check/order?vatNumber=PL1231232".formatted(port),
                null,
                CreateVatOrderResponse.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response1.getBody()).isNotNull();

        Thread.sleep(2 * 1000);

        long orderId = response1.getBody().id();
        ResponseEntity<GetVatOrderResponse> response2 = rest.getForEntity(
                "http://localhost:%s/vat-check/order/".formatted(port) + orderId,
                GetVatOrderResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody())
                .isEqualTo(new GetVatOrderResponse(
                        VatOrderStatus.COMPLETED,
                        new VatOrderResult(false, null, null)));
    }

    @Test
    public void vatCheckFlowWhenProblemsWithVies() throws InterruptedException {
        when(vies.checkVat(new VatNumber("PL", "1231232")))
                .thenThrow(new ViesException("vies error", new RuntimeException()))
                .thenThrow(new ViesException("vies error", new RuntimeException()))
                .thenThrow(new ViesException("vies error", new RuntimeException()))
                .thenReturn(Optional.of(new VatRegistryData("Some company", "Some address")));

        ResponseEntity<CreateVatOrderResponse> response1 = rest.postForEntity(
                "http://localhost:%s/vat-check/order?vatNumber=PL1231232".formatted(port),
                null,
                CreateVatOrderResponse.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response1.getBody()).isNotNull();

        Thread.sleep(2 * 1000);

        long orderId = response1.getBody().id();
        ResponseEntity<GetVatOrderResponse> response2 = rest.getForEntity(
                "http://localhost:%s/vat-check/order/".formatted(port) + orderId,
                GetVatOrderResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody())
                .isEqualTo(new GetVatOrderResponse(
                        VatOrderStatus.IN_PROGRESS,
                        null));

        Thread.sleep(8 * 1000);

        ResponseEntity<GetVatOrderResponse> response3 = rest.getForEntity(
                "http://localhost:%s/vat-check/order/".formatted(port) + orderId,
                GetVatOrderResponse.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody())
                .isEqualTo(new GetVatOrderResponse(
                        VatOrderStatus.COMPLETED,
                        new VatOrderResult(true, "Some company", "Some address")));
    }

}

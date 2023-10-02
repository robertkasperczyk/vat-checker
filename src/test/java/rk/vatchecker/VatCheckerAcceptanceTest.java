package rk.vatchecker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import rk.vatchecker.api.CreateVatOrderResponse;
import rk.vatchecker.vies.ViesFacade;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VatCheckerAcceptanceTest {

    @MockBean
    private ViesFacade vies;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void vatCheckFlow() {
        ResponseEntity<CreateVatOrderResponse> response = rest.postForEntity(
                "http://localhost:%s/vat-check/order?vatNumber=PL1231232".formatted(port),
                null,
                CreateVatOrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

}

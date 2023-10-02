package rk.vatchecker.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import rk.vatchecker.api.validation.VatOrderValidator;
import rk.vatchecker.db.VatCheckOrder;
import rk.vatchecker.db.VatOrderStatus;
import rk.vatchecker.vatorder.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest
@Import(VatOrderController.class)
class VatOrderControllerIntegrationTest {

    @MockBean
    private VatOrderService service;
    @MockBean
    private VatOrderValidator validator;

    @Autowired
    private MockMvc mvc;

    @Test
    public void createsOrder() throws Exception {
        String vatNumber = "PL123121";
        when(service.createOrder(vatNumber)).thenReturn(1L);

        MvcResult result = mvc.perform(post("/vat-check/order").param("vatNumber", vatNumber)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"id\":1}");
    }

    @Test
    public void createsWithInvalidVat() throws Exception {
        String vatNumber = "PL123121";
        doThrow(new InvalidVatFormatException("Invalid vat number")).when(validator).validate(vatNumber);

        MvcResult result = mvc.perform(post("/vat-check/order").param("vatNumber", vatNumber)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Invalid vat number");
    }

    @Test
    public void getsOrder() throws Exception {
        when(service.getOrder(1L)).thenReturn(new OrderWithResult(
                new VatCheckOrder(1L, LocalDateTime.parse("2022-02-01T11:12"), VatOrderStatus.IN_PROGRESS, "PL123121"),
                null));

        MvcResult result = mvc.perform(get("/vat-check/order/1")).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo("{\"status\":\"IN_PROGRESS\",\"data\":null}");
    }

    @Test
    public void getsCompletedOrder() throws Exception {
        when(service.getOrder(1L)).thenReturn(new OrderWithResult(
                new VatCheckOrder(1L, LocalDateTime.parse("2022-02-01T11:12"), VatOrderStatus.COMPLETED, "PL123121"),
                new VatOrderResult(true, "Some company", "Some address")));

        MvcResult result = mvc.perform(get("/vat-check/order/1")).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo("{\"status\":\"COMPLETED\",\"data\":{\"valid\":true,\"name\":\"Some company\",\"address\":\"Some address\"}}");
    }

    @Test
    public void getsNotExistingOrder() throws Exception {
        doThrow(new VatCheckOrderNotFoundException(1)).when(service).getOrder(1);

        MvcResult result = mvc.perform(get("/vat-check/order/1")).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Unable to find vat check order with id: 1");
    }

}
package rk.vatchecker;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import org.junit.jupiter.api.Test;
import rk.vatchecker.api.ViesException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ViesFacadeIntegrationTest {

    @Test
    public void checks() throws ViesException {
        VatNumber vatNumber = new VatNumber("PL", "6762609180");

        Optional<VatData> actual = new ViesFacade(new CheckVatService()).checkVat(vatNumber);

        assertThat(actual)
                .contains(new VatData(
                        "ROBERT KASPERCZYK",
                        "KRAKÓW \nDR. JANA PILTZA 19 M45\n30-392 "));
    }

}
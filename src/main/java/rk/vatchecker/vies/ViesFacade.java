package rk.vatchecker.vies;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatPortType;
import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.soap.SOAPFaultException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import javax.xml.datatype.XMLGregorianCalendar;

@Service
@AllArgsConstructor
public class ViesFacade {

    private final CheckVatService vies;

    public Optional<VatRegistryData> checkVat(VatNumber vatNumber) throws ViesException {
        Holder<String> country = new Holder<>(vatNumber.countryCode());
        Holder<String> number = new Holder<>(vatNumber.number());
        Holder<XMLGregorianCalendar> date = new Holder<>();
        Holder<Boolean> valid = new Holder<>();
        Holder<String> name = new Holder<>();
        Holder<String> address = new Holder<>();

        try {
            CheckVatPortType cv = vies.getCheckVatPort();
            cv.checkVat(country, number, date, valid, name, address);
        } catch (SOAPFaultException ex) {
            SOAPFault fault = ex.getFault();
            String faultKey = fault.getFaultString();
            throw new ViesException("Cannot connect to VIES. Fault: %s.".formatted(faultKey), ex);
        } catch (Exception ex) {
            throw new ViesException("Unable to connect to VIES.", ex);
        }

        if (valid.value) {
            return Optional.of(new VatRegistryData(name.value, address.value));
        } else {
            return Optional.empty();
        }
    }

}

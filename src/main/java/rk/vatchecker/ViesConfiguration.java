package rk.vatchecker;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViesConfiguration {

    @Bean
    public CheckVatService vies() {
        return new CheckVatService();
    }

}

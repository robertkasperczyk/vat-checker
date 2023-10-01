package rk.vatchecker.vies;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ViesConfiguration {

    @Bean
    public CheckVatService vies() {
        return new CheckVatService();
    }

    @Bean
    public ExecutorService executors() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public Retry retryConfig() {
        RetryConfig retryConfig = RetryConfig.custom()
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2))
                .build();
        return Retry.of("vies", retryConfig);
    }

}

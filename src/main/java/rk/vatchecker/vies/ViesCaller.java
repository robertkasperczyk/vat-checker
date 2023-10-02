package rk.vatchecker.vies;

import io.github.resilience4j.retry.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rk.vatchecker.StatsLogger;
import rk.vatchecker.db.VatOrderRepository;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static rk.vatchecker.db.VatOrderStatus.COMPLETED;
import static rk.vatchecker.db.VatOrderStatus.IN_PROGRESS;

@Slf4j
@Service
@AllArgsConstructor
public class ViesCaller {

    private final ViesFacade vies;
    private final ExecutorService executors;
    private final VatOrderRepository repository;
    private final Retry retryConfig;
    private final StatsLogger statsLogger;

    public void init() {
        repository.getNotFinished().forEach(order -> add(order.id(), order.vatNumber()));
    }

    public void add(long orderId, String vatNumber) {
        Function<VatNumber, Optional<VatRegistryData>> viesCall = Retry.decorateFunction(retryConfig, vies::checkVat);
        executors.execute(() -> {
            repository.updateStatus(orderId, IN_PROGRESS);
            statsLogger.checkInProgress();
            Optional<VatRegistryData> vatData = viesCall.apply(new VatNumber(vatNumber));
            statsLogger.checkFinished();
            vatData.ifPresentOrElse(
                    data -> repository.updateStatusAndData(orderId, COMPLETED, data),
                    () -> repository.updateStatusAndData(orderId, COMPLETED));
        });
    }

}

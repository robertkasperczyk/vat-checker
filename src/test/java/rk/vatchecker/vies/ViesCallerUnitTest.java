package rk.vatchecker.vies;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rk.vatchecker.db.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViesCallerUnitTest {

    @Mock
    private ViesFacade vies;
    @Mock
    private ExecutorService executors;
    @Mock
    private VatOrderRepository repository;

    @Captor
    private ArgumentCaptor<Runnable> captor;

    private ViesCaller viesCaller;

    @BeforeEach
    void setUp() {
        RetryConfig retryConfig = RetryConfig.custom()
                .intervalFunction(IntervalFunction.ofDefaults())
                .build();
        viesCaller = new ViesCaller(vies, executors, repository, Retry.of("vies", retryConfig));
    }

    @Test
    public void initializes() {
        LocalDateTime time = LocalDateTime.parse("2020-01-11T11:33");
        VatCheckOrder order1 = new VatCheckOrder(1L, time, VatOrderStatus.IN_PROGRESS, "PL1231223123");
        VatCheckOrder order2 = new VatCheckOrder(2L, time.plusHours(3), VatOrderStatus.SUBMITTED, "PL1231223124");
        when(repository.getNotFinished()).thenReturn(List.of(order1, order2));
        when(vies.checkVat(new VatNumber(order1.vatNumber()))).thenReturn(Optional.empty());
        VatRegistryData order2data = new VatRegistryData("Company", "Some Address");
        when(vies.checkVat(new VatNumber(order2.vatNumber()))).thenReturn(Optional.of(order2data));

        viesCaller.init();

        verify(executors, times(2)).execute(captor.capture());
        List<Runnable> runs = captor.getAllValues();
        assertThat(runs).hasSize(2);

        runs.get(0).run();
        verify(repository).updateStatus(1L, VatOrderStatus.IN_PROGRESS);
        verify(vies).checkVat(new VatNumber(order1.vatNumber()));
        verify(repository).updateStatusAndData(1L, VatOrderStatus.COMPLETED);

        runs.get(1).run();
        verify(repository).updateStatus(2L, VatOrderStatus.IN_PROGRESS);
        verify(vies).checkVat(new VatNumber(order1.vatNumber()));
        verify(repository).updateStatusAndData(2L, VatOrderStatus.COMPLETED, order2data);
    }

    @Test
    public void initializesWhenNoOrdersInProgress() {
        when(repository.getNotFinished()).thenReturn(List.of());

        viesCaller.init();

        verifyNoInteractions(executors);
    }

    @Test
    public void addsValidNumberForExecution() {
        String vatNumber = "PL1231223123";
        VatRegistryData data = new VatRegistryData("Company", "Some Address");
        when(vies.checkVat(new VatNumber(vatNumber))).thenReturn(Optional.of(data));

        viesCaller.add(1L, vatNumber);

        verify(executors, times(1)).execute(captor.capture());
        List<Runnable> runs = captor.getAllValues();
        assertThat(runs).hasSize(1);
        runs.get(0).run();
        verify(repository).updateStatus(1L, VatOrderStatus.IN_PROGRESS);
        verify(vies).checkVat(new VatNumber(vatNumber));
        verify(repository).updateStatusAndData(1L, VatOrderStatus.COMPLETED, data);
    }

    @Test
    public void addsInvalidNumberForExecution() {
        String vatNumber = "PL1231223123";
        when(vies.checkVat(new VatNumber(vatNumber))).thenReturn(Optional.empty());

        viesCaller.add(1L, vatNumber);

        verify(executors, times(1)).execute(captor.capture());
        List<Runnable> runs = captor.getAllValues();
        assertThat(runs).hasSize(1);
        runs.get(0).run();
        verify(repository).updateStatus(1L, VatOrderStatus.IN_PROGRESS);
        verify(vies).checkVat(new VatNumber(vatNumber));
        verify(repository).updateStatusAndData(1L, VatOrderStatus.COMPLETED);
    }

}
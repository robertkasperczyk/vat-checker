package rk.vatchecker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class StatsLogger {

    private final AtomicInteger checksInProgress = new AtomicInteger(0);
    private final AtomicInteger checksCompletedToday = new AtomicInteger(0);

    public synchronized void checkInProgress() {
        checksInProgress.incrementAndGet();
    }

    public synchronized void checkFinished() {
        checksInProgress.decrementAndGet();
        checksCompletedToday.incrementAndGet();
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    public synchronized void logChecksInProgress() {
        log.info("Currently, there is %d VAT checks in progress".formatted(checksInProgress.get()));
    }

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public synchronized void logDailyChecks() {
        log.info("%d VAT checks has been completed yesterday".formatted(checksCompletedToday.get()));
        checksCompletedToday.set(0);
    }


}

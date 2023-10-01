package rk.vatchecker;

import lombok.AllArgsConstructor;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import rk.vatchecker.vies.ViesCaller;

@AllArgsConstructor
@Service
public class ApplicationInitializer {

    private final ViesCaller viesCaller;

    @EventListener
    public void appStarted(ContextStartedEvent event) {
        viesCaller.init();
    }

}

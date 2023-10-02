package rk.vatchecker;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import rk.vatchecker.vies.ViesCaller;

import java.util.Objects;

@AllArgsConstructor
@Service
public class ApplicationInitializer {

    private final ViesCaller viesCaller;
    private final ApplicationContext context;

    @EventListener
    public void appStarted(ContextRefreshedEvent event) {
        if (Objects.equals(context.getId(), event.getApplicationContext().getId())) {
            viesCaller.init(); // execute only once
        }
    }

}

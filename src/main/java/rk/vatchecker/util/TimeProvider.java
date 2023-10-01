package rk.vatchecker.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeProvider {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}

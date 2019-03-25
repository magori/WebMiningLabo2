package ch.hesso;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeWatcher {
    private Instant start = Instant.now();
    private String pattern = "mm:ss.SSS";
    private Instant end;

    public static TimeWatcher start() {
        return new TimeWatcher();
    }

    public TimeWatcher end() {
        this.end = Instant.now();
        return this;
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public String format() {
        return this.format(this.pattern);
    }

    public String format(String pattern) {
        if (this.end == null) {
            this.end();
        }
        LocalTime time = LocalTime.MIDNIGHT.plus(this.duration());
        return DateTimeFormatter.ofPattern(pattern).format(time);
    }
}
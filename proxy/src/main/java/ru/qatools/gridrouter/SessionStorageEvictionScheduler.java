package ru.qatools.gridrouter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.qatools.gridrouter.sessions.StatsCounter;

import java.time.Duration;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Configuration
@EnableScheduling
public class SessionStorageEvictionScheduler {

    @Value("${grid.router.evict.sessions.timeout.seconds}")
    private int timeout;

    @Autowired
    private StatsCounter statsCounter;

    @Scheduled(cron = "${grid.router.evict.sessions.cron}")
    public void expireOldSessions() {
        statsCounter.expireSessionsOlderThan(Duration.ofSeconds(timeout));
    }
}

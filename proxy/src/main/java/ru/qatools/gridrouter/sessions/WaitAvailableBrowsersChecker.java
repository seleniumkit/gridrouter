package ru.qatools.gridrouter.sessions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.qatools.gridrouter.config.Version;

import java.time.Duration;
import java.time.temporal.Temporal;

import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Ilya Sadykov
 */
public class WaitAvailableBrowsersChecker implements AvailableBrowsersChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitAvailableBrowsersChecker.class);

    @Value("${grid.router.queue.interval.seconds}")
    protected int queueWaitInterval;

    @Autowired
    protected StatsCounter statsCounter;

    @Value("${grid.router.queue.timeout.seconds}")
    protected int queueTimeout;

    public WaitAvailableBrowsersChecker() {
    }

    public WaitAvailableBrowsersChecker(int queueTimeout, int queueWaitInterval, StatsCounter statsCounter) {
        this.queueTimeout = queueTimeout;
        this.queueWaitInterval = queueWaitInterval;
        this.statsCounter = statsCounter;
    }

    @Override
    public void ensureFreeBrowsersAvailable(String user, String remoteHost, String browser, Version version) {
        int waitAttempt = 0;
        final String requestId = randomUUID().toString();
        final Temporal waitingStarted = now();
        final Duration maxWait = Duration.ofSeconds(queueTimeout);
        while (maxWait.compareTo(Duration.between(waitingStarted, now())) > 0 &&
                (countSessions(user, browser, version)) >= version.getPermittedCount()) {
            try {
                onWait(user, browser, version, requestId, waitAttempt);
                Thread.sleep(SECONDS.toMillis(queueWaitInterval));
            } catch (InterruptedException e) {
                LOGGER.error("Failed to sleep thread", e);
            }
            if (maxWait.compareTo(Duration.between(waitingStarted, now())) < 0) {
                onWaitTimeout(user, browser, version, requestId, waitAttempt);
            }
        }
        onWaitFinished(user, browser, version, requestId, waitAttempt);
    }

    protected void onWaitTimeout(String user, String browser, Version version, String requestId, int waitAttempt) {
        throw new WaitAvailableBrowserTimeoutException(
                format("Waiting for available browser of %s %s timed out for %s after %s attempts",
                        browser, version.getNumber(), user, waitAttempt));
    }

    protected void onWait(String user, String browser, Version version, String requestId, int waitAttempt) {
        LOGGER.info("[SESSION_WAIT_AVAILABLE_BROWSER] [{}] [{}] [{}] [{}] [{}]",
                user, browser, version.getNumber(), version.getPermittedCount(), ++waitAttempt);
    }

    protected void onWaitFinished(String user, String browser, Version version, String requestId, int waitAttempt) {
        LOGGER.info("[SESSION_WAIT_FINISHED] [{}] [{}] [{}] [{}] [{}]",
                user, browser, version.getNumber(), version.getPermittedCount(), ++waitAttempt);
    }

    protected int countSessions(String user, String browser, Version actualVersion) {
        return statsCounter.getSessionsCountForUserAndBrowser(user, browser, actualVersion.getNumber());
    }
}

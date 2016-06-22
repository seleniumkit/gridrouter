package ru.qatools.gridrouter.sessions;

import org.junit.Before;
import org.junit.Test;
import ru.qatools.gridrouter.config.Version;

import java.time.Duration;
import java.time.temporal.Temporal;

import static java.time.ZonedDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov
 */
public class WaitAvailableBrowsersCheckerTest {
    WaitAvailableBrowsersChecker checker;
    Version version;
    StatsCounter counter;

    @Before
    public void setUp() throws Exception {
        counter = mock(StatsCounter.class);
        checker = new WaitAvailableBrowsersChecker(3, 1, counter);
        version = new Version();
        version.setPermittedCount(10);
        version.setNumber("33");
        when(counter.getSessionsCountForUserAndBrowser(eq("user"), eq("firefox"), eq("33"))).thenReturn(10);
    }

    @Test
    public void testWaitAvailableBrowsersChecker() throws Exception {
        Temporal started = now();
        try {
            checker.ensureFreeBrowsersAvailable("user", "host", "firefox", version);
        } catch (WaitAvailableBrowserTimeoutException e) {
            // do nothing
        }
        verify(counter, times(3)).getSessionsCountForUserAndBrowser(eq("user"), eq("firefox"), eq("33"));
        assertThat(Duration.between(started, now()).toMillis(), greaterThanOrEqualTo(3000L));
    }

    @Test(expected = WaitAvailableBrowserTimeoutException.class)
    public void testWaitAvailableBrowsersTimeout() throws Exception {
        checker.ensureFreeBrowsersAvailable("user", "host", "firefox", version);
    }

    @Test
    public void testNoWaitAvailableBrowser() throws Exception {
        when(counter.getSessionsCountForUserAndBrowser(eq("user"), eq("firefox"), eq("33"))).thenReturn(5);

        Temporal started = now();
        checker.ensureFreeBrowsersAvailable("user", "host", "firefox", version);
        verify(counter, times(1)).getSessionsCountForUserAndBrowser(eq("user"), eq("firefox"), eq("33"));
        assertThat(Duration.between(started, now()).toMillis(), lessThan(1000L));
        verifyNoMoreInteractions(counter);
    }
}
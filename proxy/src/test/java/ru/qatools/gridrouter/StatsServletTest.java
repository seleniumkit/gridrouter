package ru.qatools.gridrouter;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.sessions.BrowsersCountMap;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.*;
import static ru.qatools.gridrouter.utils.HttpUtils.executeSimpleGet;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class StatsServletTest {

    @Rule
    public GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule(8081);

    @Test
    public void testStats() throws IOException {
        assertThat(getActual(USER_1), is(empty()));

        hub.emulate().newSessions(1);
        hub.emulate().quit();

        WebDriver driver = new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), firefox());
        assertThat(getActual(USER_1), is(newCountMap("firefox", "32.0")));

        driver.quit();
        assertThat(getActual(USER_1), is(empty()));
    }

    @Test
    public void testStatsForDifferentUsers() throws IOException {
        hub.emulate().newSessions(1);
        new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), firefox());
        assertThat(getActual(USER_1), is(newCountMap("firefox", "32.0")));
        assertThat(getActual(USER_2), is(empty()));
    }

    @Test
    public void testEvictionOfOldSession() throws Exception {
        hub.emulate().newSessions(1);
        new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), firefox());
        Thread.sleep(1000);
        assertThat(getActual(USER_1), is(newCountMap("firefox", "32.0")));
        Thread.sleep(6000);
        assertThat(getActual(USER_1), is(empty()));
    }

    @Test
    public void testActiveSessionIsNotEvicted() throws Exception {
        hub.emulate().newSessions(1).navigation();
        WebDriver driver = new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), firefox());
        for (int i = 0; i < 3; i++) {
            Thread.sleep(2000);
            driver.getCurrentUrl();
            driver.get("http://yandex.ru");
        }
        assertThat(getActual(USER_1), is(newCountMap("firefox", "32.0")));
    }

    private BrowsersCountMap getActual(String user) throws IOException {
        return executeSimpleGet(gridRouter.baseUrl(user) + "/stats", BrowsersCountMap.class);
    }

    private BrowsersCountMap newCountMap(String browser, String version) {
        BrowsersCountMap expected = new BrowsersCountMap();
        expected.increment(browser, version);
        return expected;
    }

    private BrowsersCountMap empty() {
        return new BrowsersCountMap();
    }
}

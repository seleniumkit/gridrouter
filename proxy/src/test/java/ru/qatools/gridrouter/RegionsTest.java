package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.HUB_PORT;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_1;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_3;
import static ru.qatools.gridrouter.utils.GridRouterRule.baseUrl;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class RegionsTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub1 = new HubEmulatorRule(HUB_PORT);

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule(HUB_PORT + 1);

    @Rule
    public HubEmulatorRule hub3 = new HubEmulatorRule(HUB_PORT + 2);

    @Test
    public void testRegionIsChangedAfterFailedTry() {
        hub3.emulate().newSessions(1);
        new RemoteWebDriver(hubUrl(baseUrl(USER_3)), firefox());
        hub1.verify().newSessionRequestsCountIs(1);
        hub2.verify().newSessionRequestsCountIs(0);
        hub3.verify().newSessionRequestsCountIs(1);
    }

    @Test
    public void testAllHostsAreTriedExactlyOnceInTheEnd() {
        getWebDriverSafe(USER_3);
        hub1.verify().newSessionRequestsCountIs(1);
        hub2.verify().newSessionRequestsCountIs(1);
        hub3.verify().newSessionRequestsCountIs(1);
    }

    @Test
    public void testConfigIsImmutableBetweenRequests() {
        // note here user1 is used for simplicity
        getWebDriverSafe(USER_1);
        hub1.verify().newSessionRequestsCountIs(1);
        getWebDriverSafe(USER_1);
        hub1.verify().newSessionRequestsCountIs(2);
    }

    private static void getWebDriverSafe(String user) {
        try {
            new RemoteWebDriver(hubUrl(baseUrl(user)), firefox());
        } catch (WebDriverException ignored) {
        }
    }
}

package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.*;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class RegionsTest {

    @ClassRule
    public static GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub1 = new HubEmulatorRule( 8081);

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule( 8082);

    @Rule
    public HubEmulatorRule hub3 = new HubEmulatorRule( 8083);

    @Test
    public void testRegionIsChangedAfterFailedTry() {
        hub3.emulate().newSessions(1);
        new RemoteWebDriver(hubUrl(gridRouter.baseUrl(USER_3)), firefox());
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
            new RemoteWebDriver(hubUrl(gridRouter.baseUrl(user)), firefox());
        } catch (WebDriverException ignored) {
        }
    }
}

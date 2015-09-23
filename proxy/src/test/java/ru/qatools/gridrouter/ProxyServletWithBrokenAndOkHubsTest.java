package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_2;
import static ru.qatools.gridrouter.utils.GridRouterRule.baseUrl;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithBrokenAndOkHubsTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub1 = new HubEmulatorRule(GridRouterRule.HUB_PORT);

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule(GridRouterRule.HUB_PORT + 1);

    {
        hub1.emulate().newSessionFailures(1);
        hub2.emulate().newSessions(1);
    }

    @Test
    public void testFailingHubIsSkipped() {
        new RemoteWebDriver(hubUrl(baseUrl(USER_2)), firefox());
        hub1.verify().totalRequestsCountIs(1);
        hub1.verify().totalRequestsCountIs(1);
    }
}

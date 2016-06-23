package ru.qatools.gridrouter;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_2;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithBrokenAndOkHubsTest {

    @Rule
    public GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub1 = new HubEmulatorRule(8081, hub -> hub.emulate().newSessionFailures(1));

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule(8082, hub -> hub.emulate().newSessions(1));

    @Test
    public void testFailingHubIsSkipped() {
        new RemoteWebDriver(hubUrl(gridRouter.baseUrl(USER_2)), firefox());
        hub1.verify().totalRequestsCountIs(1);
        hub1.verify().totalRequestsCountIs(1);
    }
}

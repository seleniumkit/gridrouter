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
import static ru.qatools.gridrouter.utils.GridRouterRule.BASE_URL_WITH_AUTH;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithBrokenHubTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule(GridRouterRule.HUB_PORT);

    {
        hub.emulate().newSessionFailures(1);
    }

    @Test(expected = WebDriverException.class)
    public void testFailingHubIsSkipped() {
        new RemoteWebDriver(GridRouterRule.hubUrl(BASE_URL_WITH_AUTH), firefox());
        hub.verify().totalRequestsCountIs(1);
    }
}

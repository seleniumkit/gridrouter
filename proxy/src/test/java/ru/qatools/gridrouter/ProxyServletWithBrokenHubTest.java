package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithBrokenHubTest {

    @ClassRule
    public static GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule( 8081, hub -> hub.emulate().newSessionFailures(1));

    @Test(expected = WebDriverException.class)
    public void testFailingHubIsSkipped() {
        new RemoteWebDriver(GridRouterRule.hubUrl(gridRouter.baseUrlWithAuth), firefox());
        hub.verify().totalRequestsCountIs(1);
    }
}

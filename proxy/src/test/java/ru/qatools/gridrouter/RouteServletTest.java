package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_3;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

public class RouteServletTest {

    @ClassRule
    public static GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule( 8081);

    @Test(expected = WebDriverException.class, timeout = 10 * 1000)
    public void testRouteTimeout() {
            hub.emulate().newSessionFreeze(30);
            new RemoteWebDriver(hubUrl(gridRouter.baseUrl(USER_3)), firefox());
    }

}
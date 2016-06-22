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
import static ru.qatools.gridrouter.utils.GridRouterRule.*;

public class RouteServletTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule(HUB_PORT);

    @Test(expected = WebDriverException.class, timeout = 10 * 1000)
    public void testRouteTimeout() {
            hub.emulate().newSessionFreeze(30);
            new RemoteWebDriver(hubUrl(baseUrl(USER_3)), firefox());
    }

}
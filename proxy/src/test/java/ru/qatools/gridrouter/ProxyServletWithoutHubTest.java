package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.BASE_URL_WITH_AUTH;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithoutHubTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Test(expected = WebDriverException.class)
    public void testProxyWithProperAuth() {
        new RemoteWebDriver(GridRouterRule.hubUrl(BASE_URL_WITH_AUTH), firefox());
    }
}

package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;

import static org.openqa.selenium.remote.DesiredCapabilities.firefox;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithoutHubTest {

    @ClassRule
    public static GridRouterRule gridRouterRule = new GridRouterRule();

    @Test(expected = WebDriverException.class)
    public void testProxyWithProperAuth() {
        new RemoteWebDriver(GridRouterRule.hubUrl(gridRouterRule.baseUrlWithAuth), firefox());
    }
}

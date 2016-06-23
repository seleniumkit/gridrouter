package ru.qatools.gridrouter;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;

import static org.openqa.selenium.remote.DesiredCapabilities.chrome;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletExceptionsWithoutHubTest {

    @Rule
    public GridRouterRule gridRouter = new GridRouterRule();

    @Test(expected = UnsupportedCommandException.class)
    public void testProxyWithWrongAuth() {
        new RemoteWebDriver(hubUrl(gridRouter.baseUrlWrongPassword), firefox());
    }

    @Test(expected = UnsupportedCommandException.class)
    public void testProxyWithoutAuth() {
        new RemoteWebDriver(hubUrl(gridRouter.baseUrl), firefox());
    }

    @Test(expected = WebDriverException.class)
    public void testProxyWithNotSupportedBrowser() {
        new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), chrome());
    }

    @Test(expected = WebDriverException.class)
    public void testProxyWithNotSupportedVersion() {
        DesiredCapabilities caps = firefox();
        caps.setVersion("1");
        new RemoteWebDriver(hubUrl(gridRouter.baseUrlWithAuth), caps);
    }
}

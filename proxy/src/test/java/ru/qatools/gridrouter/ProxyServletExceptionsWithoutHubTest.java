package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;

import static org.openqa.selenium.remote.DesiredCapabilities.chrome;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;
import static ru.qatools.gridrouter.utils.GridRouterRule.*;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletExceptionsWithoutHubTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Test(expected = UnsupportedCommandException.class)
    public void testProxyWithWrongAuth() {
        new RemoteWebDriver(hubUrl(BASE_URL_WITH_WRONG_PASSWORD), firefox());
    }

    @Test(expected = UnsupportedCommandException.class)
    public void testProxyWithoutAuth() {
        new RemoteWebDriver(hubUrl(BASE_URL), firefox());
    }

    @Test(expected = WebDriverException.class)
    public void testProxyWithNotSupportedBrowser() {
        new RemoteWebDriver(hubUrl(BASE_URL_WITH_AUTH), chrome());
    }

    @Test(expected = WebDriverException.class)
    public void testProxyWithNotSupportedVersion() {
        DesiredCapabilities caps = firefox();
        caps.setVersion("1");
        new RemoteWebDriver(hubUrl(BASE_URL_WITH_AUTH), caps);
    }
}

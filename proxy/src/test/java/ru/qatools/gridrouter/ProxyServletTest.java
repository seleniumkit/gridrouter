package ru.qatools.gridrouter;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import ru.qatools.gridrouter.utils.GridRouterRule;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.openqa.selenium.Platform.ANY;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.baseUrl;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public abstract class ProxyServletTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    private final URL url;

    public ProxyServletTest(String user) {
        url = GridRouterRule.hubUrl(baseUrl(user));
    }

    protected final URL getUrl() {
        return url;
    }

    @Test
    public void testSpecifyingBrowserVersion() {
        DesiredCapabilities caps = firefox();
        caps.setVersion("32");
        new RemoteWebDriver(getUrl(), caps);
    }

    @Test
    public void testSessionIdDoesNotChange() {
        RemoteWebDriver driver = new RemoteWebDriver(getUrl(), firefox());
        String sessionId = driver.getSessionId().toString();
        driver.get("some url");
        assertThat(driver.getSessionId().toString(), is(equalTo(sessionId)));
        driver.getCurrentUrl();
        assertThat(driver.getSessionId().toString(), is(equalTo(sessionId)));
    }

    @Test
    public void testSessionIdChangesForANewBrowser() {
        RemoteWebDriver driver1 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId1 = driver1.getSessionId().toString();
        RemoteWebDriver driver2 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId2 = driver2.getSessionId().toString();
        assertThat(sessionId1, is(not(equalTo(sessionId2))));
    }

    @Test
    public void testQuit() {
        new RemoteWebDriver(getUrl(), firefox()).quit();
    }

    @Test
    public void testSendRequestParams() {
        RemoteWebDriver driver = new RemoteWebDriver(getUrl(), firefox());
        String url = "some url";
        driver.get(url);
        assertThat(driver.getCurrentUrl(), is(url));
    }

    @Test
    public void testFindElement() {
        RemoteWebDriver driver = new RemoteWebDriver(getUrl(), firefox());
        String selector = "//lol[foo='bar']";
        WebElement element = driver.findElement(By.xpath(selector));
        assertThat(
                ((RemoteWebElement) element).getId(),
                is(String.valueOf(selector.hashCode()))
        );
    }

    @Test
    public void testNullVersion() throws Exception {
        String browserName = "other";
        try {
            new RemoteWebDriver(getUrl(), new DesiredCapabilities(browserName, null, ANY));
        } catch (WebDriverException e) {
            assertThat(e.getMessage(),
                    startsWith("Cannot find " + browserName + " capabilities on any available node"));
        }
    }
}

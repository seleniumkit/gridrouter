package ru.qatools.gridrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.BASE_URL_WITH_AUTH;
import static ru.qatools.gridrouter.utils.GridRouterRule.HUB_PORT;
import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public class StatsServletTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule(HUB_PORT);

    @Test
    public void testStats() throws IOException {
        int actual = executeSimpleGet(BASE_URL_WITH_AUTH + "/stats");
        assertThat(actual, notNullValue());
        assertThat(actual, is(0));

        hub.emulate().newSessions(1);
        hub.emulate().quit();

        WebDriver driver = new RemoteWebDriver(hubUrl(BASE_URL_WITH_AUTH), firefox());

        actual = executeSimpleGet(BASE_URL_WITH_AUTH + "/stats");
        assertThat(actual, is(1));

        driver.quit();

        actual = executeSimpleGet(BASE_URL_WITH_AUTH + "/stats");
        assertThat(actual, is(0));
    }

    public static int executeSimpleGet(String url) throws IOException {
        CloseableHttpResponse execute = HttpClientBuilder
                .create().build()
                .execute(new HttpGet(url));
        InputStream content = execute.getEntity().getContent();
        return new ObjectMapper().readValue(content, Integer.class);
    }
}

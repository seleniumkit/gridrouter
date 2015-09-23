package ru.qatools.gridrouter;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import ru.qatools.gridrouter.utils.GridRouterRule;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.qatools.gridrouter.utils.GridRouterRule.BASE_URL_WITH_AUTH;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class PingServletTest {

    @ClassRule
    public static TestRule START_GRID_ROUTER = new GridRouterRule();

    @Test
    public void testPingWithAuth() throws IOException {
        assertThat(executeSimpleGet(BASE_URL_WITH_AUTH + "/ping"), equalTo(SC_OK));
    }

    public static int executeSimpleGet(String url) throws IOException {
        return HttpClientBuilder
                .create().build()
                .execute(new HttpGet(url))
                .getStatusLine()
                .getStatusCode();
    }
}

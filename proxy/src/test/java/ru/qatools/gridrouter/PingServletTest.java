package ru.qatools.gridrouter;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import ru.qatools.gridrouter.utils.GridRouterRule;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class PingServletTest {

    @Rule
    public GridRouterRule gridRouter = new GridRouterRule();

    @Test
    public void testPingWithAuth() throws IOException {
        assertThat(executeSimpleGet(gridRouter.baseUrlWithAuth + "/ping"), equalTo(SC_OK));
    }

    public static int executeSimpleGet(String url) throws IOException {
        return HttpClientBuilder
                .create().build()
                .execute(new HttpGet(url))
                .getStatusLine()
                .getStatusCode();
    }
}

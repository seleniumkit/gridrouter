package ru.qatools.gridrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ru.qatools.gridrouter.utils.GridRouterRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static ru.qatools.gridrouter.utils.GridRouterRule.*;

/**
 * TODO add test for user with different browsers and different versions
 * @author Dmitry Baev charlie@yandex-team.ru
 */
@RunWith(Parameterized.class)
public class QuotaServletTest {

    @ClassRule
    public static GridRouterRule gridRouter = new GridRouterRule();

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {USER_1, 1}, {USER_2, 4}, {USER_3, 8},
        });
    }

    private final String user;
    private final int browsersCount;

    public QuotaServletTest(String user, int browsersCount) {
        this.user = user;
        this.browsersCount = browsersCount;
    }

    @Test
    public void testQuota() throws IOException {
        Map<String, Integer> quota = executeSimpleGet(gridRouter.baseUrl(user) + "/quota");
        assertThat(quota.size(), is(1));
        assertThat(quota.get("firefox:32.0"), is(browsersCount));
    }

    public static Map<String, Integer> executeSimpleGet(String url) throws IOException {
        CloseableHttpResponse execute = HttpClientBuilder
                .create().build()
                .execute(new HttpGet(url));
        InputStream content = execute.getEntity().getContent();
        //noinspection unchecked
        return new ObjectMapper().readValue(content, HashMap.class);

    }
}

package ru.qatools.gridrouter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.HUB_PORT;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_1;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_4;
import static ru.qatools.gridrouter.utils.MatcherUtils.canObtain;
import static ru.qatools.gridrouter.utils.QuotaUtils.copyQuotaFile;
import static ru.qatools.gridrouter.utils.QuotaUtils.deleteQuotaFile;
import static ru.qatools.gridrouter.utils.QuotaUtils.replacePortInQuotaFile;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.timeoutHasExpired;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class QuotaReloadTest {

    private static final int HUB_PORT_2 = HUB_PORT + 1;

    @Rule
    public TestRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule(HUB_PORT_2) {{
        emulate().newSessions(1);
    }};

    @Test
    public void testQuotaIsReloadedOnFileChange() throws Exception {
        replacePortInQuotaFile(USER_1, HUB_PORT_2);
        Thread.sleep(5000); // just to avoid multiple exceptions in the logs
        assertThat(USER_1, should(canObtain(firefox()))
                .whileWaitingUntil(timeoutHasExpired().withPollingInterval(SECONDS.toMillis(3))));
    }

    @Test
    public void testNewQuotaFileIsLoaded() throws Exception {
        copyQuotaFile(USER_1, USER_4, HUB_PORT_2);
        Thread.sleep(5000); // just to avoid multiple exceptions in the logs
        assertThat(USER_4, should(canObtain(firefox()))
                .whileWaitingUntil(timeoutHasExpired().withPollingInterval(SECONDS.toMillis(3))));
    }

    @After
    public void tearDown() {
        hub2.verify().newSessionRequestsCountIs(1);
        hub2.verify().totalRequestsCountIs(1);
    }

    @AfterClass
    public static void restoreQuotaFiles() {
        replacePortInQuotaFile(USER_1, HUB_PORT);
        deleteQuotaFile(USER_4);
    }
}

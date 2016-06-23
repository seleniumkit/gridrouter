package ru.qatools.gridrouter;

import org.junit.*;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_1;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_4;
import static ru.qatools.gridrouter.utils.MatcherUtils.canObtain;
import static ru.qatools.gridrouter.utils.QuotaUtils.*;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.timeoutHasExpired;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Ignore
public class QuotaReloadTest {

    public static final int HUB_PORT_2 = 8082;
    @Rule
    public GridRouterRule gridRouter = new GridRouterRule();

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule( HUB_PORT_2, hub -> hub.emulate().newSessions(1));

    @Test
    public void testQuotaIsReloadedOnFileChange() throws Exception {
        replacePortInQuotaFile(USER_1, hub2.getPort());
        assertThat(USER_1, should(canObtain(gridRouter, firefox()))
                .whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(60))
                        .withPollingInterval(SECONDS.toMillis(3))));
    }

    @Test
    public void testNewQuotaFileIsLoaded() throws Exception {
        copyQuotaFile(USER_1, USER_4, 0, 0, hub2.getPort());
        assertThat(USER_4, should(canObtain(gridRouter, firefox()))
                .whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(60))
                        .withPollingInterval(SECONDS.toMillis(3))));
    }

    @After
    public void tearDown() {
        hub2.verify().newSessionRequestsCountIs(1);
        hub2.verify().totalRequestsCountIs(1);
    }

    @AfterClass
    public static void restoreQuotaFiles() throws Exception {
        replacePortInQuotaFile(USER_1, 8081);
        deleteQuotaFile(USER_4);
    }
}

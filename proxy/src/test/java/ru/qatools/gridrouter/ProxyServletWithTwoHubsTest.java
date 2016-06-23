package ru.qatools.gridrouter;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_2;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithTwoHubsTest extends ProxyServletTest {

    @Rule
    public HubEmulatorRule hub1 = new HubEmulatorRule( 8081, hub -> hub.emulate().newSessions(1));

    @Rule
    public HubEmulatorRule hub2 = new HubEmulatorRule( 8082, hub -> hub.emulate().newSessions(1));

    public ProxyServletWithTwoHubsTest() throws Exception {
        super(USER_2);
    }

    @Test
    public void testSessionIdsHaveNoCommonPrefix() {
        RemoteWebDriver driver1 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId1 = driver1.getSessionId().toString();
        RemoteWebDriver driver2 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId2 = driver2.getSessionId().toString();
        assertThat("sessionIds should not have the same prefix",
                !sessionId1.regionMatches(0, sessionId2, 0, 30));

        hub1.verify().totalRequestsCountIs(1);
        hub2.verify().totalRequestsCountIs(1);
    }

    @Override
    public void testSpecifyingBrowserVersion() {
        super.testSpecifyingBrowserVersion();
    }

    @Override
    public void testSessionIdDoesNotChange() {
        hub1.emulate().navigation();
        hub2.emulate().navigation();
        super.testSessionIdDoesNotChange();
    }

    @Test
    @Override
    public void testSessionIdChangesForANewBrowser() {
        super.testSessionIdChangesForANewBrowser();
        hub1.verify().totalRequestsCountIs(1);
        hub2.verify().totalRequestsCountIs(1);
    }

    @Override
    public void testQuit() {
        hub1.emulate().quit();
        hub2.emulate().quit();
        super.testQuit();
    }

    @Override
    public void testSendRequestParams() {
        hub1.emulate().navigation();
        hub2.emulate().navigation();
        super.testSendRequestParams();
    }

    @Test
    @Override
    public void testFindElement() {
        hub1.emulate().navigation().findElement();
        hub2.emulate().navigation().findElement();
        super.testFindElement();
    }
}

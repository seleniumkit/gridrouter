package ru.qatools.gridrouter;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_1;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletWithOneHubTest extends ProxyServletTest {

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule( 8081,
            hub -> hub.emulate().newSessions(1)
    );

    public ProxyServletWithOneHubTest() throws Exception {
        super(USER_1);
    }

    @Test
    public void testSessionIdsHaveACommonPrefix() {
        hub.emulate().newSessions(1);

        RemoteWebDriver driver1 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId1 = driver1.getSessionId().toString();
        RemoteWebDriver driver2 = new RemoteWebDriver(getUrl(), firefox());
        String sessionId2 = driver2.getSessionId().toString();
        assertThat("sessionIds should have the same prefix",
                sessionId1.regionMatches(0, sessionId2, 0, 30));

        hub.verify().totalRequestsCountIs(2);
    }

    @Test
    @Override
    public void testSpecifyingBrowserVersion() {
        super.testSpecifyingBrowserVersion();
        hub.verify().totalRequestsCountIs(1);
    }

    @Test
    @Override
    public void testSessionIdDoesNotChange() {
        hub.emulate().navigation();
        super.testSessionIdDoesNotChange();
        hub.verify().totalRequestsCountIs(4);
    }

    @Test
    @Override
    public void testSessionIdChangesForANewBrowser() {
        hub.emulate().newSessions(1);
        super.testSessionIdChangesForANewBrowser();
        hub.verify().totalRequestsCountIs(2);
    }

    @Test
    @Override
    public void testQuit() {
        hub.emulate().quit();
        super.testQuit();
        hub.verify().newSessionRequestsCountIs(1)
                .quitRequestsCountIs(1);
    }

    @Override
    public void testSendRequestParams() {
        hub.emulate().navigation();
        super.testSendRequestParams();
        hub.verify().totalRequestsCountIs(4);
    }

    @Override
    public void testFindElement() {
        hub.emulate().navigation().findElement();
        super.testFindElement();
        hub.verify().totalRequestsCountIs(3);
    }
}

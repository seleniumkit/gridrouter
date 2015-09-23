package ru.qatools.gridrouter;

import org.junit.AfterClass;
import org.junit.ClassRule;
import ru.qatools.gridrouter.utils.GridRouterRule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletExceptionsWithHubTest extends ProxyServletExceptionsWithoutHubTest {

    @ClassRule
    public static HubEmulatorRule hub = new HubEmulatorRule(GridRouterRule.HUB_PORT);

    @AfterClass
    public static void tearDown() {
        hub.verify().totalRequestsCountIs(0);
    }
}

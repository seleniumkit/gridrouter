package ru.qatools.gridrouter;

import org.junit.After;
import org.junit.Rule;
import ru.qatools.gridrouter.utils.HubEmulatorRule;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class ProxyServletExceptionsWithHubTest extends ProxyServletExceptionsWithoutHubTest {

    @Rule
    public HubEmulatorRule hub = new HubEmulatorRule( 8081);

    @After
    public void tearDown() {
        hub.verify().totalRequestsCountIs(0);
    }
}

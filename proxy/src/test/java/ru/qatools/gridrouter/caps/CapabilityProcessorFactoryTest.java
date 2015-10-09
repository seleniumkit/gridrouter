package ru.qatools.gridrouter.caps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.qatools.gridrouter.json.JsonCapabilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static org.openqa.selenium.remote.DesiredCapabilities.internetExplorer;
import static ru.qatools.gridrouter.utils.jsonUtils.buildJsonCapabilities;
import static org.hamcrest.Matchers.*;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/application-context.xml")
public class CapabilityProcessorFactoryTest {

    @Autowired
    private CapabilityProcessorFactory factory;

    @Test
    public void testGetIEProcessor() throws Exception {
        JsonCapabilities ieCaps = buildJsonCapabilities(internetExplorer());
        assertThat(factory.getProcessor(ieCaps), is(instanceOf(IECapabilityProcessor.class)));
    }

    @Test
    public void testGetDummyProcessor() throws Exception {
        JsonCapabilities firefoxCaps = buildJsonCapabilities(firefox());
        assertThat(factory.getProcessor(firefoxCaps), is(instanceOf(DummyCapabilityProcessor.class)));
    }
}

package ru.qatools.gridrouter.caps;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.utils.JsonUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AppiumCapabilityProcessorTest {

    private CapabilityProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new AppiumCapabilityProcessor();
    }

    @Test
    public void accept() throws Exception {
        assertThat(processor.accept(createCapabilities("", "iOS")), is(true));
        assertThat(processor.accept(createCapabilities("blabla", "iOS")), is(false));
        assertThat(processor.accept(createCapabilities("", "bla")), is(false));
        assertThat(processor.accept(createCapabilities("bla", "iOS")), is(false));
    }

    private JsonCapabilities createCapabilities(String browserName, String platformName) throws IOException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(browserName, "test", Platform.ANY);
        desiredCapabilities.setCapability("platformName", platformName);
        return JsonUtils.buildJsonCapabilities(desiredCapabilities);
    }
    
    @Test
    public void process() throws Exception {
        JsonCapabilities jsonCapabilities = new JsonCapabilities();
        processor.process(jsonCapabilities);
        assertThat(jsonCapabilities.any().keySet(), contains("keepKeyChains"));
        assertThat(jsonCapabilities.any().get("keepKeyChains"), equalTo(true));
    }

}
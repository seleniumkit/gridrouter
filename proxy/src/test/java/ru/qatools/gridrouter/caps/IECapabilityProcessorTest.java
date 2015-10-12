package ru.qatools.gridrouter.caps;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.Proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.Proxy.ProxyType.DIRECT;
import static org.openqa.selenium.remote.BrowserType.IE;
import static org.openqa.selenium.remote.CapabilityType.PROXY;
import static org.openqa.selenium.remote.DesiredCapabilities.firefox;
import static org.openqa.selenium.remote.DesiredCapabilities.internetExplorer;
import static ru.qatools.gridrouter.utils.JsonUtils1.buildJsonCapabilities;
import static ru.qatools.gridrouter.utils.JsonUtils1.buildJsonMessage;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class IECapabilityProcessorTest {

    private IECapabilityProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new IECapabilityProcessor();
    }

    @Test
    public void testAccept() throws Exception {
        assertThat(processor.accept(buildJsonCapabilities(internetExplorer())), is(true));
        assertThat(processor.accept(buildJsonCapabilities(firefox())), is(false));
    }

    @Test
    public void testAddProxy() throws Exception {
        String version = "11";
        JsonCapabilities capabilities = buildJsonCapabilities(internetExplorer(), version);

        processor.process(capabilities);

        assertThat(capabilities.getBrowserName(), is(equalTo(IE)));
        assertThat(capabilities.getVersion(), is(equalTo(version)));
        assertThat(capabilities.any().get(PROXY), is(notNullValue()));
        assertThat(((Proxy) capabilities.any().get(PROXY)).getProxyType(), is(equalTo(DIRECT.name())));
    }

    @Test
    public void testJsonMarshalling() throws Exception {
        JsonMessage message = buildJsonMessage(internetExplorer());
        processor.process(message.getDesiredCapabilities());
        String proxyType = (String) new JSONObject(message.toJson())
                .getJSONObject("desiredCapabilities")
                .getJSONObject("proxy")
                .get("proxyType");
        assertThat(proxyType, is(equalTo(DIRECT.name())));
    }

    @Test
    public void testExistingProxyIsNotOverridden() throws Exception {
        DesiredCapabilities caps = internetExplorer();
        org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
        proxy.setHttpProxy(PROXY);
        caps.setCapability(PROXY, proxy);
        JsonCapabilities capabilities = buildJsonCapabilities(caps);

        processor.process(capabilities);

        assertThat(capabilities.any().get(PROXY), not(instanceOf(Proxy.class)));
    }
}

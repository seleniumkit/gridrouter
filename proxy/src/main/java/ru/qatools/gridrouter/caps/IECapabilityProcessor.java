package ru.qatools.gridrouter.caps;

import org.springframework.stereotype.Service;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.json.Proxy;

/**
 * <p>
 * Sets "ie.ensureCleanSession" and "ie.usePerProcessProxy" for all new
 * internet explorer sessions to ensure clean browser state.
 * </p>
 * <p>
 * Apart from that it sets the "proxy" capability to
 * {@link org.openqa.selenium.Proxy.ProxyType#DIRECT ProxyType.DIRECT}
 * because explorers tend to reuse the proxy from the previous sessions.
 * </p>
 *
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@SuppressWarnings("JavadocReference")
@Service
public class IECapabilityProcessor implements CapabilityProcessor {

    private static final String IE_BROWSER_NAME = "internet explorer";

    @Override
    public boolean accept(JsonCapabilities caps) {
        return caps.getBrowserName().equals(IE_BROWSER_NAME);
    }

    @Override
    public void process(JsonCapabilities caps) {
        caps.any().put("ie.ensureCleanSession", true);
        caps.any().put("ie.usePerProcessProxy", true);
        if (!caps.any().containsKey("proxy")) {
            Proxy proxy = new Proxy();
            proxy.setProxyType("DIRECT");
            caps.any().put("proxy", proxy);
        }
    }
}

package ru.qatools.gridrouter.utils;

import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.JsonMessageFactory;

import java.io.IOException;
import java.util.Map;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.PROXY;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    public static JsonCapabilities buildJsonCapabilities(DesiredCapabilities capabilities)
            throws IOException {
        return buildJsonMessage(capabilities).getDesiredCapabilities();
    }

    public static JsonCapabilities buildJsonCapabilities(DesiredCapabilities capabilities, String version)
            throws IOException {
        capabilities.setVersion(version);
        return buildJsonMessage(capabilities).getDesiredCapabilities();
    }

    public static JsonMessage buildJsonMessage(DesiredCapabilities capabilities) throws IOException {
        JSONObject capabilitiesObject = new JSONObject();
        Map<String, ?> capabilitiesMap = capabilities.asMap();
        capabilitiesMap.keySet().forEach(k -> capabilitiesObject.put(k, capabilitiesMap.get(k)));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("desiredCapabilities", capabilitiesObject);
        return JsonMessageFactory.from(jsonObject.toString());
    }
}

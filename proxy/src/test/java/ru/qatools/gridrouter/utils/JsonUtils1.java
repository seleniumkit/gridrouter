package ru.qatools.gridrouter.utils;

import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.JsonMessageFactory;

import java.io.IOException;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.PROXY;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class JsonUtils1 {

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
        capabilitiesObject.put(BROWSER_NAME, capabilities.getBrowserName());
        capabilitiesObject.put(VERSION, capabilities.getVersion());
        capabilitiesObject.put(PLATFORM, capabilities.getPlatform());
        capabilitiesObject.put(PROXY, capabilities.getCapability(PROXY));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("desiredCapabilities", capabilitiesObject);
        return JsonMessageFactory.from(jsonObject.toString());
    }
}

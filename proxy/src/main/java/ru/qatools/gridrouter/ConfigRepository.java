package ru.qatools.gridrouter;

import ru.qatools.gridrouter.config.Browser;
import ru.qatools.gridrouter.config.Browsers;
import ru.qatools.gridrouter.config.Version;
import ru.qatools.gridrouter.json.JsonCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Sadykov
 */
public interface ConfigRepository {
    Map<String, Browsers> getQuotaMap();

    String getRoute(String routeId);

    default Version findVersion(String user, JsonCapabilities caps) {
        final Browsers browsers = getQuotaMap().get(user);
        return browsers != null ? browsers.find(caps.getBrowserName(), caps.getVersion()) : null;
    }

    default Map<String, Integer> getBrowsersCountMap(String user) {
        HashMap<String, Integer> countMap = new HashMap<>();
        for (Browser browser : getQuotaMap().get(user).getBrowsers()) {
            for (Version version : browser.getVersions()) {
                countMap.put(browser.getName() + ":" + version.getNumber(), version.getCount());
            }
        }
        return countMap;
    }
}

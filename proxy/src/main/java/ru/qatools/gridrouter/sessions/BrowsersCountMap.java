package ru.qatools.gridrouter.sessions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class BrowsersCountMap extends HashMap<String, Map<String, Integer>> implements GridRouterUserStats {

    public void increment(String browser, String version) {
        putIfAbsent(browser, new HashMap<>());
        get(browser).compute(version, (v, count) -> Optional.ofNullable(count).orElse(0) + 1);
    }

    public void decrement(BrowserVersion browser) {
        decrement(browser.getBrowser(), browser.getVersion());
    }

    public void decrement(String browser, String version) {
        if (!containsKey(browser)) {
            return;
        }

        Map<String, Integer> versions = get(browser);
        if (!versions.containsKey(version)) {
            return;
        }

        int count = versions.get(version) - 1;
        if (count > 0) {
            versions.put(version, count);
        } else {
            versions.remove(version);
        }

        if (versions.isEmpty()) {
            remove(browser);
        }
    }
}

package ru.qatools.gridrouter.config;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithBrowserVersionFind {

    List<Browser> getBrowsers();

    default Browser findBrowser(Browser browser) {
        return findBrowser(browser.getName());
    }

    default Browser findBrowser(String name) {
        return getBrowsers().stream()
                .filter(b -> b.getName().equals(name))
                .findFirst().orElse(null);
    }

    default Version find(String browserName, String browserVersion) {
        Browser browser = findBrowser(browserName);

        if (browser == null) {
            return null;
        }

        return isEmpty(browserVersion) ?
               browser.findDefaultVersion() :
               browser.findVersion(browserVersion);
    }
}

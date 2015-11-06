package ru.qatools.gridrouter.config;

import java.util.Iterator;
import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithUpdateBrowsers {

    default void update(Browsers that) {
        for (Iterator<Browser> iterator = this.getBrowsers().iterator(); iterator.hasNext(); ) {
            Browser browser = iterator.next();
            Browser thatBrowser = that.findBrowser(browser);
            if (thatBrowser != null) {
                browser.update(thatBrowser);
            }
            if (thatBrowser == null || browser.getVersions().isEmpty()) {
                iterator.remove();
            }
        }
    }

    List<Browser> getBrowsers();
}

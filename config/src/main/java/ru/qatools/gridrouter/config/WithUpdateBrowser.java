package ru.qatools.gridrouter.config;

import java.util.Iterator;
import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithUpdateBrowser {

    default void update(Browser that) {
        for (Iterator<Version> iterator = this.getVersions().iterator(); iterator.hasNext(); ) {
            Version version = iterator.next();
            Version thatVersion = that.findVersion(version);
            if (thatVersion != null) {
                version.update(thatVersion);
            }
            if (thatVersion == null || version.getRegions().isEmpty()) {
                iterator.remove();
            }
        }
    }

    List<Version> getVersions();
}

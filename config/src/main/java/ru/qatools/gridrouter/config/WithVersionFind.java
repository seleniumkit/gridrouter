package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithVersionFind {

    default Version findDefaultVersion() {
        return findVersion(getDefaultVersion());
    }

    default Version findVersion(Version version) {
        return findVersion(version.getNumber());
    }

    default Version findVersion(String versionPrefix) {
        return getVersions().stream()
                .filter(v -> v.getNumber().startsWith(versionPrefix))
                .findFirst().orElse(null);
    }

    List<Version> getVersions();

    String getDefaultVersion();
}

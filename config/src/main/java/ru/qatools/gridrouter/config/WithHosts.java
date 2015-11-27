package ru.qatools.gridrouter.config;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithHosts {

    default List<Host> getHosts() {
        return getBrowsers().stream()
                .flatMap(b -> b.getVersions().stream())
                .flatMap(v -> v.getRegions().stream())
                .flatMap(r -> r.getHosts().stream())
                .collect(toList());
    }

    List<Browser> getBrowsers();
}

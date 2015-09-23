package ru.qatools.gridrouter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithRoutesMap {

    List<Browser> getBrowsers();

    default Map<String, String> getRoutesMap() {
        HashMap<String, String> routes = new HashMap<>();
        getBrowsers().stream()
                .flatMap(b -> b.getVersions().stream())
                .flatMap(v -> v.getRegions().stream())
                .flatMap(r -> r.getHosts().stream())
                .forEach(h -> routes.put(h.getRouteId(), h.getRoute()));
        return routes;
    }
}

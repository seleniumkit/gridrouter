package ru.qatools.gridrouter.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithRoutesMap extends WithHosts {

    default Map<String, String> getRoutesMap() {
        Map<String, String> routes = new HashMap<>();
        getHosts().forEach(h -> routes.put(h.getRouteId(), h.getRoute()));
        return routes;
    }
}

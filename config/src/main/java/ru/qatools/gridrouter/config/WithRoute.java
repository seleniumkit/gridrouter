package ru.qatools.gridrouter.config;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithRoute {

    default String getAddress() {
        return getName() + ":" + getPort();
    }

    default String getRoute() {
        return "http://" + getAddress();
    }

    default String getRouteId() {
        return DigestUtils.md5Hex(getRoute().getBytes(StandardCharsets.UTF_8));
    }

    String getName();

    int getPort();
}

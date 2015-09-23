package ru.qatools.gridrouter.config;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithRoute {

    int getPort();

    String getName();

    int getCount();

    default String getRoute() {
        return String.format("http://%s:%d", getName(), getPort());
    }

    default String getRouteId() {
        return DigestUtils.md5Hex(getRoute().getBytes(StandardCharsets.UTF_8));
    }
}

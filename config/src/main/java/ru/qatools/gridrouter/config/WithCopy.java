package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithCopy {

    List<Host> getHosts();

    /**
     * Creates a copy for the {@link Region} object, which is almost deep:
     * the hosts itself are not copied although the list is new.
     *
     * @return a copy of the object
     */
    default Region copy() {
        Region result = new Region();
        result.getHosts().addAll(getHosts());
        return result;
    }
}

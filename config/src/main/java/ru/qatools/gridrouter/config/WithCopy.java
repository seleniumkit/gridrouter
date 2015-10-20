package ru.qatools.gridrouter.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithCopy {

    List<Host> getHosts();

    String getName();

    /**
     * Creates a copy for the {@link Region} object, which is almost deep:
     * the hosts itself are not copied although the list is new.
     *
     * @return a copy of the object
     */
    default Region copy() {
        return new Region(new ArrayList<>(getHosts()), getName());
    }
}

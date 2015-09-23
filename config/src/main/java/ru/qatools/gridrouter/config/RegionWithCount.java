package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface RegionWithCount extends WithCount {

    List<Host> getHosts();

    @Override
    default int getCount() {
        return getHosts().stream().mapToInt(Host::getCount).sum();
    }
}

package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface VersionWithCount extends WithCount {

    List<Region> getRegions();

    @Override
    default int getCount() {
        return getRegions().stream().mapToInt(Region::getCount).sum();
    }
}

package ru.qatools.gridrouter.config;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithUpdateVersion {

    default void update(Version that) {
        List<Host> hosts = that.getRegions().stream()
                .flatMap(region -> region.getHosts().stream())
                .collect(toList());

        for (Iterator<Region> iterator = this.getRegions().iterator(); iterator.hasNext(); ) {
            Region region = iterator.next();
            region.update(hosts);
            if (region.getHosts().isEmpty()) {
                iterator.remove();
            }
        }
    }

    List<Region> getRegions();
}

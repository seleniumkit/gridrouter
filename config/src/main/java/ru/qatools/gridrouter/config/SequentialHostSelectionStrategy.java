package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class SequentialHostSelectionStrategy implements HostSelectionStrategy {

    private int regionIndex;

    private int hostIndex;

    @Override
    public Region selectRegion(List<Region> regions) {
        Region region = regions.get(regionIndex++ % regions.size());
        regionIndex %= regions.size();
        return region;
    }

    @Override
    public Host selectHost(List<Host> hosts) {
        Host host = hosts.get(hostIndex++ % hosts.size());
        hostIndex %= hosts.size();
        return host;
    }
}

package ru.qatools.gridrouter.config;

import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface HostSelectionStrategy {

    Region selectRegion(List<Region> regions);

    Host selectHost(List<Host> hosts);
}

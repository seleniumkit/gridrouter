package ru.qatools.gridrouter.config;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.nCopies;
import static java.util.Collections.shuffle;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class RandomHostSelectionStrategy implements HostSelectionStrategy {

    protected <T extends WithCount> T selectRandom(List<T> elements) {
        List<T> copies = new ArrayList<>();
        for (T element : elements) {
            copies.addAll(nCopies(element.getCount(), element));
        }
        shuffle(copies);
        return copies.get(0);
    }

    @Override
    public Region selectRegion(List<Region> regions) {
        return selectRandom(regions);
    }

    @Override
    public Host selectHost(List<Host> hosts) {
        return selectRandom(hosts);
    }
}

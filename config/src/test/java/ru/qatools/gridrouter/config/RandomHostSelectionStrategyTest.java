package ru.qatools.gridrouter.config;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class RandomHostSelectionStrategyTest {

    private static final double ALLOWED_DEVIATION = 0.01;

    @Test
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void testRandomness() {
        int entriesCount = 5000000;
        int keysCount = 10;

        Host host1 = new Host("host_1", 4444, keysCount - 1);

        List<Host> hosts = new ArrayList<>(keysCount);
        hosts.add(host1);

        int i = keysCount;
        while (i --> 1) {
            hosts.add(newHost());
        }

        HashMap<Host, Integer> appearances = new HashMap<>(keysCount, entriesCount / keysCount);

        RandomHostSelectionStrategy strategy = new RandomHostSelectionStrategy();
        i = entriesCount;
        while (i-- > 0) {
            Host host = strategy.selectRandom(hosts);
            appearances.put(host, Optional.ofNullable(appearances.get(host)).orElse(0) + 1);
        }

        assertThat(appearances.remove(host1), isAround(entriesCount / 2));

        for (int count : appearances.values()) {
            assertThat(count, isAround(entriesCount / 2 / (keysCount - 1)));
        }
    }

    private static Host newHost() {
        return new Host(UUID.randomUUID().toString(), 4444, 1);
    }

    private static Matcher<Integer> isAround(int count) {
        return both(greaterThan(
                (int) (count * (1 - ALLOWED_DEVIATION))
        )).and(lessThan(
                (int) (count * (1 + ALLOWED_DEVIATION))
        ));
    }
}

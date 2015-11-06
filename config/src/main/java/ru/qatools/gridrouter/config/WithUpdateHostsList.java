package ru.qatools.gridrouter.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithUpdateHostsList {

    default void update(List<Host> hostsList) {
        Map<Host, Host> hosts = hostsList.stream().collect(toMap(identity(), identity()));

        for (Iterator<Host> iterator = getHosts().iterator(); iterator.hasNext(); ) {
            Host host = iterator.next();
            if (hosts.containsKey(host)) {
                host.setCount(hosts.get(host).getCount());
            } else {
                iterator.remove();
            }
        }
    }

    List<Host> getHosts();
}

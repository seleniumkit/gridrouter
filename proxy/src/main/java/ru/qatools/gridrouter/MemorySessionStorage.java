package ru.qatools.gridrouter;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class MemorySessionStorage implements SessionStorage {

    private final Map<String, Temporal> session2instant = new HashMap<>();
    private final Map<String, Integer> user2count      = new HashMap<>();
    private final Map<String, String>  session2user    = new HashMap<>();

    @Override
    public synchronized void put(String sessionId, String user) {
        if (session2instant.put(sessionId, now()) == null) {
            user2count.compute(user, (k, count) -> Optional.ofNullable(count).orElse(0) + 1);
            session2user.put(sessionId, user);
        }
    }

    @Override
    public void update(String sessionId) {
        session2instant.replace(sessionId, now());
    }

    @Override
    public synchronized void remove(String sessionId) {
        if (session2instant.remove(sessionId) != null) {
            String user = session2user.remove(sessionId);
            user2count.compute(user, (k, count) -> count - 1);
        }
    }

    @Override
    public synchronized List<String> expireSessionsOlderThan(Duration duration) {
        List<String> sessions2delete = session2instant.entrySet().stream()
                .filter(e -> duration.compareTo(Duration.between(e.getValue(), now())) < 0)
                .map(Map.Entry::getKey)
                .collect(toList());
        sessions2delete.stream().forEach(this::remove);
        return sessions2delete;
    }

    @Override
    public synchronized int getCountFor(String user) {
        return user2count.getOrDefault(user, 0);
    }
}

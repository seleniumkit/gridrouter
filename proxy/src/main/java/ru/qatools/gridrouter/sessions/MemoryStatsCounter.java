package ru.qatools.gridrouter.sessions;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class MemoryStatsCounter implements StatsCounter {

    private final Map<String, Temporal> session2instant = new HashMap<>();
    private final Map<String, String> session2user = new HashMap<>();
    private final Map<String, BrowserVersion> session2browserVersion = new HashMap<>();
    private final Map<String, BrowsersCountMap> user2browserCount = new HashMap<>();

    @Override
    public synchronized void startSession(String sessionId, String user, String browser, String version, String route) {
        if (session2instant.put(sessionId, now()) == null) {
            session2user.put(sessionId, user);
            session2browserVersion.put(sessionId, new BrowserVersion(browser, version));
            user2browserCount.putIfAbsent(user, new BrowsersCountMap());
            user2browserCount.get(user).increment(browser, version);
        }
    }

    @Override
    public void updateSession(String sessionId, String route) {
        session2instant.replace(sessionId, now());
    }

    @Override
    public synchronized void deleteSession(String sessionId, String route) {
        if (session2instant.remove(sessionId) != null) {
            String user = session2user.remove(sessionId);
            BrowserVersion browser = session2browserVersion.remove(sessionId);
            user2browserCount.get(user).decrement(browser);
        }
    }

    @Override
    public void expireSessionsOlderThan(Duration duration) {
        List<String> sessions2delete = session2instant.entrySet().stream()
                .filter(e -> duration.compareTo(Duration.between(e.getValue(), now())) < 0)
                .map(Map.Entry::getKey)
                .collect(toList());
        sessions2delete.stream().forEach(this::deleteSession);
    }

    @Override
    public Set<String> getActiveSessions() {
        return session2instant.keySet();
    }

    @Override
    public synchronized BrowsersCountMap getStats(String user) {
        return user2browserCount.getOrDefault(user, new BrowsersCountMap());
    }
}

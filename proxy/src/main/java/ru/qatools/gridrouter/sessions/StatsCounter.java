package ru.qatools.gridrouter.sessions;

import java.time.Duration;
import java.util.Set;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface StatsCounter {

    void startSession(String sessionId, String user, String browser, String version);

    void updateSession(String sessionId);

    void deleteSession(String sessionId);

    void expireSessionsOlderThan(Duration duration);

    Set<String> getActiveSessions();

    GridRouterUserStats getStats(String user);
}

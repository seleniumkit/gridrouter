package ru.qatools.gridrouter.sessions;

import java.time.Duration;
import java.util.Set;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface StatsCounter {

    default void startSession(String sessionId, String user, String browser, String version) {
        startSession(sessionId, user, browser, version, null);
    }

    default void updateSession(String sessionId) {
        updateSession(sessionId, null);
    }

    default void deleteSession(String sessionId) {
        deleteSession(sessionId, null);
    }

    void startSession(String sessionId, String user, String browser, String version, String route);

    default void updateSession(String sessionId, String route) {

    }

    void deleteSession(String sessionId, String route);

    void expireSessionsOlderThan(Duration duration);

    Set<String> getActiveSessions();

    GridRouterUserStats getStats(String user);

    int getSessionsCountForUser(String user);

    int getSessionsCountForUserAndBrowser(String user, String browser, String version);
}

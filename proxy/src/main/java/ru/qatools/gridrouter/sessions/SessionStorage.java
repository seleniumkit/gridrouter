package ru.qatools.gridrouter.sessions;

import java.time.Duration;
import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface SessionStorage {

    void put(String sessionId, String user, String browser, String version);

    void update(String sessionId);

    void remove(String sessionId);

    List<String> expireSessionsOlderThan(Duration duration);

    BrowsersCountMap getBrowsersCountFor(String user);
}

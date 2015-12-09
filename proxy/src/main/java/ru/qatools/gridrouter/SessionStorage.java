package ru.qatools.gridrouter;

import java.time.Duration;
import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface SessionStorage {

    void put(String sessionId, String user);

    void update(String sessionId);

    void remove(String sessionId);

    List<String> expireSessionsOlderThan(Duration duration);

    int getCountFor(String user);
}

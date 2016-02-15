package ru.qatools.gridrouter.sessions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.qatools.gridrouter.json.JsonFormatter.toJson;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class MemoryStatsCounterTest {

    private MemoryStatsCounter storage;

    @Before
    public void setUp() throws Exception {
        storage = new MemoryStatsCounter();
    }

    @Test
    public void testEmptyStorage() throws Exception {
        assertThat(countJsonFor("user"), is("{}"));
        assertThat(expiredSessions(ZERO), is(empty()));
        assertThat(expiredSessions(Duration.ofDays(1)), is(empty()));
    }

    @Test
    public void testAddSession() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "33");
        storage.startSession("session3", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":3}}"));
        storage.startSession("session1", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":3}}"));
    }

    @Test
    public void testDifferentBrowsers() throws Exception {
        storage.startSession("session1", "user", "chrome", "33");
        storage.startSession("session2", "user", "firefox", "33");
        storage.startSession("session3", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"chrome\":{\"33\":1},\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testDifferentVersions() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "34");
        storage.startSession("session3", "user", "firefox", "34");
        storage.startSession("session4", "user", "firefox", "firefox");
        storage.startSession("session5", "user", "firefox", "firefox");
        storage.startSession("session6", "user", "firefox", "firefox");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1,\"34\":2,\"firefox\":3}}"));
    }

    @Test
    public void testRemoveExistingSession() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
        storage.deleteSession("session1");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        storage.deleteSession("session1");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        storage.deleteSession("session2");
        assertThat(countJsonFor("user"), is("{}"));
    }

    @Test
    public void testRemoveNotExistingSession() throws Exception {
        storage.deleteSession("session1");
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
        storage.deleteSession("session4");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testMultipleUsers() throws Exception {
        storage.startSession("session1", "user1", "firefox", "33");
        storage.startSession("session2", "user2", "firefox", "33");
        storage.startSession("session3", "user2", "firefox", "33");
        assertThat(countJsonFor("user1"), is("{\"firefox\":{\"33\":1}}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":2}}"));
        storage.deleteSession("session1");
        storage.deleteSession("session2");
        assertThat(countJsonFor("user1"), is("{}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":1}}"));
    }

    @Test
    public void testNewSessionsAreNotExpired() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "33");
        assertThat(expiredSessions(1000), is(empty()));
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testOldSessionsAreExpired() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        storage.startSession("session2", "user", "firefox", "33");
        Thread.sleep(500);
        storage.startSession("session3", "user", "firefox", "33");
        assertThat(expiredSessions(250), containsInAnyOrder("session1", "session2"));
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        Thread.sleep(500);
        assertThat(expiredSessions(250), contains("session3"));
        assertThat(countJsonFor("user"), is("{}"));
    }

    @Test
    public void testUpdateExistingSession() throws Exception {
        storage.startSession("session1", "user", "firefox", "33");
        Thread.sleep(500);
        storage.updateSession("session1");
        assertThat(expiredSessions(250), is(empty()));
    }

    @Test
    public void testMultipleUsersExpiration() throws Exception {
        storage.startSession("session1", "user1", "firefox", "33");
        Thread.sleep(500);
        storage.startSession("session2", "user2", "firefox", "33");
        assertThat(expiredSessions(250), contains("session1"));
        assertThat(countJsonFor("user1"), is("{}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":1}}"));
    }

    private String countJsonFor(String user) throws JsonProcessingException {
        return toJson(storage.getStats(user));
    }

    public Set<String> expiredSessions(int millis) {
        return expiredSessions(Duration.ofMillis(millis));
    }

    public Set<String> expiredSessions(Duration duration) {
        final Set<String> removedSessionIds = new HashSet<>(storage.getActiveSessions());
        storage.expireSessionsOlderThan(duration);
        removedSessionIds.removeAll(storage.getActiveSessions());
        return removedSessionIds;
    }
}

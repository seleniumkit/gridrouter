package ru.qatools.gridrouter.sessions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static ru.qatools.gridrouter.json.JsonFormatter.toJson;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class MemorySessionStorageTest {

    private MemorySessionStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new MemorySessionStorage();
    }

    @Test
    public void testEmptyStorage() throws Exception {
        assertThat(countJsonFor("user"), is("{}"));
        assertThat(storage.expireSessionsOlderThan(ZERO), is(empty()));
        assertThat(storage.expireSessionsOlderThan(Duration.ofDays(1)), is(empty()));
    }

    @Test
    public void testAddSession() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "33");
        storage.put("session3", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":3}}"));
        storage.put("session1", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":3}}"));
    }

    @Test
    public void testDifferentBrowsers() throws Exception {
        storage.put("session1", "user", "chrome", "33");
        storage.put("session2", "user", "firefox", "33");
        storage.put("session3", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"chrome\":{\"33\":1},\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testDifferentVersions() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "34");
        storage.put("session3", "user", "firefox", "34");
        storage.put("session4", "user", "firefox", "firefox");
        storage.put("session5", "user", "firefox", "firefox");
        storage.put("session6", "user", "firefox", "firefox");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1,\"34\":2,\"firefox\":3}}"));
    }

    @Test
    public void testRemoveExistingSession() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
        storage.remove("session1");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        storage.remove("session1");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        storage.remove("session2");
        assertThat(countJsonFor("user"), is("{}"));
    }

    @Test
    public void testRemoveNotExistingSession() throws Exception {
        storage.remove("session1");
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "33");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
        storage.remove("session4");
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testMultipleUsers() throws Exception {
        storage.put("session1", "user1", "firefox", "33");
        storage.put("session2", "user2", "firefox", "33");
        storage.put("session3", "user2", "firefox", "33");
        assertThat(countJsonFor("user1"), is("{\"firefox\":{\"33\":1}}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":2}}"));
        storage.remove("session1");
        storage.remove("session2");
        assertThat(countJsonFor("user1"), is("{}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":1}}"));
    }

    @Test
    public void testNewSessionsAreNotExpired() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "33");
        assertThat(expiredSessions(1000), is(empty()));
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":2}}"));
    }

    @Test
    public void testOldSessionsAreExpired() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        storage.put("session2", "user", "firefox", "33");
        Thread.sleep(500);
        storage.put("session3", "user", "firefox", "33");
        assertThat(expiredSessions(250), containsInAnyOrder("session1", "session2"));
        assertThat(countJsonFor("user"), is("{\"firefox\":{\"33\":1}}"));
        Thread.sleep(500);
        assertThat(expiredSessions(250), contains("session3"));
        assertThat(countJsonFor("user"), is("{}"));
    }

    @Test
    public void testUpdateExistingSession() throws Exception {
        storage.put("session1", "user", "firefox", "33");
        Thread.sleep(500);
        storage.update("session1");
        assertThat(expiredSessions(250), is(empty()));
    }

    @Test
    public void testMultipleUsersExpiration() throws Exception {
        storage.put("session1", "user1", "firefox", "33");
        Thread.sleep(500);
        storage.put("session2", "user2", "firefox", "33");
        assertThat(expiredSessions(250), contains("session1"));
        assertThat(countJsonFor("user1"), is("{}"));
        assertThat(countJsonFor("user2"), is("{\"firefox\":{\"33\":1}}"));
    }

    private String countJsonFor(String user) throws JsonProcessingException {
        return toJson(storage.getBrowsersCountFor(user));
    }

    public List<String> expiredSessions(int millis) {
        return storage.expireSessionsOlderThan(Duration.ofMillis(millis));
    }
}

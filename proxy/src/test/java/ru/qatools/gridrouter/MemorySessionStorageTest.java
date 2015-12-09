package ru.qatools.gridrouter;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

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
    public void testEmptyStorage() {
        assertThat(storage.getCountFor("user1"), is(0));
        assertThat(storage.expireSessionsOlderThan(ZERO), is(empty()));
        assertThat(storage.expireSessionsOlderThan(Duration.ofDays(1)), is(empty()));
    }

    @Test
    public void testAddSession() {
        storage.put("session1", "user");
        storage.put("session2", "user");
        storage.put("session3", "user");
        assertThat(storage.getCountFor("user"), is(3));
        storage.put("session1", "user");
        assertThat(storage.getCountFor("user"), is(3));
    }

    @Test
    public void testRemoveExistingSession() {
        storage.put("session1", "user");
        storage.put("session2", "user");
        assertThat(storage.getCountFor("user"), is(2));
        storage.remove("session1");
        assertThat(storage.getCountFor("user"), is(1));
        storage.remove("session1");
        assertThat(storage.getCountFor("user"), is(1));
        storage.remove("session2");
        assertThat(storage.getCountFor("user"), is(0));
    }

    @Test
    public void testRemoveNotExistingSession() {
        storage.remove("session1");
        storage.put("session1", "user");
        storage.put("session2", "user");
        assertThat(storage.getCountFor("user"), is(2));
        storage.remove("session4");
        assertThat(storage.getCountFor("user"), is(2));
    }

    @Test
    public void testMultipleUsers() {
        storage.put("session1", "user1");
        storage.put("session2", "user2");
        storage.put("session3", "user2");
        assertThat(storage.getCountFor("user1"), is(1));
        assertThat(storage.getCountFor("user2"), is(2));
        storage.remove("session1");
        storage.remove("session2");
        assertThat(storage.getCountFor("user1"), is(0));
        assertThat(storage.getCountFor("user2"), is(1));
    }

    @Test
    public void testNewSessionsAreNotExpired() throws Exception {
        storage.put("session1", "user");
        storage.put("session2", "user");
        assertThat(storage.expireSessionsOlderThan(Duration.ofSeconds(1)), is(empty()));
        assertThat(storage.getCountFor("user"), is(2));
    }

    @Test
    public void testOldSessionsAreExpired() throws Exception {
        storage.put("session1", "user");
        storage.put("session2", "user");
        Thread.sleep(500);
        storage.put("session3", "user");
        assertThat(storage.expireSessionsOlderThan(Duration.ofMillis(250)), containsInAnyOrder("session1", "session2"));
        assertThat(storage.getCountFor("user"), is(1));
        Thread.sleep(500);
        assertThat(storage.expireSessionsOlderThan(Duration.ofMillis(250)), contains("session3"));
        assertThat(storage.getCountFor("user"), is(0));
    }

    @Test
    public void testUpdateExistingSession() throws Exception {
        storage.put("session1", "user");
        Thread.sleep(500);
        storage.update("session1");
        assertThat(storage.expireSessionsOlderThan(Duration.ofMillis(250)), is(empty()));
    }

    @Test
    public void testMultipleUsersExpiration() throws Exception {
        storage.put("session1", "user1");
        Thread.sleep(500);
        storage.put("session2", "user2");
        assertThat(storage.expireSessionsOlderThan(Duration.ofMillis(250)), contains("session1"));
        assertThat(storage.getCountFor("user1"), is(0));
        assertThat(storage.getCountFor("user2"), is(1));
    }
}

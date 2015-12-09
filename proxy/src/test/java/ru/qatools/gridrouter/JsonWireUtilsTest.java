package ru.qatools.gridrouter;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static ru.qatools.gridrouter.JsonWireUtils.WD_HUB_SESSION;
import static ru.qatools.gridrouter.JsonWireUtils.getFullSessionId;
import static ru.qatools.gridrouter.JsonWireUtils.getSessionHash;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class JsonWireUtilsTest {

    @Test
    public void testGetSessionHash() {
        String routeHash = md5Hex("hubAddress".getBytes(StandardCharsets.UTF_8));
        assertThat(getSessionHash(sessionRequest(routeHash, randomUUID().toString(), "")), is(equalTo(routeHash)));
        assertThat(getSessionHash(sessionRequest(routeHash, randomUUID().toString(), "dhgdhg")), is(equalTo(routeHash)));
        assertThat(getSessionHash(sessionRequest(routeHash, randomUUID().toString(), "dh/gdh/")), is(equalTo(routeHash)));
        assertThat(getSessionHash(sessionRequest(routeHash, randomUUID().toString(), "dh/gdh/g")), is(equalTo(routeHash)));
    }

    @Test
    public void testGetFullSessionId() {
        String routeHash = md5Hex("hubAddress".getBytes(StandardCharsets.UTF_8));
        String sessionId = randomUUID().toString();
        String expected = routeHash + sessionId;
        assertThat(getFullSessionId(sessionRequest(routeHash, sessionId, "")), is(equalTo(expected)));
        assertThat(getFullSessionId(sessionRequest(routeHash, sessionId, "sfgsds")), is(equalTo(expected)));
        assertThat(getFullSessionId(sessionRequest(routeHash, sessionId, "sfg/sds/")), is(equalTo(expected)));
        assertThat(getFullSessionId(sessionRequest(routeHash, sessionId, "sfg/sds/adfad")), is(equalTo(expected)));
    }

    public String sessionRequest(String routeHash, String sessionId, String sessionCommand) {
        if (!sessionCommand.isEmpty()) {
            sessionCommand = "/".concat(sessionCommand);
        }
        return WD_HUB_SESSION + routeHash + sessionId + sessionCommand;
    }
}

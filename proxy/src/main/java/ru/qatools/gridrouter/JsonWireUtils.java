package ru.qatools.gridrouter;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.DELETE;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
public final class JsonWireUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonWireUtils.class);

    public static final String WD_HUB_SESSION = "/wd/hub/session/";

    public static final int SESSION_HASH_LENGTH = 32;

    private JsonWireUtils() {
    }

    public static boolean isUriValid(String uri) {
        return uri.length() > getUriPrefixLength();
    }

    public static boolean isSessionDeleteRequest(HttpServletRequest request, String command) {
        return DELETE.name().equalsIgnoreCase(request.getMethod()) && !command.contains("/");
    }

    public static String getSessionHash(String uri) {
        return uri.substring(WD_HUB_SESSION.length(), getUriPrefixLength());
    }

    public static String getFullSessionId(String uri) {
        String tail = uri.substring(WD_HUB_SESSION.length());
        int end = tail.indexOf('/');
        if (end < 0) {
            return tail;
        }
        return tail.substring(0, end);
    }

    public static int getUriPrefixLength() {
        return WD_HUB_SESSION.length() + SESSION_HASH_LENGTH;
    }

    public static String redirectionUrl(String host, String command) throws URISyntaxException {
        return new URIBuilder(host).setPath(WD_HUB_SESSION + command).build().toString();
    }

    public static String getCommand(String uri) {
        String encodedCommand = uri.substring(getUriPrefixLength());
        try {
            return URLDecoder.decode(encodedCommand, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("[UNABLE_TO_DECODE_COMMAND] - could not decode command: {}", encodedCommand, e);
            return encodedCommand;
        }
    }
}

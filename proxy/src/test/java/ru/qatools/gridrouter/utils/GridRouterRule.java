package ru.qatools.gridrouter.utils;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.util.security.Password;

import java.net.MalformedURLException;
import java.net.URL;

import static java.util.UUID.randomUUID;
import static ru.qatools.gridrouter.utils.SocketUtil.findFreePort;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class GridRouterRule extends JettyRule {

    public static final String USER_1 = "user1";
    public static final String USER_2 = "user2";
    public static final String USER_3 = "user3";
    public static final String USER_4 = "user4";
    public static final String PASSWORD = "password";
    public static final String ROLE = "user";

    public final String baseUrl;
    public final String baseUrlWithAuth;
    public final String baseUrlWrongPassword;


    public GridRouterRule() {
        super(
                "/",
                "src/main/webapp",
                "target/classes",
                findFreePort(),
                new HashLoginService() {{
                    setName("Selenium Grid Router");
                    putUser(USER_1, new Password(PASSWORD), new String[]{ROLE});
                    putUser(USER_2, new Password(PASSWORD), new String[]{ROLE});
                    putUser(USER_3, new Password(PASSWORD), new String[]{ROLE});
                    putUser(USER_4, new Password(PASSWORD), new String[]{ROLE});
                }}
        );
        baseUrl = "http://localhost:" + getPort();
        baseUrlWithAuth = baseUrl(USER_1);
        baseUrlWrongPassword = baseUrl(USER_1, randomUUID().toString());
    }

    public static URL hubUrl(String baseUrl) {
        try {
            return new URL(baseUrl + "/wd/hub");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String baseUrl(String user) {
        return baseUrl(user, PASSWORD);
    }

    public String baseUrl(String user, String password) {
        return String.format("http://%s:%s@localhost:%d",
                user, password, getPort());
    }
}

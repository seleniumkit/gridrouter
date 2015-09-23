package ru.qatools.gridrouter.utils;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.util.security.Password;

import java.net.MalformedURLException;
import java.net.URL;

import static java.util.UUID.randomUUID;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class GridRouterRule extends JettyRule {

    public static final int GRID_ROUTER_PORT = 8080;
    public static final int HUB_PORT = GRID_ROUTER_PORT + 1;

    public static final String USER_1 = "user1";
    public static final String USER_2 = "user2";
    public static final String USER_3 = "user3";
    public static final String PASSWORD = "password";
    public static final String ROLE = "user";

    public static final String BASE_URL = "http://localhost:" + GRID_ROUTER_PORT;
    public static final String BASE_URL_WITH_AUTH = baseUrl(USER_1);

    public static final String BASE_URL_WITH_WRONG_PASSWORD
            = baseUrl(USER_1, randomUUID().toString());


    public GridRouterRule() {
        super(
                "/",
                "src/main/webapp",
                "target/classes",
                GRID_ROUTER_PORT,
                new HashLoginService() {{
                    setName("Selenium Grid Router");
                    putUser(USER_1, new Password(PASSWORD), new String[]{ROLE});
                    putUser(USER_2, new Password(PASSWORD), new String[]{ROLE});
                    putUser(USER_3, new Password(PASSWORD), new String[]{ROLE});
                }}
        );
    }

    public static String baseUrl(String user) {
        return baseUrl(user, PASSWORD);
    }

    public static String baseUrl(String user, String password) {
        return String.format("http://%s:%s@localhost:%d",
                user, password, GRID_ROUTER_PORT);
    }

    public static URL hubUrl(String baseUrl) {
        try {
            return new URL(baseUrl + "/wd/hub");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

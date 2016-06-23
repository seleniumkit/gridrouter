package ru.qatools.gridrouter.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import static ru.qatools.gridrouter.utils.GridRouterRule.hubUrl;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public final class MatcherUtils {

    private MatcherUtils() {
    }

    /**
     * Creates a matcher that tries to obtain a browser
     * for a user that it is matched against.
     *
     * @return A matcher instance that creates a new webdriver
     * on {@link Matcher#matches(Object) matches()} method invocation.
     *
     * @param browser capabilities for the browser to obtain
     */
    public static Matcher<String> canObtain(final GridRouterRule gridRouter, final DesiredCapabilities browser) {
        return new TypeSafeMatcher<String>() {

            private Exception exception;

            @Override
            protected boolean matchesSafely(String user) {
                try {
                    new RemoteWebDriver(hubUrl(gridRouter.baseUrl(user)), browser);
                    return true;
                } catch (Exception e) {
                    exception = e;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("not able to obtain browser because of ")
                           .appendValue(exception.toString());
            }
        };
    }
}

package ru.qatools.gridrouter.config;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class BrowsersUpdateTest {

    @Test
    public void testUpdateRegion() {
        Region region = new Region(list(
                new Host("host1", 4444, 5),
                new Host("host2", 4444, 5),
                new Host("host3", 4444, 5)
        ), "region");

        region.update(asList(
                new Host("host1", 4444, 7),
                new Host("host3", 4445, 5),
                new Host("host4", 4444, 5)
        ));

        assertThat(region.getHosts(), contains(hostThatIs("host1", 4444, 7)));
        assertThat(region.getName(), is(equalTo("region")));
    }

    @Test
    public void testUpdateVersion() {
        Version version = new Version(list(
                new Region(list(new Host("host1", 4444, 5)), "region1"),
                new Region(list(new Host("host2", 4444, 5)), "region2"),
                new Region(list(new Host("host3", 4444, 5)), "region3")
        ), "33");

        version.update(new Version(list(
                new Region(list(new Host("host1", 4444, 6)), "region1"),
                new Region(list(
                        new Host("host2", 4444, 7),
                        new Host("host4", 4444, 8)
                ), "region4")
        ), "34"));

        assertThat(version.getRegions().size(), is(2));
        assertThat(version.getNumber(), is(equalTo("33")));

        Region region1 = version.getRegions().get(0);
        assertThat(region1.getName(), is(equalTo("region1")));
        assertThat(region1.getHosts(), contains(hostThatIs("host1", 4444, 6)));

        Region region2 = version.getRegions().get(1);
        assertThat(region2.getName(), is(equalTo("region2")));
        assertThat(region2.getHosts(), contains(hostThatIs("host2", 4444, 7)));
    }

    @Test
    public void testUpdateBrowser() {
        Browser browser = new Browser(list(
                new Version(list(
                        new Region(list(new Host("host1", 4444, 5)), "region1")
                ), "33"),
                new Version(list(
                        new Region(list(new Host("host2", 4444, 5)), "region2")
                ), "34")
        ), "firefox", "35");

        browser.update(new Browser(list(
                new Version(list(
                        new Region(list(new Host("host1", 4444, 6)), "region1")
                ), "33"),
                new Version(list(
                        new Region(list(new Host("host2", 4444, 6)), "region2")
                ), "35")
        ), "chrome", "36"));

        assertThat(browser.getVersions().size(), is(1));
        assertThat(browser.getName(), is("firefox"));
        assertThat(browser.getDefaultVersion(), is("35"));

        Version version = browser.getVersions().get(0);
        assertThat(version.getRegions().get(0).getHosts(), contains(hostThatIs("host1", 4444, 6)));
    }

    @SafeVarargs
    private final <T> ArrayList<T> list(T... items) {
        return new ArrayList<>(asList(items));
    }

    private Matcher<Host> hostThatIs(String name, int port, int count) {
        return new TypeSafeMatcher<Host>() {
            @Override
            protected boolean matchesSafely(Host host) {
                return host.getName().equals(name) && host.getPort() == port && host.getCount() == count;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(format("a host with {name=%s, port=%d, count=%d}", name, port, count));
            }
        };
    }
}

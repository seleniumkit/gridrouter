package ru.qatools.gridrouter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
@RunWith(Parameterized.class)
public class CommandDecodingTest {

    public static final String SUFFIX = "http://host.com/wd/hub/session/8dec71ede39ad9ff3";

    public static final String POSTFIX = "b3fbc03311bdc45282358f1-f09c-4c44-8057-4b82f4a53002/element/id/";

    public String requestUri;

    public String elementId;

    public CommandDecodingTest(String elementId) throws Exception {
        this.requestUri = String.format("%s%s%s", SUFFIX, POSTFIX, URLEncoder.encode(elementId, UTF_8.name()));
        this.elementId = elementId;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getData() {
        return Arrays.asList(
                new Object[]{"text_???"},
                new Object[]{"text_&_not_text"}
        );
    }

    @Test
    public void testOutput() throws Exception {
        assertThat(JsonWireUtils.getCommand(requestUri), endsWith(elementId));
    }
}

package ru.qatools.gridrouter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static ru.qatools.gridrouter.json.WithErrorMessage.DEFAULT_ERROR_MESSAGE;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class JsonMessageTest {

    @Test
    public void testProperJson() throws IOException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("status", 69);
        jsonObject.put("sessionId", "session id");
        jsonObject.put("some other key", "some other value");

        JSONObject capabilitiesObject = new JSONObject();
        capabilitiesObject.put("browserName", "firefox");
        capabilitiesObject.put("version", "32.0");
        capabilitiesObject.put("some capability key", "some capability value");
        jsonObject.put("desiredCapabilities", capabilitiesObject);

        JSONObject valueObject = new JSONObject();
        valueObject.put("message", "some error message");
        valueObject.put("some value key", "some value value");
        jsonObject.put("value", valueObject);

        JsonMessage jsonMessage = JsonMessageFactory.from(jsonObject.toString());

        assertThat(jsonMessage.getStatus(), is(69));
        assertThat(jsonMessage.getSessionId(), is("session id"));
        assertThat(jsonMessage.any().get("some other key"), is("some other value"));

        JsonCapabilities jsonCapabilities = jsonMessage.getDesiredCapabilities();
        assertThat(jsonCapabilities.getBrowserName(), is("firefox"));
        assertThat(jsonCapabilities.getVersion(), is("32.0"));
        assertThat(jsonCapabilities.any().get("some capability key"), is("some capability value"));

        assertThat(jsonMessage.getErrorMessage(), is("some error message"));
    }

    @Test
    public void testJsonWithKeysMissing() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 69);

        JsonMessage jsonMessage = JsonMessageFactory.from(jsonObject.toString());

        assertThat(jsonMessage.getStatus(), is(69));
        assertThat(jsonMessage.getSessionId(), is(nullValue()));
        assertThat(jsonMessage.getDesiredCapabilities(), is(nullValue()));
    }

    @Test
    public void testErrorMessageForNullValue() throws IOException {
        JSONObject jsonObject = new JSONObject();
        JsonMessage jsonMessage = JsonMessageFactory.from(jsonObject.toString());
        assertThat(jsonMessage.getErrorMessage(), is(DEFAULT_ERROR_MESSAGE));
    }

    @Test
    public void testNullErrorMessageForPresentValue() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", new JSONObject());
        JsonMessage jsonMessage = JsonMessageFactory.from(jsonObject.toString());
        assertThat(jsonMessage.getErrorMessage(), is(DEFAULT_ERROR_MESSAGE));
    }

    @Test
    public void testValueOfSimpleType() throws IOException {
        String jsonRaw =
                "{"
                    + "\"using\":\"xpath\","
                    + "\"value\":\"//lol[foo='bar']\""
                + "}";
        JsonMessage jsonMessage = JsonMessageFactory.from(jsonRaw);

        assertThat(jsonMessage.getSessionId(), is(nullValue()));
        assertThat(jsonMessage.any().get("value"), is("//lol[foo='bar']"));
    }

    @Test
    public void testJsonView() throws JsonProcessingException {
        JsonMessage jsonMessage = new JsonMessage();

        jsonMessage.setSessionId("session id");
        jsonMessage.setStatus(69);

        JsonCapabilities jsonCapabilities = new JsonCapabilities();
        jsonCapabilities.setBrowserName("browser name");
        jsonCapabilities.setVersion("browser version");
        jsonMessage.setDesiredCapabilities(jsonCapabilities);

        jsonMessage.set("some key", "some value");

        JSONObject jsonObject = new JSONObject(jsonMessage.toJson());
        assertThat(jsonObject.getString("sessionId"), is("session id"));
        assertThat(jsonObject.getInt("status"), is(69));

        JSONObject capabilitiesObject = jsonObject.getJSONObject("desiredCapabilities");
        assertThat(capabilitiesObject.get("browserName"), is("browser name"));
        assertThat(capabilitiesObject.get("version"), is("browser version"));

        assertThat(jsonObject.isNull("value"), is(true));
        assertThat(jsonObject.isNull("message"), is(true));
        assertThat(jsonObject.isNull("errorMessage"), is(true));
    }

    @Test
    public void testSettingErrorMessage() throws JsonProcessingException {
        JsonMessage jsonMessage = JsonMessageFactory.error(69, "some error message");
        JSONObject jsonObject = new JSONObject(jsonMessage.toJson());
        assertThat(jsonObject.getInt("status"), is(69));
        assertThat(jsonObject.getJSONObject("value").getString("message"), is("some error message"));

    }
}

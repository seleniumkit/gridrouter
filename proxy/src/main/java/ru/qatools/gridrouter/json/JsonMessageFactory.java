package ru.qatools.gridrouter.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public final class JsonMessageFactory {

    JsonMessageFactory() {
    }

    public static JsonMessage from(String content) throws IOException {
        return new ObjectMapper().readValue(content, JsonMessage.class);
    }

    public static JsonMessage from(InputStream stream) throws IOException {
        return new ObjectMapper().readValue(stream, JsonMessage.class);
    }

    public static JsonMessage error(int status, String errorMessage) {
        JsonMessage message = new JsonMessage();
        message.setStatus(status);
        message.setErrorMessage(errorMessage);
        return message;
    }
}

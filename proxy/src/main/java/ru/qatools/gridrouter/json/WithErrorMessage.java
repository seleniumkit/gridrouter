package ru.qatools.gridrouter.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithErrorMessage {

    String VALUE_KEY = "value";
    String MESSAGE_KEY = "message";

    String DEFAULT_ERROR_MESSAGE = "no error message was provided from hub";

    Map<String, Object> any();

    void set(String name, Object value);

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default String getErrorMessage() throws IOException {
        try {
            return (String) ((Map<String, Object>)
                    any().getOrDefault(VALUE_KEY, emptyMap()))
                         .getOrDefault(MESSAGE_KEY, DEFAULT_ERROR_MESSAGE);
        } catch (ClassCastException ignored) {
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @JsonIgnore
    default void setErrorMessage(String message) {
        JsonValue value = new JsonValue();
        value.setMessage(message);
        set(VALUE_KEY, value);
    }
}

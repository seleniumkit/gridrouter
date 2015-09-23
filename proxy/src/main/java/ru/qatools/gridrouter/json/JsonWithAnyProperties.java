package ru.qatools.gridrouter.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public abstract class JsonWithAnyProperties {

    private Map<String, Object> otherProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> any() {
        return otherProperties;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        otherProperties.put(name, value);
    }
}

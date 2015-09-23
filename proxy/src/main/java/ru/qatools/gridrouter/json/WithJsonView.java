package ru.qatools.gridrouter.json;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public interface WithJsonView {

    default String toJson() throws JsonProcessingException {
        return JsonFormatter.toJson(this);
    }
}

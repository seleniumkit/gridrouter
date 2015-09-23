package ru.qatools.gridrouter.utils;

import org.json.JSONObject;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

/**
 * Sets the element id (according to protocol specification)
 * to the hashcode of the selector. This way we can check that
 * the selector was passed through proxy correctly.
 *
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class FindElementCallback implements ExpectationCallback {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        JSONObject jsonObject = new JSONObject(httpRequest.getBodyAsString());
        String selector = jsonObject.get("value").toString();

        JSONObject responce = new JSONObject();
        responce.put("status", 0);
        JSONObject value = new JSONObject();
        value.put("ELEMENT", selector.hashCode());
        responce.put("value", value);
        return response(responce.toString()).withStatusCode(500);
    }
}

package ru.qatools.gridrouter.utils;

import org.json.JSONObject;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public class RememberUrlCallback implements ExpectationCallback {

    private static String currentUrl = "{\"value\":\"\"}";

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if (httpRequest.getMethod().toString().contains("POST")) {
            JSONObject jsonObject = new JSONObject(httpRequest.getBodyAsString());
            currentUrl = jsonObject.get("url").toString();
            return response();
        } else if (httpRequest.getMethod().toString().contains("GET")) {
            return response(currentUrl);
        }
        return response("invalid request!").withStatusCode(400);
    }
}

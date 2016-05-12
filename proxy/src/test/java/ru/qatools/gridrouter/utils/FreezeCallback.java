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
public class FreezeCallback implements ExpectationCallback {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        final long HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
//        try {
//            Thread.sleep(HOUR_IN_MILLISECONDS);
//        } catch (InterruptedException ignored) {}
        return response().withStatusCode(500);
    }
}

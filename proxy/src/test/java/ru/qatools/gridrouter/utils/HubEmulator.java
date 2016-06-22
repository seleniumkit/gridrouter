package ru.qatools.gridrouter.utils;

import org.json.JSONObject;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpCallback.callback;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class HubEmulator {

    private static final String WD_HUB_SESSION = "/wd/hub/session";

    private static final String SESSION_ID_REGEX = "[-a-zA-Z0-9]{36}";

    private ClientAndServer hub;

    public HubEmulator(int hubPort) {
        hub = startClientAndServer(hubPort);
    }

    public HubEmulations emulate() {
        return new HubEmulations();
    }

    public HubVerifications verify() {
        return new HubVerifications();
    }

    public void stop() {
        hub.stop();
    }

    public class HubEmulations {

        public HubEmulations newSessions(int sessionsCount) {
            for (int i = 0; i < sessionsCount; i++) {
                hub.when(newSessionRequest(), once()).respond(newSessionSuccessful());
            }
            return this;
        }

        public HubEmulations newSessionFailures(int times) {
            return newSessionFailures(Times.exactly(times));
        }

        public HubEmulations newSessionFailures(Times times) {
            hub.when(newSessionRequest(), times).respond(newSessionFailed());
            return this;
        }

        public HubEmulations newSessionFreeze(int seconds) {
            hub.when(newSessionRequest(), once()).respond(
                    response()
//                            .withDelay(TimeUnit.SECONDS, seconds)
                            .withStatusCode(500)
            );
            return this;
        }
        
        public HubEmulations navigation() {
            hub.when(sessionRequest("url"))
               .callback(callback().withCallbackClass(
                       RememberUrlCallback.class.getCanonicalName()));
            return this;
        }

        public HubEmulations findElement() {
            hub.when(sessionRequest("element").withMethod("POST"))
               .callback(callback().withCallbackClass(
                       FindElementCallback.class.getCanonicalName()));
            return this;
        }

        public HubEmulations quit() {
            hub.when(sessionQuitRequest()).respond(emptyResponse());
            return this;
        }
    }

    public class HubVerifications {

        public HubVerifications newSessionRequestsCountIs(int sessionsCount) {
            hub.verify(newSessionRequest(), exactly(sessionsCount));
            return this;
        }

        public HubVerifications quitRequestsCountIs(int times) {
            hub.verify(sessionQuitRequest(), exactly(times));
            return this;
        }

        public HubVerifications totalRequestsCountIs(int times) {
            hub.verify(request(".*"), exactly(times));
            return this;
        }
    }

    private static HttpRequest newSessionRequest() {
        return request(WD_HUB_SESSION).withMethod("POST");
    }

    private static HttpRequest sessionRequest(String handler) {
        return request(WD_HUB_SESSION + "/" + SESSION_ID_REGEX + "/" + handler);
    }

    private static HttpRequest sessionQuitRequest() {
        return request(WD_HUB_SESSION +"/.*").withMethod("DELETE");
    }

    private HttpResponse emptyResponse() {
        JSONObject json = new JSONObject();
        json.put("value", new JSONObject());
        return response(json.toString());
    }


    private static HttpResponse newSessionSuccessful() {
        JSONObject json = new JSONObject();
        json.put("value", new JSONObject());
        json.put("sessionId", randomUUID());
        return response(json.toString());
    }

    private static HttpResponse newSessionFailed() {
        JSONObject json = new JSONObject();
        json.put("status", 6);
        JSONObject value = new JSONObject();
        value.put("message", "unable to start browser");
        json.put("value", value);
        return response(json.toString()).withStatusCode(500);
    }
}

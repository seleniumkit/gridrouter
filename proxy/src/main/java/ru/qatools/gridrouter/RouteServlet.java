package ru.qatools.gridrouter;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.qatools.gridrouter.caps.CapabilityProcessorFactory;
import ru.qatools.gridrouter.config.Host;
import ru.qatools.gridrouter.config.HostSelectionStrategy;
import ru.qatools.gridrouter.config.Region;
import ru.qatools.gridrouter.config.Version;
import ru.qatools.gridrouter.json.JsonCapabilities;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.JsonMessageFactory;
import ru.qatools.gridrouter.sessions.AvailableBrowserCheckExeption;
import ru.qatools.gridrouter.sessions.AvailableBrowsersChecker;
import ru.qatools.gridrouter.sessions.StatsCounter;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static ru.qatools.gridrouter.RequestUtils.getRemoteHost;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
@WebServlet(urlPatterns = {"/wd/hub/session"}, asyncSupported = true)
@ServletSecurity(value = @HttpConstraint(rolesAllowed = {"user"}))
public class RouteServlet extends SpringHttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServlet.class);

    private static final String ROUTE_TIMEOUT_CAPABILITY = "grid.router.route.timeout.seconds";

    private static final int MAX_ROUTE_TIMEOUT_SECONDS = 300;

    @Autowired
    private transient ConfigRepository config;
    @Autowired
    private transient HostSelectionStrategy hostSelectionStrategy;
    @Autowired
    private transient StatsCounter statsCounter;
    @Autowired
    private transient CapabilityProcessorFactory capabilityProcessorFactory;
    @Autowired
    private transient AvailableBrowsersChecker avblBrowsersChecker;

    @Value("${grid.router.route.timeout.seconds:120}")
    private int routeTimeout;

    private AtomicLong requestCounter = new AtomicLong();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        JsonMessage message = JsonMessageFactory.from(request.getInputStream());

        long requestId = requestCounter.getAndIncrement();
        int routeTimeout = getRouteTimeout(request.getRemoteUser(), message);
        AtomicBoolean terminated = new AtomicBoolean(false);
        executor.submit(getRouteCallable(request, message, response, requestId, routeTimeout, terminated));
        executor.shutdown();
        try {
            executor.awaitTermination(routeTimeout, TimeUnit.SECONDS);
            terminated.set(true);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        replyWithError("Timed out when searching for valid host", response);
    }

    private Callable<Object> getRouteCallable(HttpServletRequest request, JsonMessage message, HttpServletResponse response,
                                              long requestId, int routeTimeout, AtomicBoolean terminated) {
        return () -> {
            route(request, message, response, requestId, routeTimeout, terminated);
            return null;
        };
    }

    private int getRouteTimeout(String user, JsonMessage message) {
        JsonCapabilities caps = message.getDesiredCapabilities();
        try {
            if (caps.any().containsKey(ROUTE_TIMEOUT_CAPABILITY)) {
                Integer desiredRouteTimeout = Integer.valueOf(String.valueOf(caps.any().get(ROUTE_TIMEOUT_CAPABILITY)));
                if (desiredRouteTimeout > MAX_ROUTE_TIMEOUT_SECONDS) {
                    return desiredRouteTimeout;
                }
                LOGGER.warn("[{}] [INVALID_ROUTE_TIMEOUT] [{}]", user, desiredRouteTimeout);
            }
        } catch (NumberFormatException ignored) {
        }
        return routeTimeout;
    }

    private void route(HttpServletRequest request, JsonMessage message,
                       HttpServletResponse response,
                       long requestId, int routeTimeout, AtomicBoolean terminated) throws IOException {


        long initialSeconds = Instant.now().getEpochSecond();

        JsonCapabilities caps = message.getDesiredCapabilities();

        String user = request.getRemoteUser();
        String remoteHost = getRemoteHost(request);
        String browser = caps.describe();
        Version actualVersion = config.findVersion(user, caps);

        if (actualVersion == null) {
            LOGGER.warn("[{}] [UNSUPPORTED_BROWSER] [{}] [{}] [{}]", requestId, user, remoteHost, browser);
            replyWithError(format("Cannot find %s capabilities on any available node",
                    caps.describe()), response);
            return;
        }

        caps.setVersion(actualVersion.getNumber());

        capabilityProcessorFactory.getProcessor(caps).process(caps);

        List<Region> allRegions = actualVersion.getRegions()
                .stream().map(Region::copy).collect(toList());
        List<Region> unvisitedRegions = new ArrayList<>(allRegions);

        int attempt = 0;
        JsonMessage hubMessage = null;
        try (CloseableHttpClient client = newHttpClient(routeTimeout * 1000)) {
            if (actualVersion.getPermittedCount() != null) {
                avblBrowsersChecker.ensureFreeBrowsersAvailable(user, remoteHost, browser, actualVersion);
            }

            while (!allRegions.isEmpty() && !terminated.get()) {
                attempt++;

                Region currentRegion = hostSelectionStrategy.selectRegion(allRegions, unvisitedRegions);
                Host host = hostSelectionStrategy.selectHost(currentRegion.getHosts());

                String route = host.getRoute();
                try {
                    LOGGER.info("[{}] [SESSION_ATTEMPTED] [{}] [{}] [{}] [{}] [{}]", requestId, user, remoteHost, browser, route, attempt);

                    String target = route + request.getRequestURI();
                    HttpResponse hubResponse = client.execute(post(target, message));
                    hubMessage = JsonMessageFactory.from(hubResponse.getEntity().getContent());

                    if (hubResponse.getStatusLine().getStatusCode() == SC_OK) {
                        String sessionId = hubMessage.getSessionId();
                        hubMessage.setSessionId(host.getRouteId() + sessionId);
                        replyWithOk(hubMessage, response);
                        long createdDurationSeconds = Instant.now().getEpochSecond() - initialSeconds;
                        LOGGER.info("[{}] [{}] [SESSION_CREATED] [{}] [{}] [{}] [{}] [{}] [{}]",
                                requestId, createdDurationSeconds, user, remoteHost, browser, route, sessionId, attempt);
                        statsCounter.startSession(hubMessage.getSessionId(), user, caps.getBrowserName(), actualVersion.getNumber(), route);
                        return;
                    }
                    LOGGER.warn("[{}] [SESSION_FAILED] [{}] [{}] [{}] [{}] - {}",
                            requestId, user, remoteHost, browser, route, hubMessage.getErrorMessage());
                } catch (JsonProcessingException exception) {
                    LOGGER.error("[{}] [BAD_HUB_JSON] [{}] [{}] [{}] [{}] - {}", "",
                            requestId, user, remoteHost, browser, route, exception.getMessage());
                } catch (IOException exception) {
                    LOGGER.error("[{}] [HUB_COMMUNICATION_FAILURE] [{}] [{}] [{}] - {}",
                            requestId, user, remoteHost, browser, route, exception.getMessage());
                }

                currentRegion.getHosts().remove(host);
                if (currentRegion.getHosts().isEmpty()) {
                    allRegions.remove(currentRegion);
                }

                unvisitedRegions.remove(currentRegion);
                if (unvisitedRegions.isEmpty()) {
                    unvisitedRegions = new ArrayList<>(allRegions);
                }
            }
        } catch (AvailableBrowserCheckExeption e) {
            LOGGER.error("[{}] [AVAILABLE_BROWSER_CHECK_ERROR] [{}] [{}] [{}] - {}",
                    requestId, user, remoteHost, browser, e.getMessage());
        }

        LOGGER.error("[{}] [SESSION_NOT_CREATED] [{}] [{}] [{}]", requestId, user, remoteHost, browser);
        if (hubMessage == null) {
            replyWithError("Cannot create session on any available node", response);
        } else {
            replyWithError(hubMessage, response);
        }
    }


    protected void replyWithOk(JsonMessage message, HttpServletResponse response) throws IOException {
        reply(SC_OK, message, response);
    }

    protected void replyWithError(String errorMessage, HttpServletResponse response) throws IOException {
        replyWithError(JsonMessageFactory.error(13, errorMessage), response);
    }

    protected void replyWithError(JsonMessage message, HttpServletResponse response) throws IOException {
        reply(SC_INTERNAL_SERVER_ERROR, message, response);
    }

    protected void reply(int code, JsonMessage message, HttpServletResponse response) throws IOException {
        response.setStatus(code);
        response.setContentType(APPLICATION_JSON.toString());
        String messageRaw = message.toJson();
        response.setContentLength(messageRaw.getBytes(UTF_8).length);
        try (OutputStream output = response.getOutputStream()) {
            IOUtils.write(messageRaw, output, UTF_8);
        }
    }

    protected HttpPost post(String target, JsonMessage message) throws IOException {
        HttpPost method = new HttpPost(target);
        StringEntity entity = new StringEntity(message.toJson(), APPLICATION_JSON);
        method.setEntity(entity);
        method.setHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        return method;
    }

    protected CloseableHttpClient newHttpClient(int maxTimeout) {
        return HttpClientBuilder.create().setDefaultRequestConfig(
                RequestConfig.custom()
                        .setConnectionRequestTimeout(10000)
                        .setConnectTimeout(10000)
                        .setSocketTimeout(maxTimeout)
                        .build()
        ).setRedirectStrategy(new LaxRedirectStrategy()).disableAutomaticRetries().build();
    }
}
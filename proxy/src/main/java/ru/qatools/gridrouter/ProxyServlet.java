package ru.qatools.gridrouter;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.JsonMessageFactory;
import ru.qatools.gridrouter.sessions.StatsCounter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnServletContext;
import static ru.qatools.gridrouter.JsonWireUtils.*;
import static ru.qatools.gridrouter.RequestUtils.getRemoteHost;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
@WebServlet(
        urlPatterns = {WD_HUB_SESSION + "*"},
        asyncSupported = true,
        initParams = {
                @WebInitParam(name = "timeout", value = "300000"),
                @WebInitParam(name = "idleTimeout", value = "300000")
        }
)
public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServlet.class);

    @Autowired
    private transient ConfigRepository config;

    @Autowired
    private transient StatsCounter statsCounter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void sendProxyRequest(
            HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Request proxyRequest) {
        try {
            Request request = getRequestWithoutSessionId(clientRequest, proxyRequest);
            super.sendProxyRequest(clientRequest, proxyResponse, request);
        } catch (Exception exception) {
            LOGGER.error("[REQUEST_READ_FAILURE] [{}] - could not read client request, proxying request as is",
                    clientRequest.getRemoteHost(), exception);
            super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
        }
    }

    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();

        String remoteHost = getRemoteHost(request);

        if (!isUriValid(uri)) {
            LOGGER.warn("[INVALID_SESSION_HASH] [{}] - request uri is {}", remoteHost, uri);
            return null;
        }

        String route = config.getRoute(getSessionHash(uri));
        String command = getCommand(uri);

        if (route == null) {
            LOGGER.error("[ROUTE_NOT_FOUND] [{}] - request uri is {}", remoteHost, uri);
            return null;
        }

        if (isSessionDeleteRequest(request, command)) {
            LOGGER.info("[SESSION_DELETED] [{}] [{}] [{}]", remoteHost, route, command);
            statsCounter.deleteSession(getFullSessionId(uri), route);
        } else {
            statsCounter.updateSession(getFullSessionId(uri), route);
        }

        try {
            return redirectionUrl(route, command);
        } catch (Exception exception) {
            LOGGER.error("[REDIRECTION_URL_ERROR] [{}] - error building redirection uri because of {}\n"
                            + "    request uri:    {}\n"
                            + "    parsed route:   {}\n"
                            + "    parsed command: {}",
                    remoteHost, exception.toString(), uri, route,  command);
        }
        return null;
    }

    protected Request getRequestWithoutSessionId(HttpServletRequest clientRequest, Request proxyRequest) throws IOException {
        String content = IOUtils.toString(clientRequest.getInputStream(), UTF_8);
        if (!content.isEmpty()) {
            String remoteHost = getRemoteHost(clientRequest);
            content = removeSessionIdSafe(content, remoteHost);
        }
        return proxyRequest.content(
                new StringContentProvider(clientRequest.getContentType(), content, UTF_8));
    }

    private String removeSessionIdSafe(String content, String remoteHost) {
        try {
            JsonMessage message = JsonMessageFactory.from(content);
            message.setSessionId(null);
            return message.toJson();
        } catch (IOException exception) {
            LOGGER.error("[UNABLE_TO_REMOVE_SESSION_ID] [{}] - could not create proxy request without session id, "
                            + "proxying request as is. Request content is: {}",
                    remoteHost, content, exception);
        }
        return content;
    }
}

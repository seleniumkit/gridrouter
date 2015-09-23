package ru.qatools.gridrouter;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.qatools.gridrouter.json.GridStats;
import ru.qatools.gridrouter.json.JsonMessage;
import ru.qatools.gridrouter.json.JsonMessageFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnServletContext;
import static ru.qatools.gridrouter.RequestUtils.getRemoteHost;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
@WebServlet(
        urlPatterns = {ProxyServlet.WD_HUB_SESSION + "*"},
        asyncSupported = true,
        initParams = {
                @WebInitParam(name = "maxThreads", value = "512"),
                @WebInitParam(name = "timeout", value = "300000"),
                @WebInitParam(name = "idleTimeout", value = "300000")
        }
)
public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServlet.class);

    public static final String WD_HUB_SESSION = "/wd/hub/session/";

    public static final int SESSION_HASH_LENGTH = 32;

    @Autowired
    private ConfigRepository config;

    @Autowired
    private GridStats stats;

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
            LOGGER.error("[{}] [{}] - could not read client request, proxying request as is",
                    "REQUEST_READ_FAILURE", clientRequest.getRemoteHost(), exception);
            super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
        }
    }

    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();

        String remoteHost = getRemoteHost(request);
        
        if (!isUriValid(uri)) {
            LOGGER.warn("[{}] [{}] - request uri is {}", "INVALID_SESSION_HASH", remoteHost, uri);
            return null;
        }

        String route = getRoute(uri);
        String command = getCommand(uri);

        if (route == null) {
            LOGGER.error("[{}] [{}] - request uri is {}", "ROUTE_NOT_FOUND", remoteHost, uri);
            return null;
        }

        if (isSessionDeleteRequest(request, command)) {
            LOGGER.info("[{}] [{}] [{}] [{}]", "SESSION_DELETED", remoteHost, route, command);
            stats.stopSession();
        }

        try {
            return redirectionUrl(route, command);
        } catch (Exception exception) {
            LOGGER.error("[{}] [{}] - error building redirection uri because of {}\n"
                            + "    request uri:    {}\n"
                            + "    parsed route:   {}\n"
                            + "    parsed command: {}",
                    "REDIRECTION_URL_ERROR", remoteHost,
                    exception.toString(), uri, route,  command);
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
            LOGGER.error("[{}] [{}] - could not create proxy request without session id, "
                            + "proxying request as is. Request content is: {}",
                    "UNABLE_TO_REMOVE_SESSION_ID", remoteHost,
                    content,
                    exception);
        }
        return content;
    }

    protected String redirectionUrl(String host, String command) throws URISyntaxException {
        return new URIBuilder(host).setPath(WD_HUB_SESSION + command).build().toString();
    }

    protected String getRoute(String uri) {
        return config.getRoutes().get(getSessionHash(uri));
    }

    protected String getCommand(String uri) {
        return uri.substring(getUriPrefixLength());
    }

    protected boolean isUriValid(String uri) {
        return uri.length() > getUriPrefixLength();
    }

    protected boolean isSessionDeleteRequest(HttpServletRequest request, String command) {
        return DELETE.name().equalsIgnoreCase(request.getMethod())
                && !command.contains("/");
    }

    protected String getSessionHash(String uri) {
        return uri.substring(WD_HUB_SESSION.length(), getUriPrefixLength());
    }

    protected int getUriPrefixLength() {
        return WD_HUB_SESSION.length() + SESSION_HASH_LENGTH;
    }
}

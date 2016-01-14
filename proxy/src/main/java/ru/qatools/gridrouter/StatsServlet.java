package ru.qatools.gridrouter;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.qatools.gridrouter.json.JsonFormatter;
import ru.qatools.gridrouter.sessions.StatsCounter;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 */
@WebServlet(urlPatterns = {"/stats"}, asyncSupported = true)
@ServletSecurity(value = @HttpConstraint(rolesAllowed = {"user"}))
public class StatsServlet extends SpringHttpServlet {

    @Autowired
    private transient StatsCounter statsCounter;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(SC_OK);
        response.setContentType(APPLICATION_JSON_VALUE);
        try (OutputStream output = response.getOutputStream()) {
            IOUtils.write(JsonFormatter.toJson(
                    statsCounter.getStats(request.getRemoteUser())
            ), output, UTF_8);
        }
    }
}

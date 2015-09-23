package ru.qatools.gridrouter;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnServletContext;
import static ru.qatools.gridrouter.json.JsonFormatter.toJson;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@WebServlet(urlPatterns = {"/quota"}, asyncSupported = true)
@ServletSecurity(value = @HttpConstraint(rolesAllowed = {"user"}))
public class QuotaServlet extends HttpServlet {

    @Autowired
    private ConfigRepository config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(SC_OK);
        resp.setContentType(APPLICATION_JSON_VALUE);
        try (OutputStream output = resp.getOutputStream()) {
            String jsonResponse = toJson(config.getBrowsersCountMap(req.getRemoteUser()));
            IOUtils.write(jsonResponse, output, UTF_8);
        }
    }
}

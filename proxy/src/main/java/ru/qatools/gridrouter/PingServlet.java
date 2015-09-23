package ru.qatools.gridrouter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 */
@WebServlet(urlPatterns = {"/ping"}, asyncSupported = true)
public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter writer = resp.getWriter()) {
            writer.print("OK");
            writer.flush();
        }
    }
}

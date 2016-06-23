package ru.qatools.gridrouter.utils;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JettyRule implements TestRule {

    private final String contextPath;
    private final String classPath;
    private final String warPath;
    private final int port;
    private Server server;

    private Object[] beans;

    public JettyRule(String contextPath, String warPath, String classPath, int port, Object... beans) {
        this.contextPath = contextPath;
        this.classPath = classPath;
        this.warPath = warPath;
        this.port = port;
        this.beans = beans;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    protected void before() throws Exception {
        WebAppContext context = new WebAppContext();
        context.setResourceBase(warPath);
        context.setExtraClasspath(classPath);
        context.setContextPath(contextPath);
        context.setParentLoaderPriority(true);

        context.setConfigurations(new Configuration[]{
                new AnnotationConfiguration(),
                new WebXmlConfiguration(),
                new WebInfConfiguration()
        });

        server = new Server(port);
        server.setHandler(context);
        for (Object bean : beans) {
            server.addBean(bean);
        }
        server.start();
    }

    protected void after() throws Exception {
        server.stop();
    }

    public int getPort() {
        return port;
    }
}

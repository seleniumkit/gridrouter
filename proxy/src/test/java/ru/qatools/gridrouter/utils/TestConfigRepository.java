package ru.qatools.gridrouter.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.beanloader.BeanLoader;
import ru.qatools.gridrouter.ConfigRepository;
import ru.qatools.gridrouter.config.Browsers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static javax.xml.bind.JAXB.marshal;
import static javax.xml.bind.JAXB.unmarshal;

/**
 * @author Ilya Sadykov
 */
public class TestConfigRepository implements ConfigRepository {
    protected static final String XML_GLOB = "*.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigRepository.class);
    private static Map<String, Browsers> initialBrowsers = new HashMap<>();
    private static Map<String, String> initialRoutes = new HashMap<>();
    private static Map<String, Browsers> userBrowsers = new HashMap<>();
    private static Map<String, String> routes = new HashMap<>();

    static {
        try {
            final Path quotaDir = Paths.get(TestConfigRepository.class.getClassLoader().getResource("quota").toURI());
            LOGGER.debug("Loading quota configuration");
            initialBrowsers = new HashMap<>();
            initialRoutes = new HashMap<>();
            BeanLoader.loadAll(Browsers.class, quotaDir, XML_GLOB, (path, quota) -> {
                String user = FilenameUtils.getBaseName(path.toString());
                initialBrowsers.put(user, quota);
                initialRoutes.putAll(quota.getRoutesMap());
            });
            initialBrowsers = unmodifiableMap(initialBrowsers);
            initialRoutes = unmodifiableMap(initialRoutes);
            resetConfig();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Quota configuration loading failed", e);
        }
    }

    private static Browsers copy(Browsers quota) {
        StringWriter writer = new StringWriter();
        marshal(quota, writer);
        return unmarshal(new StringReader(writer.toString()), Browsers.class);
    }


    public static synchronized void resetConfig() {
        userBrowsers.clear();
        initialBrowsers.entrySet().forEach(e -> {
            userBrowsers.put(e.getKey(), copy(e.getValue()));
        });
        routes.clear();
        routes.putAll(initialRoutes);
    }

    public static synchronized void changePort(int from, int to) {
        userBrowsers.keySet().forEach(quotaName ->
                userBrowsers.get(quotaName).getBrowsers().forEach(browser ->
                        browser.getVersions().forEach(version ->
                                version.getRegions().forEach(region ->
                                        region.getHosts().forEach(host -> {
                                            if (host.getPort() == from) {
                                                LOGGER.info("Changing port of {} from {} to {} for user {}",
                                                        host, from, to, quotaName);
                                                host.setPort(to);
                                                routes.putAll(userBrowsers.get(quotaName).getRoutesMap());
                                            }
                                        })))));
    }

    @Override
    public Map<String, Browsers> getQuotaMap() {
        return userBrowsers;
    }

    @Override
    public String getRoute(String routeId) {
        return routes.get(routeId);
    }

}

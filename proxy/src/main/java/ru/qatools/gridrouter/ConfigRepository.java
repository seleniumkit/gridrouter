package ru.qatools.gridrouter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.qatools.beanloader.BeanChangeListener;
import ru.qatools.beanloader.BeanLoader;
import ru.qatools.beanloader.BeanWatcher;
import ru.qatools.gridrouter.config.Browser;
import ru.qatools.gridrouter.config.Browsers;
import ru.qatools.gridrouter.config.Version;
import ru.qatools.gridrouter.json.JsonCapabilities;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Repository
public class ConfigRepository implements BeanChangeListener<Browsers> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepository.class);

    private static final String XML_GLOB = "*.xml";

    @Value("${grid.config.quota.directory}")
    private File quotaDirectory;

    @Value("${grid.config.quota.hotReload}")
    private boolean quotaHotReload;

    private Map<String, Browsers> userBrowsers = new HashMap<>();

    private Map<String, String> routes = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            if (quotaHotReload) {
                LOGGER.debug("Starting quota watcher");
                BeanWatcher.watchFor(Browsers.class, quotaDirectory.toPath(), XML_GLOB, this);
            } else {
                reload();
            }
        } catch (IOException e) {
            LOGGER.error("Quota configuration loading failed", e);
        }
    }

    public void reload() throws IOException {
        LOGGER.debug("Loading quota configuration");
        BeanLoader.loadAll(Browsers.class, quotaDirectory.toPath(), XML_GLOB, this);
    }

    @Override
    public void beanChanged(Path filename, Browsers browsers) {
        if (browsers == null) {
            LOGGER.info("Configuration file [{}] was deleted. "
                    + "It is not purged from the running gridrouter process though.", filename);
        } else {
            LOGGER.info("Loading quota configuration file [{}]", filename);
            String user = FilenameUtils.getBaseName(filename.toString());
            userBrowsers.put(user, browsers);
            routes.putAll(browsers.getRoutesMap());
            LOGGER.info("Loaded quota configuration for [{}]: \n\n{}", user, browsers.toXml());
        }
    }

    public void updateBrowsers(Browsers newBrowsers) {
        LOGGER.info("Got new browsers list, updating all quotas. New browsers list is:\n\n{}", newBrowsers.toXml());
        userBrowsers.values().stream().forEach(browsers -> browsers.update(newBrowsers));

        StringBuilder builder = new StringBuilder();
        userBrowsers.entrySet().stream().forEach(e ->
                builder.append(e.getKey()).append(":\n").append(e.getValue().toXml()).append("\n"));
        LOGGER.info("Updated quotas, now they are the following:\n\n{}", builder.toString());
    }

    public Map<String, String> getRoutes() {
        return routes;
    }

    // TODO refact with Optional
    public Version findVersion(String user, JsonCapabilities caps) {
        return userBrowsers.get(user).find(caps.getBrowserName(), caps.getVersion());
    }

    public Map<String, Integer> getBrowsersCountMap(String user) {
        Map<String, Integer> countMap = new HashMap<>();
        for (Browser browser : userBrowsers.get(user).getBrowsers()) {
            for (Version version : browser.getVersions()) {
                countMap.put(browser.getName() + ":" + version.getNumber(), version.getCount());
            }
        }
        return countMap;
    }
}

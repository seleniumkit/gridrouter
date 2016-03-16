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
                LOGGER.debug("Loading quota configuration");
                BeanLoader.loadAll(Browsers.class, quotaDirectory.toPath(), XML_GLOB, this);
            }
        } catch (IOException e) {
            LOGGER.error("Quota configuration loading failed", e);
        }
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
            LOGGER.info("Loaded quota configuration for [{}] from [{}]: \n\n{}",
                    user, filename, browsers.toXml());
        }
    }

    public Map<String, Browsers> getQuotaMap() {
        return userBrowsers;
    }

    public String getRoute(String routeId) {
        return routes.get(routeId);
    }

    public Version findVersion(String user, JsonCapabilities caps) {
        final Browsers browsers = userBrowsers.get(user);
        return browsers != null ? browsers.find(caps.getBrowserName(), caps.getVersion()) : null;
    }

    public Map<String, Integer> getBrowsersCountMap(String user) {
        HashMap<String, Integer> countMap = new HashMap<>();
        for (Browser browser : this.userBrowsers.get(user).getBrowsers()) {
            for (Version version : browser.getVersions()) {
                countMap.put(browser.getName() + ":" + version.getNumber(), version.getCount());
            }
        }
        return countMap;
    }
}

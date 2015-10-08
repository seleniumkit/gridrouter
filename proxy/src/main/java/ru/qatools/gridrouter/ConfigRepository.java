package ru.qatools.gridrouter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.qatools.beanloader.BeanChangeListener;
import ru.qatools.beanloader.BeanWatcher;
import ru.qatools.gridrouter.config.Browser;
import ru.qatools.gridrouter.config.Browsers;
import ru.qatools.gridrouter.config.Version;
import ru.qatools.gridrouter.json.JsonCapabilities;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.newDirectoryStream;

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
    public void init() throws JAXBException, IOException {
        if (isQuotaHotReload()) {
            startQuotaWatcher();
        } else {
            loadQuotaOnce(getQuotaPath());
        }
    }

    private void startQuotaWatcher() {
        LOGGER.debug("Starting quota watcher");
        try {
            BeanWatcher.watchFor(Browsers.class, getQuotaPath().toString(), XML_GLOB, this);
        } catch (IOException e) {
            LOGGER.error("Quota configuration loading failed: \n\n{}", e);
        }
    }

    private void loadQuotaOnce(Path quotaPath) throws IOException {
        for (Path filename : newDirectoryStream(quotaPath, XML_GLOB)) {
            beanChanged(filename, JAXB.unmarshal(filename.toFile(), Browsers.class));
        }
    }

    @Override
    public void beanChanged(Path filename, Browsers browsers) {
        if (browsers == null) {
            LOGGER.info("Configuration file [{}] was deleted. "
                    + "It is not purged from the running gridrouter process though.", filename);
        } else {
            LOGGER.info("Loading quota configuration file [{}]", filename);
            String user = getFileName(filename);
            userBrowsers.put(user, browsers);
            routes.putAll(browsers.getRoutesMap());
            LOGGER.info("Loaded quota configuration for [{}] from [{}]: \n\n{}",
                    user, filename, browsers.toXml());
        }
    }

    protected boolean isQuotaHotReload() {
        return quotaHotReload;
    }

    protected Path getQuotaPath() {
        return quotaDirectory.toPath();
    }

    public Map<String, String> getRoutes() {
        return routes;
    }

    public Map<String, Browsers> getUserBrowsers() {
        return this.userBrowsers;
    }

    protected Browsers getUserBrowsers(String user) {
        return getUserBrowsers().get(user);
    }

    public Version findVersion(String user, JsonCapabilities caps) {
        return userBrowsers.get(user).find(caps.getBrowserName(), caps.getVersion());
    }

    private static String getFileName(Path path) {
        return FilenameUtils.getBaseName(path.toString());
    }

    public Map<String, Integer> getBrowsersCountMap(String user) {
        HashMap<String, Integer> countMap = new HashMap<>();
        for (Browser browser : getUserBrowsers(user).getBrowsers()) {
            for (Version version : browser.getVersions()) {
                countMap.put(browser.getName() + ":" + version.getNumber(), version.getCount());
            }
        }
        return countMap;
    }
}

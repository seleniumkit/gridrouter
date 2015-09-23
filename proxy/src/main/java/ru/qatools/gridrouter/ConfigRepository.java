package ru.qatools.gridrouter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.qatools.gridrouter.config.Browser;
import ru.qatools.gridrouter.config.Browsers;
import ru.qatools.gridrouter.config.Version;
import ru.qatools.gridrouter.json.JsonCapabilities;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.newDirectoryStream;
import static ru.qatools.gridrouter.utils.DirectoryWatcher.newWatcher;

/**
 * @author Alexander Andyashin aandryashin@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Repository
public class ConfigRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepository.class);

    public static final String XML_GLOB = "*.xml";

    @Value("${grid.config.quota.directory}")
    private File quotaDirectory;

    @Value("${grid.config.quota.hotReload}")
    private boolean quotaHotReload;

    private Map<String, Browsers> userBrowsers = new HashMap<>();

    private Map<String, String> routes = new HashMap<>();

    private Thread quotaWatcherThread;

    @PostConstruct
    public void init() throws JAXBException, IOException {
        initBrowsers(getQuotaPath());
        if (isQuotaHotReload()) {
            startQuotaWatcher();
        }
    }

    @PreDestroy
    public void destroy() {
        if (isQuotaHotReload()) {
            stopQuotaWatcher();
        }
    }

    private void startQuotaWatcher() {
        LOGGER.debug("Starting quota watcher");
        setQuotaWatcherThread(newWatcher(getQuotaPath(), "glob:" + XML_GLOB, (kind, browserPath) -> {
            LOGGER.info("Reload configuration [{}] on event [{}]", browserPath, kind.name());
            initBrowsers(getQuotaPath());
        }));
        getQuotaWatcherThread().start();

    }

    private void stopQuotaWatcher() {
        LOGGER.debug("Stopping quota watcher");
        if (getQuotaWatcherThread() != null && getQuotaWatcherThread().isAlive()) {
            getQuotaWatcherThread().interrupt();
        }
    }

    public void initBrowsers(Path quotaPath) {
        Map<String, Browsers> temporaryUserBrowsers = new HashMap<>(getUserBrowsers());
        Map<String, String> temporaryRoutes = new HashMap<>(getRoutes());
        try (DirectoryStream<Path> stream = newDirectoryStream(quotaPath, XML_GLOB)) {
            for (Path browsersPath : stream) {
                LOGGER.info("Load configuration from [{}]", browsersPath);
                String user = getFileName(browsersPath);
                try {
                    Browsers browsers = JAXB.unmarshal(browsersPath.toFile(), Browsers.class);
                    temporaryUserBrowsers.put(user, browsers);
                    temporaryRoutes.putAll(browsers.getRoutesMap());

                    LOGGER.info("Loaded configuration for [{}] from [{}]: \n\n{}",
                            user, browsersPath, browsers.toXml());
                } catch (Exception e) {
                    LOGGER.error("Loaded configuration failed for [{}]: \n\n{}", browsersPath, e);
                }
            }
            setUserBrowsers(temporaryUserBrowsers);
            setRoutes(temporaryRoutes);
        } catch (IOException e) {
            LOGGER.error("Loaded configuration failed: \n\n{}", e);
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

    protected void setRoutes(Map<String, String> routes) {
        this.routes = routes;
    }

    public Map<String, Browsers> getUserBrowsers() {
        return this.userBrowsers;
    }

    protected Browsers getUserBrowsers(String user) {
        return getUserBrowsers().get(user);
    }

    protected void setUserBrowsers(Map<String, Browsers> userBrowsers) {
        this.userBrowsers = userBrowsers;
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

    public Thread getQuotaWatcherThread() {
        return quotaWatcherThread;
    }

    public void setQuotaWatcherThread(Thread quotaWatcherThread) {
        this.quotaWatcherThread = quotaWatcherThread;
    }

}

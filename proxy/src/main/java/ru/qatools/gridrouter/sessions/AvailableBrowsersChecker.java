package ru.qatools.gridrouter.sessions;

import ru.qatools.gridrouter.config.Version;

/**
 * @author Ilya Sadykov
 */
public interface AvailableBrowsersChecker {
    /**
     * Blocks or throws an exception if there is no browsers available for user
     */
    void ensureFreeBrowsersAvailable(String user, String remoteHost, String browser, Version actualVersion);
}

package ru.qatools.gridrouter.sessions;

import ru.qatools.gridrouter.config.Version;

/**
 * @author Ilya Sadykov
 */
public class SkipAvailableBrowsersChecker implements AvailableBrowsersChecker {
    @Override
    public void ensureFreeBrowsersAvailable(String user, String remoteHost, String browser, Version actualVersion) {
        // do nothing
    }
}

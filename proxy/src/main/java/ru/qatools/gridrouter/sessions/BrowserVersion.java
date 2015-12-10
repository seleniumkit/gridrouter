package ru.qatools.gridrouter.sessions;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class BrowserVersion {

    private final String browser;
    private final String version;

    public BrowserVersion(String browser, String version) {
        this.browser = browser;
        this.version = version;
    }

    public String getBrowser() {
        return browser;
    }

    public String getVersion() {
        return version;
    }
}

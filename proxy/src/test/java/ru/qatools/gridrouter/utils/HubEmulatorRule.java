package ru.qatools.gridrouter.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class HubEmulatorRule extends TestWatcher {

    private HubEmulator hub;

    public HubEmulatorRule(int hubPort) {
        hub = new HubEmulator(hubPort);
    }

    @Override
    protected void finished(Description description) {
        hub.stop();
    }

    public HubEmulator.HubEmulations emulate() {
        return hub.emulate();
    }

    public HubEmulator.HubVerifications verify() {
        return hub.verify();
    }
}

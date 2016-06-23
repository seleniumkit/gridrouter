package ru.qatools.gridrouter.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static ru.qatools.gridrouter.utils.SocketUtil.findFreePort;
import static ru.qatools.gridrouter.utils.TestConfigRepository.changePort;
import static ru.qatools.gridrouter.utils.TestConfigRepository.resetConfig;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class HubEmulatorRule extends TestWatcher {
    static final Logger LOGGER = LoggerFactory.getLogger(HubEmulatorRule.class);
    private int fromPort;
    private int port;
    private HubEmulator hub;

    public HubEmulatorRule(int fromPort) {
        this(fromPort, hub -> {
        });
    }

    public HubEmulatorRule(int fromPort, Consumer<HubEmulator> initializer) {
        this.fromPort = fromPort;
        port = findFreePort();
        LOGGER.info("Selected new free port {}, starting emulator...", port);
        hub = new HubEmulator(port);
        if (initializer != null) {
            LOGGER.info("Running initializer...");
            try {
                initializer.accept(hub);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Waiting for config initialization...");
        changePort(fromPort, port);
    }

    @Override
    protected void finished(Description description) {
        resetConfig();
        hub.stop();
    }

    public HubEmulator.HubEmulations emulate() {
        return hub.emulate();
    }

    public HubEmulator.HubVerifications verify() {
        return hub.verify();
    }

    public int getPort() {
        return port;
    }
}

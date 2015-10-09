package ru.qatools.gridrouter.caps;

import ru.qatools.gridrouter.json.JsonCapabilities;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface CapabilityProcessor {

    boolean accept(JsonCapabilities caps);

    void process(JsonCapabilities caps);
}

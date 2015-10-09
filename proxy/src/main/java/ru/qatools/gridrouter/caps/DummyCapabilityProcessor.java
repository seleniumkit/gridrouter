package ru.qatools.gridrouter.caps;

import ru.qatools.gridrouter.json.JsonCapabilities;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class DummyCapabilityProcessor implements CapabilityProcessor {

    @Override
    public boolean accept(JsonCapabilities caps) {
        throw new UnsupportedOperationException("Method DummyCapabilityProcessor::accept should never be called");
    }

    @Override
    public void process(JsonCapabilities caps) {
    }
}

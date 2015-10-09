package ru.qatools.gridrouter.caps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.qatools.gridrouter.json.JsonCapabilities;

import java.util.List;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Component
public class CapabilityProcessorFactory {

    @Autowired
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<CapabilityProcessor> processors;

    public CapabilityProcessor getProcessor(JsonCapabilities caps) {
        return processors.stream()
                .filter(p -> p.accept(caps))
                .findFirst()
                .orElse(new DummyCapabilityProcessor());
    }
}

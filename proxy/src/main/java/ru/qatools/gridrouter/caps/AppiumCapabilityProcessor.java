package ru.qatools.gridrouter.caps;

import org.springframework.stereotype.Service;
import ru.qatools.gridrouter.json.JsonCapabilities;

import java.util.Map;

/**
 * <p>
 * Sets "keepKeyChains" capability for Mac sessions.
 * </p>
 *
 * @author Ivan Krutov vania-pooh@yandex-team.ru
 * 
 */
@SuppressWarnings("JavadocReference")
@Service
public class AppiumCapabilityProcessor implements CapabilityProcessor {

    private static final String PLATFORM_NAME = "platformName";
    private static final String IOS = "iOS";
    
    @Override
    public boolean accept(JsonCapabilities caps) {
        return caps.getBrowserName().isEmpty() && isMac(caps);
    }

    @Override
    public void process(JsonCapabilities caps) {
        caps.any().put("keepKeyChains", true);
    }
    
    private boolean isMac(JsonCapabilities jsonCapabilities) {
        Map<String, Object> capsMap = jsonCapabilities.any();
        return 
                capsMap.containsKey(PLATFORM_NAME) &&
                String.valueOf(capsMap.get(PLATFORM_NAME)).contains(IOS);
                
    }
}

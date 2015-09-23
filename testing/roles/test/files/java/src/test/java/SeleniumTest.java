import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SeleniumTest {

    @Test
    public void testConnection() throws Exception {
        URL url = new URL("http://selenium:selenium@gridrouter:8080/wd/hub");
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setVersion("38.0");
        WebDriver driver = new RemoteWebDriver(url, capabilities);
        driver.get("http://www.yandex.ru");
        assertThat(driver.getTitle(), equalTo("Яндекс"));
    }
}

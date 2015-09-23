from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities

class TestSelenium:
    def test_selenium(self):
        driver = webdriver.Remote(
            command_executor='http://selenium:selenium@gridrouter:8080/wd/hub',
            desired_capabilities=DesiredCapabilities.CHROME)

        driver.get('http://www.yandex.ru')
        assert driver.title != ''
        driver.close()

var assert = require('assert'),
    config = require('../config.json'),
    WebDriver = require('webdriver-http-sync');

describe('"webdriver-http-sync" selenium client for js', function () {
    it('should work', function () {

        this.timeout(60000);

        var driver = new WebDriver(config.baseUrl, {
            browserName: 'firefox',
            version: '38'
        });
        driver.navigateTo('http://yandex.ru');
        var title = driver.getPageTitle();
        assert.equal(title, 'Яндекс');
        driver.close();
    });
});

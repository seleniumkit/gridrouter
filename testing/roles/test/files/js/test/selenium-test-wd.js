'use strict';
var fs = require('fs'),
    assert = require('assert'),
    config = require('../config.json'),
    wd = require('wd');

describe('"wd" selenium client for js', function () {
    var driver;
    beforeEach(function() {
        this.timeout(60000);
        return driver = wd.promiseChainRemote(config.baseUrl)
            .init({
                browserName: 'chrome',
                version: '43'
            })
            .get('http://yandex.ru');
    });

    it('should work', function () {
        return driver.title().then(function (title) {
                assert.equal(title, 'Яндекс');
            })
            .fin(function () {
                return driver.quit();
            });
    });

    it('should push and evaluate big scripts', function() {
        return driver.execute(fs.readFileSync(__dirname + '/../fixtures/big-script.js', 'utf-8')).then(function(result) {
            assert.equal(result, 'ok');
        }).fin(function() {
            return driver.quit();
        });
    })
});

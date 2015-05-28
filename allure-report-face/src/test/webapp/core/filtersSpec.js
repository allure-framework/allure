/*global describe, it, beforeEach, afterEach, expect, spyOn, angular, inject, module */
describe('filter', function () {
    'use strict';
    beforeEach(module('allure.core.filters'));

    describe('interpolate', function () {
        beforeEach(module(function ($provide) {
            $provide.value('version', 'TEST_VER');
        }));


        it('should replace VERSION', inject(function (interpolateFilter) {
            expect(interpolateFilter('before %VERSION% after')).toEqual('before TEST_VER after');
        }));
    });

    describe('d3time', function() {
        it('should print only last time unit', inject(function(d3timeFilter) {
            expect(d3timeFilter(new Date(0))).toBe('0');
            expect(d3timeFilter(new Date(300))).toBe('300ms');
            expect(d3timeFilter(new Date(1000+300))).toBe('300ms');
            expect(d3timeFilter(new Date(1000))).toBe('1s');
            expect(d3timeFilter(new Date(61*1000))).toBe('1s');
            expect(d3timeFilter(new Date(60*1000))).toBe('1m');
            expect(d3timeFilter(new Date(60*60*1000))).toBe('1h');
            expect(d3timeFilter(new Date(25*60*60*1000))).toBe('25h');
            expect(d3timeFilter(new Date(25*60*60*1000+1))).toBe('1ms');
        }));
    });

    describe('time', function() {
        it('should format time', inject(function(timeFilter) {
            expect(timeFilter(false)).toBe('0');
            expect(timeFilter(null)).toBe('0');
            expect(timeFilter(0)).toBe('0');
            expect(timeFilter(345)).toBe('345ms');
            expect(timeFilter(1000)).toBe('1s');
            expect(timeFilter(1100)).toBe('1s 100ms');
            expect(timeFilter(60000)).toBe('1m 0s');
            expect(timeFilter(140000)).toBe('2m 20s');
            expect(timeFilter(4201000)).toBe('1h 10m');
            expect(timeFilter(3601000)).toBe('1h 0m');
            expect(timeFilter(25*3600*1000+62000)).toBe('25h 1m');
        }));
    });

    describe('linky', function() {
        function expandSce(sceValue) {
            return sceValue.$$unwrapTrustedValue();
        }
        it('should find and wrap external links', inject(function(linkyFilter) {
            expect(expandSce(linkyFilter('http://yandex.ru/'))).toBe('<a href="http://yandex.ru/" target="_blank">http://yandex.ru/</a>');
            expect(expandSce(linkyFilter('ssh://host'))).toBe('<a href="ssh://host" target="_blank">ssh://host</a>');
            expect(expandSce(linkyFilter('http://'))).toBe('<a href="http://" target="_blank">http://</a>');
        }));

        it('should pass text without links', inject(function(linkyFilter) {
            expect(expandSce(linkyFilter('nothing interesting'))).toBe('nothing interesting');
            expect(expandSce(linkyFilter('not//link'))).toBe('not//link');
        }));
    });
});

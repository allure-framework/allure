/*global describe:true, it:true, beforeEach:true, afterEach:true, expect:true, spyOn:true, module:true, inject:true, angular:true, jasmine:true */
describe('DefectsCtrl', function () {
    "use strict";
    var scope;

    function Defect(uid, message, testcases) {
        this.uid = uid;
        this.failure = {message: message};
        this.testCases = testcases;
    }

    jasmine.qatools.fakePluginApi();
    beforeEach(module('ui.router'));
    beforeEach(module('allure.defects'));
    beforeEach(function() {
        var self = this;
        module('allure.core.services', function($provide) {
            $provide.value('WatchingStore', function() {
                return self.WatchingStore = jasmine.createSpyObj('WatchingStore', ['bindProperty']);
            });
        });
    });
    beforeEach(inject(function ($controller, $rootScope) {
        this.$state = jasmine.createSpyObj('$state', ['go', 'is']);
        this.createController = function(defects) {
            var scope = $rootScope.$new();
            $controller('DefectsCtrl', {
                $scope: scope,
                $state: this.$state,
                defects: {defectsList: defects}
            });
            return scope;
        };
    }));

    describe('Both defect exists', function() {
        beforeEach(function () {
            scope = this.createController([{
                title: 'Test', status: 'BROKEN',
                defects: [
                    new Defect(1, 'FileNotFoundException: No file found', [{uid: 4}]),
                    new Defect(2, 'ArrayIndexOutOfBoundsException: 100500', [{uid: 5}])
                ]
            }, {
                title: 'Product', status: 'FAILED',
                defects: [
                    new Defect(3, 'Assertion Error: expected 3 to be 4', [{uid: 1}]),
                    new Defect(4, 'Assertion Error: expected true to be false', [{uid: 2}]),
                    new Defect(5, 'Assertion Error: expected value to be defined', [{uid: 3}])
                ]
            }]);
        });

        it('should not select testcase an defect by default', function () {
            scope.$broadcast('$stateChangeSuccess', null, {});
            expect(scope.testcase.uid).toBeUndefined();
            expect(scope.defect).toBeUndefined();
        });

        it('should find and select defect by uid', function () {
            scope.$broadcast('$stateChangeSuccess', null, {defectUid: 3});
            expect(scope.defect.uid).toBe(3);
        });

        it('should find and select testcase by uid', function () {
            scope.$broadcast('$stateChangeSuccess', null, {testcaseUid: 3});
            expect(scope.testcase.uid).toBe(3);
        });
    });

    describe("One defect is empty", function () {
        beforeEach(function () {
            scope = this.createController([{
                title: 'Test', status: 'BROKEN',
                defects: [
                    new Defect(1, 'FileNotFoundException: No file found', [{uid: 1}]),
                    new Defect(2, 'ArrayIndexOutOfBoundsException: 100500', [{uid: 2}])
                ]
            }, {
                title: 'Product', status: 'FAILED',
                defects: []
            }]);
        });

        it("should skip empty defect", function () {
            expect(scope.defects).toEqual([jasmine.objectContaining({title: 'Test'})]);
        });
    });

});

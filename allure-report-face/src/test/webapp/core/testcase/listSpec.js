/*global describe:true, it:true, beforeEach:true, afterEach:true, expect:true, spyOn:true, module:true, inject:true, angular:true, jasmine:true */
describe('Testcases list', function () {
    'use strict';
    var WatchingStore,
        Collection;

    beforeEach(module('allure.core.testcase.testcasesList', function($provide) {
        $provide.value('WatchingStore', function() {
            WatchingStore = jasmine.createSpyObj('WatchingStore', ['bindProperty']);
            return WatchingStore;
        });
        $provide.value('Collection', function() {
            Collection = jasmine.createSpyObj('Collection', ['filter', 'sort', 'limitTo', 'indexOf', 'getIndexBy', 'getNext', 'getPrevious']);
            Collection.getNext.andReturn({uid:2});
            Collection.getPrevious.andReturn({uid:0});
            return Collection;
        });
        $provide.value('status', {});
        $provide.value('severity', {});
    }));

    describe('TestCasesCtrl', function () {
        var $controller, $rootScope;

        beforeEach(inject(function (_$controller_, _$rootScope_) {
            $controller = _$controller_;
            $rootScope = _$rootScope_;
        }));

        function createController() {
            var scope = $rootScope.$new();
            scope.testcases = [
                {uid: 0, time:{start: 0}, status: 'PASSED'},
                {uid: 1, time:{start: 42}, status: 'PASSED'},
                {uid: 2, time:{start: 44}, status: 'BROKEN'},
                {uid: 3, time:{start: 33}, status: 'FAILED'}
            ];
            $controller('TestCasesCtrl', {
                $scope: scope
            });
            scope.showStatuses = {PASSED: false, BROKEN: true, FAILED: true, CANCELED: true, PENDING: false};
            $rootScope.$apply();
            return scope;
        }

        it('should bind sort settings to storage', function () {
            createController();
            expect(WatchingStore.bindProperty.calls.length).toBe(2);
        });

        it('should filter testcases by status', function() {
            var scope = createController();
            expect(scope.statusFilter({status: 'PASSED'})).toBe(false);
            expect(scope.statusFilter({status: 'FAILED'})).toBe(true);
            scope.showStatuses = {PASSED: true, BROKEN: true, FAILED: true, CANCELED: false, PENDING: false};
            expect(scope.statusFilter({status: 'PASSED'})).toBe(true);
        });

        it('should set order based on start time', function() {
            var scope = createController();
            expect(scope.testcases[0].order).toEqual(1);
            expect(scope.testcases[0].uid).toEqual(0);
            expect(scope.testcases[3].uid).toEqual(2);
            expect(scope.testcases[3].order).toEqual(4);
        });

        it('should find visible testcases count', function() {
            var scope = createController();
            expect(scope.getVisibleCount()).toBe(2);
        });

        it('should bind order, filter and limit', function() {
            createController();
            expect(Collection.filter).toHaveBeenCalled();
            expect(Collection.sort).toHaveBeenCalled();
            expect(Collection.limitTo).toHaveBeenCalled();
        });

        it('should bind filters again after change testcases list', function() {
            var scope = createController();
            scope.testcases = [
                {uid: 4, time:{start: 50}, status: 'FAILED'},
                {uid: 5, time:{start: 48}, status: 'PASSED'},
                {uid: 6, time:{start: 55}, status: 'BROKEN'},
                {uid: 7, time:{start: 64}, status: 'PASSED'}
            ];
            Collection.filter.reset();
            Collection.sort.reset();
            Collection.limitTo.reset();
            scope.$apply();
            expect(Collection.filter).toHaveBeenCalled();
            expect(Collection.sort).toHaveBeenCalled();
            expect(Collection.limitTo).toHaveBeenCalled();
        });

        it('should navigate down', function() {
            var scope = createController();
            scope.select(1);
            expect(Collection.getNext).toHaveBeenCalled();
        });

        it('should navigate up', function() {
            var scope = createController();
            scope.select(-1);
            expect(Collection.getPrevious).toHaveBeenCalled();
        });
    });

    describe('directive', function() {
        var testcases = [
                {uid: 0, time:{start: 0}, status: 'PASSED'},
                {uid: 1, time:{start: 42}, status: 'PASSED'},
                {uid: 2, time:{start: 44}, status: 'BROKEN'},
                {uid: 3, time:{start: 33}, status: 'FAILED'}
            ],
            showStatuses = {PASSED: false, BROKEN:true, FAILED:true},
            scope, elem;

        beforeEach(inject(function($templateCache) {
            $templateCache.put('templates/testcase/list.html',
                '<h4>{{getVisibleCount()}}</h4><table><tbody>' +
                    '<tr ng-repeat="testcase in testcases | orderBy:sorting.predicate:sorting.reverse | filter:statusFilter | limitTo:testcasesLimit" ng-click="selectTestcase(testcase)" data-uid="{{testcase.uid}}" ng-class="{active:testcase.uid==selectedUid}">' +
                        '<td>{{testcase.title}}</td><td>{{testcase.time.duration}}</td><td>{{testcase.status}}</td>' +
                    '</tr>' +
                '</table>'
            );
        }));
        beforeEach(inject(function ($compile, $rootScope) {
            scope = $rootScope.$new();
            scope.testcases = testcases;
            elem = angular.element('<div testcases-list="testcases" statuses="showStatuses" selected-uid="testcaseUid"></div>');
            $compile(elem)(scope);
            scope.$digest();
            scope.showStatuses = showStatuses;
            scope.$apply();
        }));

        it('should load testcases', function() {
            expect(elem.find('tr').length).toBe(2);
        });

        it('should apply view settings', function() {
            scope.$apply('showStatuses.PASSED=true');
            expect(elem.find('tr').length).toBe(4);
        });

        it('should activate selected testcase', function() {
            scope.$apply('testcaseUid=2');
            expect(elem.find('tr[data-uid=2]')).toHaveClass('active');
            expect(elem.find('tr.active').length).toBe(1);
        });

        it('should report about selected testcase', function() {
            elem.find('tr[data-uid=2]').click();
            scope.$apply();
            expect(scope.testcaseUid).toBe(2);
        });
    });
});

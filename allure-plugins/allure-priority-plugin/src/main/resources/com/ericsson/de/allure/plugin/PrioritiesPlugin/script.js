/*global angular*/
(function() {
    'use strict';
    var module = angular.module('allure.priorities', []);
    module.config(function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('priorities', {title: 'priorities.TITLE', icon: 'fa fa-flag-o', resolve: {
            defectsByPriorities: function($http) {
                return $http.get('data/priorities.json').then(function(r) {
                    return r.data;
                });
            }
        }});

        $stateProvider.state('priorities.suite', {
            url: "/:compositeUid"
        });
        $stateProvider.state('priorities.suite.expanded', {
            url: '/expanded'
        });

        testcaseProvider.attachStates('priorities.suite');
        allureTabsProvider.addTranslation('priorities');
        allureTabsProvider.addStylesheet('priorities');
    });

    module.controller('PrioritiesCtrl', function($scope, $state, status, WatchingStore, Collection, defectsByPriorities) {
        var priorities = ['BLOCKER', 'CRITICAL', 'NORMAL', 'MINOR', 'TRIVIAL'];

        $scope.defectsByPriorities = [];
        $scope.allTestCases = [];
        $scope.defectsCount = 0;
        priorities.forEach(function (priority) {
            if (!defectsByPriorities.hasOwnProperty(priority)) {
                return;
            }
            var defectsByPriority = defectsByPriorities[priority];
            $scope.defectsByPriorities.push(defectsByPriority);
            $scope.defectsCount += defectsByPriority.testCasesCount;
        });
        var store = new WatchingStore('defectsByPrioritiesSettings');

        $scope.isState = function(statename) {
            return $state.is(statename);
        };
        $scope.setSuite = function(compositeKey) {
            $state.go('priorities.suite', {compositeUid: compositeKey});
        };
        $scope.isActiveSuite = function(suite) {
            return $scope.suite === suite;
        };
        $scope.expandPriority = function(defect, expanded) {
            if(!expanded && defect.suitesMap.valueOf($scope.suite) !== undefined) {
                $state.go('priorities');
            }
            defect.expanded = expanded;
        };

        $scope.showStatuses = {};
        status.all.forEach(function(status) {
            $scope.showStatuses[status] = true;
        });

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'statistic.failed'),
            reverse: store.bindProperty($scope, 'sorting.reverse', false)
        };
        $scope.$watch('testcase.uid', function(testcaseUid, oldUid) {
            if (testcaseUid && testcaseUid !== oldUid) {
                $state.go('priorities.suite.testcase', {testcaseUid: testcaseUid});
            }
        });

        $scope.testcase = {};
        $scope.$on('$stateChangeSuccess', function(event, state, params) {
            delete $scope.suite;
            delete $scope.testcase.uid;

            if (params.compositeUid) {
                setSuite(params.compositeUid);
            }
            if (params.testcaseUid) {
                $scope.testcase.uid = params.testcaseUid;
            }
        });

        function setSuite(compositeUid) {
            $scope.suite = findSuite(compositeUid);
            $scope.defectsByPriorities.forEach(function(defectByPriority) {
                if (defectByPriority.suitesMap.hasOwnProperty(compositeUid)) {
                    defectByPriority.expanded = true;
                }
            });
        }

        function findSuite(compositeUid) {
            var keys = compositeUid.split(':'),
                priorityName = keys[0];
            var suite;

            $scope.defectsByPriorities.some(function(defectByPriority) {
                if (defectByPriority.severity !== priorityName) {
                    return undefined;
                }
                if (!defectByPriority.suitesMap.hasOwnProperty(compositeUid)) {
                    return undefined;
                }
                suite = defectByPriority.suitesMap[compositeUid];
                return suite;
            });
            //noinspection JSUnusedAssignment
            return suite;
        }

    });
})();

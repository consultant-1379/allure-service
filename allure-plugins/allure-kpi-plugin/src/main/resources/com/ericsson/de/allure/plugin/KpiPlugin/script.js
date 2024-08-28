/*global angular*/
(function() {
    'use strict';
    var module = angular.module('allure.kpi', []);
    module.config(function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('kpi', {title: 'kpi.TITLE', icon: 'fa fa-line-chart', resolve: {
            kpis: function($http) {
                return $http.get('data/kpi.json').then(function(r) {
                    return r.data;
                });
            }
        }});

        $stateProvider.state('kpi.suite', {
            url: "/:compositeUid"
        });
        $stateProvider.state('kpi.suite.expanded', {
            url: '/expanded'
        });

        testcaseProvider.attachStates('kpi.suite');
        allureTabsProvider.addTranslation('kpi');
        allureTabsProvider.addStylesheet('kpi');
    });

    module.controller('KpiCtrl', function($scope, $state, status, WatchingStore, Collection, kpis) {
        var kpiList = [];

        for (var kpi in kpis) {
            kpiList.push(kpi);
        }

        $scope.kpiList = kpiList;

        $scope.kpisById = [];
        $scope.allTestCases = [];

        kpiList.forEach(function (kpi) {
            var suiteByKpi = kpis[kpi];
            $scope.kpisById.push(suiteByKpi);
        });

        var store = new WatchingStore('kpiSettings');

        $scope.isState = function (statename) {
            return $state.is(statename);
        };

        $scope.setSuite = function(compositeKey) {
            $state.go('kpi.suite', {compositeUid: compositeKey});
        };
        $scope.isActiveSuite = function(suite) {
            return $scope.suite === suite;
        };


        $scope.expandkpi = function(kpi, expanded) {
            $scope.kpisById.forEach(function(kpiById) {
                kpiById.expanded = false;
             });
            $scope.lastClickedKpi=kpi.kpiType;
            if(!expanded) {
                $state.go('kpi');
            }
            kpi.expanded = expanded;
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'testCasesFailedCount'),
            reverse: store.bindProperty($scope, 'sorting.reverse', false)
        };
        $scope.$watch('testcase.uid', function(testcaseUid, oldUid) {
            if (testcaseUid && testcaseUid !== oldUid) {
                $state.go('kpi.suite.testcase', {testcaseUid: testcaseUid});
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

        $scope.headings = function() {
            $scope.kpisById.forEach(function(kpiById) {
                if(kpiById.kpiType == $scope.lastClickedKpi) {
                    $scope.kpiByType = kpiById;
                }
            });

            var obj = Object.values($scope.kpiByType.kpiValues)[0];
            return Object.keys(obj.kpiValues);
        }

        $scope.valueFromHeading = function(heading, map) {
            return map[heading];
        }

        function setSuite(compositeUid) {
            $scope.suite = findSuite(compositeUid);
            $scope.kpisById.forEach(function(kpi) {
                if (kpi.suitesMap.hasOwnProperty(compositeUid)) {
                    kpi.expanded = true;
                }
            });
        }

        function findSuite(compositeUid) {
            var keys = compositeUid.split(':'),
                kpi = keys[0];
            var suite;

            $scope.kpisById.some(function(kpi) {
                if (kpi.kpi !== kpi) {
                    return undefined;
                }
                if (!kpi.suitesMap.hasOwnProperty(compositeUid)) {
                    return undefined;
                }
                suite = kpi.suitesMap[compositeUid];
                return suite;
            });
            //noinspection JSUnusedAssignment
            return suite;
        }

    });
})();

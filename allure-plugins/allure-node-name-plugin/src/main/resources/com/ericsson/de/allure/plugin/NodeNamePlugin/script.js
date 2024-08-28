/*global angular*/
(function() {
    'use strict';
    var module = angular.module('allure.nodeName', []);
    module.config(function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('nodeName', {title: 'nodeName.TITLE', icon: 'fa fa-signal', resolve: {
            nodeNames: function($http) {
                return $http.get('data/nodeName.json').then(function(r) {
                    return r.data;
                });
            }
        }});

        $stateProvider.state('nodeName.suite', {
            url: "/:compositeUid"
        });
        $stateProvider.state('nodeName.suite.expanded', {
            url: '/expanded'
        });

        testcaseProvider.attachStates('nodeName.suite');
        allureTabsProvider.addTranslation('nodeName');
        allureTabsProvider.addStylesheet('nodeName');
    });

    module.controller('NodeNameCtrl', function($scope, $state, status, WatchingStore, Collection, nodeNames) {
        var nodeNameList = [];
        for (var nodeName in nodeNames) {
            nodeNameList.push(nodeName);
        }

        $scope.nodeNameList = nodeNameList;

        $scope.suitesByNodeName = [];
        $scope.allTestCases = [];

        nodeNameList.forEach(function (nodeName) {
            var suiteByNodeName = nodeNames[nodeName];
            $scope.suitesByNodeName.push(suiteByNodeName);
        });

        var store = new WatchingStore('nodeNameSettings');
        $scope.list = new Collection($scope.suitesByNodeName);

        $scope.isState = function (statename) {
            return $state.is(statename);
        };

        $scope.setSuite = function(compositeKey) {
            $state.go('nodeName.suite', {compositeUid: compositeKey});
        };
        $scope.isActiveSuite = function(suite) {
            return $scope.suite === suite;
        };

        $scope.expandnodeName = function(nodeName, expanded) {
            if(!expanded && nodeName.suitesMap.valueOf($scope.suite) !== undefined) {
                $state.go('nodeName');
            }
            nodeName.expanded = expanded;
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'testCasesFailedCount'),
            reverse: store.bindProperty($scope, 'sorting.reverse', true)
        };
        $scope.$watch('testcase.uid', function(testcaseUid, oldUid) {
            if (testcaseUid && testcaseUid !== oldUid) {
                $state.go('nodeName.suite.testcase', {testcaseUid: testcaseUid});
            }
        });

        $scope.$watch('showStatuses', function() {
            $scope.list.filter($scope.statusFilter);
        }, true);
        $scope.$watch('sorting', function() {
            $scope.list.sort($scope.sorting);
        }, true);

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
            $scope.suitesByNodeName.forEach(function(nodeName) {
                if (nodeName.suitesMap.hasOwnProperty(compositeUid)) {
                    nodeName.expanded = true;
                }
            });
        }

        function findSuite(compositeUid) {
            var keys = compositeUid.split(':'),
                node = keys[0];
            var suite;

            $scope.suitesByNodeName.some(function(nodeName) {
                if (nodeName.nodeName !== node) {
                    return undefined;
                }
                if (!nodeName.suitesMap.hasOwnProperty(compositeUid)) {
                    return undefined;
                }
                suite = nodeName.suitesMap[compositeUid];
                return suite;
            });
            //noinspection JSUnusedAssignment
            return suite;
        }

    });
})();

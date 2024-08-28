/*global angular*/
(function() {
    'use strict';
    var module = angular.module('allure.nodeType', []);
    module.config(function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('nodeType', {title: 'nodeType.TITLE', icon: 'fa fa-signal', resolve: {
            nodeTypes: function($http) {
                return $http.get('data/nodeType.json').then(function(r) {
                    return r.data;
                });
            }
        }});

        $stateProvider.state('nodeType.suite', {
            url: "/:compositeUid"
        });
        $stateProvider.state('nodeType.suite.expanded', {
            url: '/expanded'
        });

        testcaseProvider.attachStates('nodeType.suite');
        allureTabsProvider.addTranslation('nodeType');
        allureTabsProvider.addStylesheet('nodeType');
    });

    module.controller('NodeTypeCtrl', function($scope, $state, status, WatchingStore, Collection, nodeTypes) {
        var nodeTypeList = [];
        for (var nodeType in nodeTypes) {
            nodeTypeList.push(nodeType);
        }

        $scope.nodeTypeList = nodeTypeList;

        $scope.suitesByNodeType = [];
        $scope.allTestCases = [];

        nodeTypeList.forEach(function (nodeType) {
            var suiteByNodeType = nodeTypes[nodeType];
            $scope.suitesByNodeType.push(suiteByNodeType);
        });

        var store = new WatchingStore('nodeTypeSettings');
        $scope.list = new Collection($scope.suitesByNodeType);

        $scope.isState = function (statename) {
            return $state.is(statename);
        };

        $scope.setSuite = function(compositeKey) {
            $state.go('nodeType.suite', {compositeUid: compositeKey});
        };
        $scope.isActiveSuite = function(suite) {
            return $scope.suite === suite;
        };

        $scope.expandnodeType = function(nodeType, expanded) {
            if(!expanded && nodeType.suitesMap.valueOf($scope.suite) !== undefined) {
                $state.go('nodeType');
            }
            nodeType.expanded = expanded;
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'testCasesFailedCount'),
            reverse: store.bindProperty($scope, 'sorting.reverse', true)
        };
        $scope.$watch('testcase.uid', function(testcaseUid, oldUid) {
            if (testcaseUid && testcaseUid !== oldUid) {
                $state.go('nodeType.suite.testcase', {testcaseUid: testcaseUid});
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
            $scope.suitesByNodeType.forEach(function(nodeType) {
                if (nodeType.suitesMap.hasOwnProperty(compositeUid)) {
                    nodeType.expanded = true;
                }
            });
        }

        function findSuite(compositeUid) {
            var keys = compositeUid.split(':'),
                node = keys[0];
            var suite;

            $scope.suitesByNodeType.some(function(nodeType) {
                if (nodeType.nodeType !== node) {
                    return undefined;
                }
                if (!nodeType.suitesMap.hasOwnProperty(compositeUid)) {
                    return undefined;
                }
                suite = nodeType.suitesMap[compositeUid];
                return suite;
            });
            //noinspection JSUnusedAssignment
            return suite;
        }

    });
})();

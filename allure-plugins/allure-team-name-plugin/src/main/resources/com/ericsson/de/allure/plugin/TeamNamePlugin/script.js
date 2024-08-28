/*global angular*/
(function() {
    'use strict';
    var module = angular.module('allure.teamName', []);
    module.config(function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('teamName', {title: 'teamName.TITLE', icon: 'fa fa-signal', resolve: {
            teamNames: function($http) {
                return $http.get('data/teamName.json').then(function(r) {
                    return r.data;
                });
            }
        }});

        $stateProvider.state('teamName.suite', {
            url: "/:compositeUid"
        });
        $stateProvider.state('teamName.suite.expanded', {
            url: '/expanded'
        });

        testcaseProvider.attachStates('teamName.suite');
        allureTabsProvider.addTranslation('teamName');
        allureTabsProvider.addStylesheet('teamName');
    });

    module.controller('TeamNameCtrl', function($scope, $state, status, WatchingStore, Collection, teamNames) {
        var teamNameList = [];
        debugger;
        for (var teamName in teamNames) {
            teamNameList.push(teamName);
        }

        $scope.teamNameList = teamNameList;

        $scope.suitesByTeamName = [];
        $scope.allTestCases = [];

        teamNameList.forEach(function (teamName) {
            var suiteByTeamName = teamNames[teamName];
            $scope.suitesByTeamName.push(suiteByTeamName);
        });

        var store = new WatchingStore('teamNameSettings');

        $scope.isState = function (statename) {
            return $state.is(statename);
        };

        $scope.setSuite = function(compositeKey) {
            $state.go('teamName.suite', {compositeUid: compositeKey});
        };
        $scope.isActiveSuite = function(suite) {
            return $scope.suite === suite;
        };


        $scope.expandteamName = function(teamName, expanded) {
            if(!expanded && teamName.suitesMap.valueOf($scope.suite) !== undefined) {
                $state.go('teamName');
            }
            teamName.expanded = expanded;
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'testCasesFailedCount'),
            reverse: store.bindProperty($scope, 'sorting.reverse', false)
        };
        $scope.$watch('testcase.uid', function(testcaseUid, oldUid) {
            if (testcaseUid && testcaseUid !== oldUid) {
                $state.go('teamName.suite.testcase', {testcaseUid: testcaseUid});
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
            $scope.suitesByTeamName.forEach(function(teamName) {
                if (teamName.suitesMap.hasOwnProperty(compositeUid)) {
                    teamName.expanded = true;
                }
            });
        }

        function findSuite(compositeUid) {
            var keys = compositeUid.split(':'),
                node = keys[0];
            var suite;

            $scope.suitesByTeamName.some(function(teamName) {
                if (teamName.teamName !== node) {
                    return undefined;
                }
                if (!teamName.suitesMap.hasOwnProperty(compositeUid)) {
                    return undefined;
                }
                suite = teamName.suitesMap[compositeUid];
                return suite;
            });
            //noinspection JSUnusedAssignment
            return suite;
        }

    });
})();

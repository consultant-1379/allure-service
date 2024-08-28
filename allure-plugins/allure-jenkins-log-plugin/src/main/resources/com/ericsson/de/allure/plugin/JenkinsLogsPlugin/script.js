/*global angular*/
(function () {
    "use strict";
    var module = angular.module('allure.jenkins-logs', []); //dependencies should go in []
    module.config(['$stateProvider', 'allureTabsProvider', 'testcaseProvider', function ($stateProvider, allureTabsProvider, testcaseProvider) {
        $stateProvider
            .state('jenkins-logs', {
                url: "/jenkins-logs",
                templateUrl: "plugins/jenkins-logs/tab.tpl.html",
                controller: 'JenkinsLogsCtrl',
                resolve: {
                    pluginData: function ($http) {
                        return $http.get('data/jenkins-logs.json').then(function (response) {
                            return response.data;
                        });
                    }
                }
            }).state('jenkins-logs.detailed', {
                url: "/:logName"
            })
            .state('jenkins-logs.detailed.expanded', {
                url: '/expanded'
            });

        allureTabsProvider.tabs.push({name: 'jenkins-logs', title: 'Jenkins Logs', icon: 'fa fa-file-text-o'});
        allureTabsProvider.addStylesheet('jenkins-logs');
    }]);

    module.controller('JenkinsLogsCtrl', ['$scope', '$state', '$http', 'pluginData', 'WatchingStore', 'Collection', function ($scope, $state, $http, pluginData, WatchingStore, Collection) {
        "use strict";

        var store = new WatchingStore('jenkinsLogsSettings');

        $scope.exportData = new Collection(pluginData.logFiles);
        $scope.errorMessage = pluginData.errorMessage;

        $scope.isState = function (statename) {
            return $state.is(statename);
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'name'),
            reverse: store.bindProperty($scope, 'sorting.reverse', false)
        };

        $scope.$watch('sorting', function () {
            $scope.exportData.sort($scope.sorting);
        }, true);

        $scope.setLogName = function (logName) {
            $state.go('jenkins-logs.detailed', {
                logName: logName,
                attachment: {
                    type: 'text',
                    source: logName
                }
            });
        };

        $scope.$on('$stateChangeSuccess', function (event, state, params) {
            delete $scope.logName;
            delete $scope.source;
            delete $scope.attachText;
            if (params.logName) {
                $scope.logName = params.logName;
                $scope.source = $scope.getSourceUrl(params.logName);
                fileGetContents($scope.source);
            }
        });

        $scope.getSourceUrl = function (logName) {
            return pluginData.logSourcePath + logName;
        };

        $scope.go = function(state) {
            $state.go(state);
        };

        function fileGetContents(url) {
            //get raw file content without parsing
            $http.get(url, {transformResponse: []}).then(function (response) {
                $scope.attachText = response.data;
            }, $scope.onError);
        }
    }]);
})();

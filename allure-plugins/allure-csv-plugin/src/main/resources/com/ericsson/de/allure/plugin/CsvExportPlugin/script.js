/*global angular*/
(function() {
    "use strict";
    var module = angular.module('allure.csvExport', ['ngSanitize', 'ngCsv']);
    module.config(['$stateProvider', 'allureTabsProvider', 'testcaseProvider', function($stateProvider, allureTabsProvider, testcaseProvider) {
        allureTabsProvider.addTab('csvExport', {title: 'CSV Export', icon: 'fa fa-table'});
        allureTabsProvider.addStylesheet('csvExport');
        testcaseProvider.attachStates('csvExport');
    }]);
    module.controller('CsvExportCtrl', ['$scope', '$state', 'data', 'WatchingStore', 'Collection', function($scope, $state, data, WatchingStore, Collection) {
        "use strict";

        var store = new WatchingStore('csvExportSettings');

        $scope.exportData = new Collection(data.slice(1, data.length));

        $scope.getArray = function() {
            return data;
        };

        $scope.isState = function(statename) {
            return $state.is(statename);
        };

        $scope.sorting = {
            predicate: store.bindProperty($scope, 'sorting.predicate', 'title'),
            reverse: store.bindProperty($scope, 'sorting.reverse', false)
        };

        $scope.$watch('sorting', function() {
            $scope.exportData.sort($scope.sorting);
        }, true);
    }]);
})();

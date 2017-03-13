define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                defaultSchemaName: '@',
                defaultTableName: '@'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/tables/preview.html',
            controller: "PreviewController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http,FeedService, RestUrlService, HiveService) {

        var self = this;

        this.model = {};
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[1];
        this.executingQuery = true;

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function(item) {
            self.query();
        };

        this.gridOptions = $scope.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true
        };


        this.query = function() {
            self.executingQuery = true;
            if (self.defaultSchemaName != null && self.defaultTableName != null) {
                if (self.rowsPerPage == null) {
                    self.rowsPerPage = 50;
                }
                self.sql = 'SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT "+self.limit;
            }

            return HiveService.queryResult(self.sql).then(function(tableData) {
                self.loading = true;
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);.0
                self.gridOptions.columnDefs = result.columns;
                self.gridOptions.data = result.rows;
                self.executingQuery = false;
            });
        };

        self.query();
    };


    angular.module(moduleName).controller('PreviewController', ["$scope","$http","FeedService","RestUrlService","HiveService",controller]);
    angular.module(moduleName)
        .directive('thinkbigPreview', directive);

});

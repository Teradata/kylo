define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                defaultSchemaName: '@',
                defaultTableName: '@',
                datasourceId: '@'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/tables/preview.html',
            controller: "PreviewController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http,FeedService, RestUrlService, HiveService, FattableService, DatasourcesService) {

        var self = this;

        this.model = {};
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[1];
        this.loading = false;

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function(item) {
            self.query();
        };

        this.query = function() {
            console.log('query');
            self.loading = true;
            if (self.defaultSchemaName != null && self.defaultTableName != null) {
                if (self.rowsPerPage == null) {
                    self.rowsPerPage = 50;
                }
                self.sql = 'SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT "+self.limit;
            }

            var successFn = function (tableData) {
                console.log('got response', tableData);
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);

                FattableService.setupTable({
                    tableContainerId: "preview-table",
                    headers: result.columns,
                    rows: result.rows
                });

                self.loading = false;
            };
            var errorFn = function (err) {
                self.loading = false;
            };

            var promise;
            if (self.datasource.isHive) {
                promise = HiveService.queryResult(self.sql);
            } else {
                promise = DatasourcesService.query(self.datasourceId, self.sql);
            }
            promise.then(successFn, errorFn);
        };

        function getDatasource(datasourceId) {
            self.loading = true;
            var successFn = function (response) {
                self.datasource = response;
                self.loading = false;
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }

        getDatasource(self.datasourceId).then(self.query)
    };


    angular.module(moduleName).controller('PreviewController', ["$scope","$http","FeedService","RestUrlService","HiveService", "FattableService", "DatasourcesService", controller]);
    angular.module(moduleName).directive('thinkbigPreview', directive);

});

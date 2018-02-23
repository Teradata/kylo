define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/tables/module-name');
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
    };
    var PreviewController = /** @class */ (function () {
        function PreviewController($scope, $http, FeedService, RestUrlService, HiveService, FattableService) {
            this.$scope = $scope;
            this.$http = $http;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.FattableService = FattableService;
            var self = this;
            this.model = {};
            this.loading = false;
            this.limitOptions = [10, 50, 100, 500, 1000];
            this.limit = this.limitOptions[1];
            this.executingQuery = true;
            //noinspection JSUnusedGlobalSymbols
            this.onLimitChange = function (item) {
                self.query();
            };
            this.gridOptions = $scope.gridOptions = {
                columnDefs: [],
                data: null,
                enableColumnResizing: true,
                enableGridMenu: true
            };
            this.query = function () {
                self.executingQuery = true;
                if (self.defaultSchemaName != null && self.defaultTableName != null) {
                    if (self.rowsPerPage == null) {
                        self.rowsPerPage = 50;
                    }
                    self.sql = 'SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT " + self.limit;
                }
                return HiveService.queryResult(self.sql).then(function (tableData) {
                    self.loading = true;
                    var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);
                    self.executingQuery = false;
                    FattableService.setupTable({
                        tableContainerId: "preview-table",
                        headers: result.columns,
                        rows: result.rows
                    });
                });
            };
            self.query();
        }
        ;
        return PreviewController;
    }());
    exports.PreviewController = PreviewController;
    angular.module(moduleName).controller('PreviewController', ["$scope", "$http", "FeedService", "RestUrlService", "HiveService", "FattableService", PreviewController]);
    angular.module(moduleName)
        .directive('thinkbigPreview', directive);
});
//# sourceMappingURL=preview.js.map
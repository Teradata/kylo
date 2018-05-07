import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";


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
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
}
export class PreviewController {

    model:any;
    loading:any;
    limitOptions:any;
    limit:any;
    executingQuery:any;
    onLimitChange:any;
    gridOptions:any;
    query:any;
    defaultSchemaName:any;
    defaultTableName:any;
    rowsPerPage:any;
    queryResults:any;
    sql:any;
    datasource:any;
    datasourceId:any;

    constructor(private $scope:any,private $http:any,private FeedService:any, private RestUrlService:any
        , private HiveService:any, private FattableService:any, private DatasourcesService:any) {

        var self = this;

        this.model = {};
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[1];
        this.loading = false;

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function(item:any) {
            self.query();
        };

        this.query = function() {
            // console.log('query');
            self.loading = true;

            var successFn = function (tableData:any) {
                // console.log('got response', tableData);
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);

                FattableService.setupTable({
                    tableContainerId: "preview-table",
                    headers: result.columns,
                    rows: result.rows
                });

                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };

            if (self.defaultSchemaName != null && self.defaultTableName != null) {
                if (self.rowsPerPage == null) {
                    self.rowsPerPage = 50;
                }
                
                var promise;
                if (self.datasource.isHive) {
                    promise = HiveService.queryResult('SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT " + self.limit);
                } else {
                    promise = DatasourcesService.preview(self.datasourceId, self.defaultSchemaName, self.defaultTableName, self.limit);
                }
            }
            promise.then(successFn, errorFn);
        };

        function getDatasource(datasourceId:any) {
            self.loading = true;
            var successFn = function (response:any) {
                self.datasource = response;
                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }

        getDatasource(self.datasourceId).then(self.query)
    };


}
angular.module(moduleName).controller('PreviewController', ["$scope","$http","FeedService","RestUrlService","HiveService", "FattableService", "DatasourcesService", PreviewController]);
angular.module(moduleName)
    .directive('thinkbigPreview', directive);


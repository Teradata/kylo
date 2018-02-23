import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/tables/module-name');


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

    constructor(private $scope:any,private $http:any,private FeedService:any, private RestUrlService:any
        , private HiveService:any, private FattableService:any) {

        var self = this;

        this.model = {};
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[1];
        this.executingQuery = true;

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function(item:any) {
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

            return HiveService.queryResult(self.sql).then(function(tableData:any) {
                self.loading = true;
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);
                self.executingQuery = false;

                FattableService.setupTable({
                    tableContainerId:"preview-table",
                    headers: result.columns,
                    rows: result.rows
                });
            });
        };


        self.query();
    };


}
angular.module(moduleName).controller('PreviewController', ["$scope","$http","FeedService","RestUrlService","HiveService", "FattableService", PreviewController]);
angular.module(moduleName)
    .directive('thinkbigPreview', directive);


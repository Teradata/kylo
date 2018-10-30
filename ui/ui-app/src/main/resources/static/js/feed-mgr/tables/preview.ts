import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import { FeedService } from '../services/FeedService';
import { DatasourcesService } from '../services/DatasourcesService';


export class PreviewController {

    model: any = {};
    loading: boolean = false;
    limitOptions: number[] = [10, 50, 100, 500, 1000];
    limit: any;
    executingQuery: any;
    gridOptions: any;
    defaultSchemaName: any;
    defaultTableName: any;
    rowsPerPage: any;
    queryResults: any;
    sql: any;
    datasource: any;
    datasourceId: any;

    static readonly $inject = ["$scope", "$http", "FeedService", "RestUrlService", "HiveService", "FattableService", "DatasourcesService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private feedService: FeedService, private RestUrlService: any
        , private HiveService: any, private FattableService: any, private datasourcesService: DatasourcesService) {


        this.limit = this.limitOptions[1];
        this.getDatasource(this.datasourceId).then(this.query);
    }

    query = () => {
        // console.log('query');
        this.loading = true;

        var successFn = (tableData: any) => {
            // console.log('got response', tableData);
            var result = this.queryResults = this.HiveService.transformQueryResultsToUiGridModel(tableData);

            this.FattableService.setupTable({
                tableContainerId: "preview-table",
                headers: result.columns,
                rows: result.rows
            });

            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };

        if (this.defaultSchemaName != null && this.defaultTableName != null) {
            if (this.rowsPerPage == null) {
                this.rowsPerPage = 50;
            }

            var promise;
            if (this.datasource.isHive) {
                promise = this.HiveService.queryResult('SELECT * FROM ' + this.defaultSchemaName + "." + this.defaultTableName + " LIMIT " + this.limit);
            } else {
                promise = this.datasourcesService.preview(this.datasourceId, this.defaultSchemaName, this.defaultTableName, this.limit);
            }
        }
        promise.then(successFn, errorFn);
    };
    onLimitChange = (item: any) => {
        this.query();
    };

    getDatasource(datasourceId: any) {
        this.loading = true;
        var successFn = (response: any) => {
            this.datasource = response;
            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };
        return this.datasourcesService.findById(datasourceId).then(successFn, errorFn);


    }
};


angular.module(moduleName).component('thinkbigPreview', {
    bindings: {
        defaultSchemaName: '@',
        defaultTableName: '@',
        datasourceId: '@'
    },
    controller : PreviewController,
    templateUrl: './preview.html',
    controllerAs: 'vm'
});

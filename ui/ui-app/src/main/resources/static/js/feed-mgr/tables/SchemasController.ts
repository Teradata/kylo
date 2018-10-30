import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import { Transition } from '@uirouter/core';
import {StateService} from '../../services/StateService';
import { FeedService } from '../services/FeedService';
import {AddButtonService} from '../../services/AddButtonService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import { DatasourcesService } from '../services/DatasourcesService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';


export class SchemasController {

    schemas: any[] = [];
    loading: boolean = true;
    datasource: any;
    cardTitle: any;
    pageName: string = 'Schemas';
    filterInternal: boolean = true;
    paginationData: any;
    paginationId: any;
    currentPage: any;
    viewType: any;
    sortOptions: any;
    filter: any;
    datasourceId: any;
    $transition$: Transition;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.datasourceId = this.$transition$.params().datasource;
        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
        this.viewType = this.paginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();

        this.filter = this.paginationDataService.filter(this.pageName);

    }

    static readonly $inject = ["$scope", "$http", "$q", "$filter", "RestUrlService",
        "PaginationDataService", "TableOptionsService", "AddButtonService",
        "FeedService", "StateService", "DatasourcesService"];
    constructor(private $scope: IScope, private $http: angular.IHttpService, private $q: angular.IQService, private $filter: angular.IFilterService, private RestUrlService: any
        , private paginationDataService: DefaultPaginationDataService, private tableOptionsService: DefaultTableOptionsService, private addButtonService: AddButtonService
        , private feedService: FeedService, private stateService: any, private datasourcesService: DatasourcesService) {

        
        $scope.$watch(() => {
            return this.viewType;
        }, (newVal: any) => {
            this.onViewTypeChange(newVal);
        });

        $scope.$watch(() => {
            return this.filter;
        }, (newVal: any) => {
            this.paginationDataService.filter(this.pageName, newVal)
        });

        this.getDatasource(this.datasourceId).then(this.getSchemas);
    };

    onViewTypeChange = (viewType: any) => {
        this.paginationDataService.viewType(this.pageName, this.viewType);
    };

    onOrderChange = (order: any) => {
        this.paginationDataService.sort(this.pageName, order);
        this.tableOptionsService.setSortOption(this.pageName, order);
        this.getSchemas();
    };

    onPaginationChange = (page: any, limit: any) => {
        this.paginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption = (option: any) => {
        var sortString = this.tableOptionsService.toSortString(option);
        var savedSort = this.paginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
        this.tableOptionsService.setSortOption(this.pageName, sortString);
    };

    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    loadSortOptions() {
        var options = { 'Schema': 'schema' };
        var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'schema', 'asc');
        this.tableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    getSchemas() {
        this.loading = true;
        var successFn = (response: any) => {
            this.schemas = response.data;
            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };

        var limit = this.paginationDataService.rowsPerPage(this.pageName);
        var start = limit == 'All' ? 0 : (limit * this.currentPage) - limit;
        var sort = this.paginationData.sort;
        var filter = this.paginationData.filter;
        var params = { start: start, limit: limit, sort: sort, filter: filter };

        var promise;
        if (this.datasource.isHive) {
            promise = this.$http.get(this.RestUrlService.HIVE_SERVICE_URL + "/schemas", { params: params });
        } else {
            promise = this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + this.datasource.id + "/schemas", { params: params });
        }

        promise.then(successFn, errorFn);
        return promise;
    }

    onClickSchema = (schema: any) => {
        this.stateService.FeedManager().Table().navigateToTables(this.datasource.id, schema);
    };

    getDatasource(datasourceId: any) {
        this.loading = true;
        var successFn = (response: any) => {
            this.datasource = response;
            this.cardTitle = this.datasource.name;
            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };
        return this.datasourcesService.findById(this.datasourceId).then(successFn, errorFn);
    }

}
angular.module(moduleName).component('schemasController', {
    templateUrl: './schemas.html',
    controller: SchemasController,
    controllerAs: "vm",
    bindings : {
        $transition$: '<'
    }
});


import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import { Transition } from '@uirouter/core';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DatasourcesService } from '../services/DatasourcesService';
import { controller } from '../../ops-mgr/service-health/ServiceComponentHealthDetailsController';
import { FeedService } from '../services/FeedService';
import {AddButtonService} from '../../services/AddButtonService';

export class TablesController {

    datasource: any;
    schema: any;
    tables: any[] = [];
    loading: boolean = true;
    cardTitle: any;
    pageName: string;
    filterInternal: boolean = true;
    paginationData: any;
    paginationId: string = 'tables';
    currentPage: any;
    viewType: any;
    sortOptions: any;
    additionalOptions: any;
    filter: any;
    selectedTables: any;
    datasourceId: any;
    $transition$: Transition;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.datasourceId = this.$transition$.params().datasource;
        this.schema = this.$transition$.params().schema;
        this.pageName = this.$filter('translate')('views.TableController.Tables');
        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
        this.viewType = this.paginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();
        this.additionalOptions = [{ header: "Cache", label: "Cache" }, { label: "Refresh Cache", icon: "refresh" }];

        this.filter = this.paginationDataService.filter(this.pageName);
    }

    static readonly $inject = ["$scope", "$http", "$q", "$filter", "RestUrlService",
        "PaginationDataService", "TableOptionsService", "AddButtonService", "FeedService",
        "StateService", "Utils", "DatasourcesService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private $q: angular.IQService, private $filter: angular.IFilterService, private RestUrlService: any
        , private paginationDataService: DefaultPaginationDataService, private tableOptionsService: DefaultTableOptionsService, private addButtonService: AddButtonService, private feedService: FeedService
        , private stateService: any, private Utils: any, private datasourcesService: DatasourcesService) {

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
        this.getDatasource(this.datasourceId).then(this.init());

    };
    successFn = (response: any) => {
        var _tables = response.hive.data;
        var feedNames = response.feedNames.data;
        if (_tables) {
            angular.forEach(_tables, (table) => {
                var tableName = table.substr(table.indexOf(".") + 1);
                this.tables.push({ tableName: tableName, fullName: table, lowerFullName: table.toLowerCase() });
            })
        }
        this.selectedTables = _.filter(this.tables, (t) => {
            var isKnown = this.isKnownFeedTable(feedNames, this.schema.toLowerCase());
            return !isKnown || (isKnown && !this.endsWithReservedWord(t));
        });
        this.loading = false;
        //TODO @Greg There was no deferred variable available in this scope.
        var deferred = this.$q.defer();
        deferred.resolve();
    };
    errorFn = (err: any) => {
        this.loading = false;
        //TODO @Greg There was no deferred variable available in this scope.
        var deferred = this.$q.defer();
        deferred.reject(err);
    };
    onViewTypeChange = (viewType: any) => {
        this.paginationDataService.viewType(this.pageName, this.viewType);
    };

    onOrderChange = (order: any) => {
        this.paginationDataService.sort(this.pageName, order);
        this.tableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange = (page: any, limit: any) => {
        this.paginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    onClickTable = (table: any) => {
        this.stateService.FeedManager().Table().navigateToTable(this.datasource.id, this.schema, table.tableName);
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

    selectedAdditionalOption = (option: any) => {
        this.$http.get(this.RestUrlService.HIVE_SERVICE_URL + "/refreshUserHiveAccessCache").then(this.init);
    };

    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    loadSortOptions = () => {
        var options = { 'Table': 'tableName' };
        var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'tableName', 'asc');
        this.tableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    endsWithReservedWord = (t: any) => {
        return this.Utils.endsWith(t.tableName, "_valid") || this.Utils.endsWith(t.tableName, "_invalid") || this.Utils.endsWith(t.tableName, "_profile") || this.Utils.endsWith(t.tableName, "_feed");
    }

    isKnownFeedTable = (feedNames: any, schema: any) => {
        return _.find(feedNames, (feedName: any) => {
            return feedName.startsWith(schema + ".");
        }) !== undefined;
    }
    getNonHiveTables = () =>{
        var deferred = this.$q.defer();

        var limit = this.paginationDataService.rowsPerPage(this.pageName);
        var start = limit == 'All' ? 0 : (limit * this.currentPage) - limit;
        var sort = this.paginationData.sort;
        var filter = this.paginationData.filter;
        var params = { schema: this.schema, start: start, limit: limit, sort: sort, filter: filter };

        var promises = {
            "hive": this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + this.datasource.id + "/tables", { params: params }),
            "feedNames": this.feedService.getFeedNames()
        };

        this.$q.all(promises).then(this.successFn, this.errorFn);

        return deferred.promise;
    }

    getHiveTables = () => {
        var deferred = this.$q.defer();

        var promises = {
            "hive": this.$http.get(this.RestUrlService.HIVE_SERVICE_URL + "/schemas/" + this.schema + "/tables"),
            "feedNames": this.feedService.getFeedNames()
        };
        this.$q.all(promises).then(this.successFn, this.errorFn);

        return deferred.promise;
    }

    init = () => {
        this.tables = [];
        if (this.datasource.isHive) {
            this.getHiveTables();
        } else {
            this.getNonHiveTables();
        }
    }

    getDatasource = (datasourceId: any) => {
        this.loading = true;
        var successFn = (response: any) => {
            this.datasource = response;
            this.cardTitle = this.schema;
            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };
        return this.datasourcesService.findById(datasourceId).then(successFn, errorFn);
    }

}
angular.module(moduleName).component('tablesController', {
    controller: TablesController,
    controllerAs: 'vm',
    templateUrl: './tables.html',
    bindings: {
        $transition$: '<'
    }
});


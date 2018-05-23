import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";


export class SchemasController {

    schemas:any;
    loading:any;
    datasource:any;
    cardTitle:any;
    pageName:any;
    filterInternal:any;
    paginationData:any;
    paginationId:any;
    currentPage:any;
    viewType:any;
    sortOptions:any;
    filter:any;
    onViewTypeChange:any;
    onOrderChange:any;
    onPaginationChange:any;
    selectedTableOption:any;
    onClickSchema:any;
    datasourceId:any;
    constructor(private $scope:any,private $http:any,private $q:any,private $transition$:any,private $filter:any,private RestUrlService:any
        , private PaginationDataService:any,private TableOptionsService:any, private AddButtonService:any
        , private FeedService:any,private StateService:any,private DatasourcesService:any){

        var self = this;
        this.schemas = [];
        this.loading = true;
        self.datasourceId = $transition$.params().datasource;
        this.pageName = 'Schemas';
        self.filterInternal = true;

        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'schemas';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);

        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal:any) {
            self.onViewTypeChange(newVal);
        });

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal:any) {
            PaginationDataService.filter(self.pageName, newVal)
        });

        this.onViewTypeChange = function(viewType:any) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        };

        this.onOrderChange = function(order:any) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            getSchemas();
        };

        this.onPaginationChange = function(page:any, limit:any) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option:any) {
            var sortString = TableOptionsService.toSortString(option);
            var savedSort = PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        };

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Schema': 'schema'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'schema', 'asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        function getSchemas() {
            self.loading = true;
            var successFn = function (response:any) {
                self.schemas = response.data;
                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };

            var limit = PaginationDataService.rowsPerPage(self.pageName);
            var start = limit == 'All' ? 0 : (limit * self.currentPage) - limit;
            var sort = self.paginationData.sort;
            var filter = self.paginationData.filter;
            var params = {start: start, limit: limit, sort: sort, filter: filter};

            var promise;
            if (self.datasource.isHive) {
                promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/schemas", {params: params});
            } else {
                promise = $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + self.datasource.id + "/schemas", {params: params});
            }

            promise.then(successFn,errorFn);
            return promise;
        }

        self.onClickSchema = function(schema:any){
            StateService.FeedManager().Table().navigateToTables(self.datasource.id, schema);
        };

        function getDatasource(datasourceId:any) {
            self.loading = true;
            var successFn = function (response:any) {
                self.datasource = response;
                self.cardTitle = self.datasource.name;
                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }


        getDatasource(self.datasourceId).then(getSchemas);

    };
   
}
angular.module(moduleName).controller('SchemasController',["$scope","$http","$q","$transition$","$filter","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","FeedService","StateService","DatasourcesService",SchemasController]);


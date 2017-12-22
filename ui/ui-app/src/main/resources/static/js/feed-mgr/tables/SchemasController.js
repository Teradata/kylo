define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var controller = function($scope,$http,$q,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, FeedService,StateService){

        var self = this;
        this.schemas = [];
        this.loading = true;
        this.cardTitle = "Schemas";
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
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        });

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            PaginationDataService.filter(self.pageName, newVal)
        });

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        };

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            getSchemas();
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
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
            var deferred = $q.defer();

            var successFn = function (response) {
                self.schemas = response.hive.data;
                self.loading = false;
                deferred.resolve();

            };
            var errorFn = function (err) {
                self.loading = false;
                deferred.reject(err);
            };

            var limit = PaginationDataService.rowsPerPage(self.pageName);
            var start = limit == 'All' ? 0 : (limit * self.currentPage) - limit;
            var sort = self.paginationData.sort;
            var filter = self.paginationData.filter;
            var params = {start: start, limit: limit, sort: sort, filter: filter};

            var promises = {
                "hive": $http.get(RestUrlService.HIVE_SERVICE_URL+"/schemas", {params: params})
            };

            $q.all(promises).then(successFn,errorFn);

            return deferred.promise;
        }

        self.onClickSchema = function(schema){
            StateService.FeedManager().Table().navigateToTables(schema);
        };
        getSchemas();

    };

    angular.module(moduleName).controller('SchemasController',["$scope","$http","$q","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","FeedService","StateService",controller]);



});


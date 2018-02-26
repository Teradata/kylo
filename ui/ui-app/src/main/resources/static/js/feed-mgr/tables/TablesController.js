define(['angular',"feed-mgr/tables/module-name", 'pascalprecht.translate'], function (angular,moduleName) {

    var controller = function($scope,$http,$q,$transition$,$filter,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, FeedService,StateService,Utils,DatasourcesService){

        var self = this;

        self.datasourceId = $transition$.params().datasource;
        self.schema = $transition$.params().schema;
        this.tables =[];
        this.loading = true;
        this.pageName = $filter('translate')('views.TableController.Tables');
        self.filterInternal = true;

        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'tables';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();
        this.additionalOptions = [{header: "Cache", label: "Cache"}, {label: "Refresh Cache", icon: "refresh"}];

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
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        this.onClickTable = function(table){
            StateService.FeedManager().Table().navigateToTable(self.datasource.id, self.schema, table.tableName);
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

        this.selectedAdditionalOption = function(option) {
            $http.get(RestUrlService.HIVE_SERVICE_URL + "/refreshUserHiveAccessCache").then(init);
        };

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Table': 'tableName'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'tableName', 'asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        function endsWithReservedWord(t) {
            return Utils.endsWith(t.tableName, "_valid") || Utils.endsWith(t.tableName, "_invalid") || Utils.endsWith(t.tableName, "_profile") || Utils.endsWith(t.tableName, "_feed");
        }

        function isKnownFeedTable(feedNames, schema) {
            return _.find(feedNames, function(feedName) {
                return feedName.startsWith(schema + ".");
            }) !== undefined;
        }

        var successFn = function (response) {
            var _tables = response.hive.data;
            var feedNames = response.feedNames.data;
            if (_tables) {
                angular.forEach(_tables, function (table) {
                    var tableName = table.substr(table.indexOf(".") + 1);
                    self.tables.push({tableName: tableName, fullName: table, lowerFullName: table.toLowerCase()});
                })
            }
            self.selectedTables = _.filter(self.tables, function (t) {
                var isKnown = isKnownFeedTable(feedNames, self.schema.toLowerCase());
                return !isKnown || (isKnown && !endsWithReservedWord(t));
            });
            self.loading = false;
            deferred.resolve();
        };
        var errorFn = function (err) {
            self.loading = false;
            deferred.reject(err);
        };

        function getNonHiveTables() {
            var deferred = $q.defer();

            var limit = PaginationDataService.rowsPerPage(self.pageName);
            var start = limit == 'All' ? 0 : (limit * self.currentPage) - limit;
            var sort = self.paginationData.sort;
            var filter = self.paginationData.filter;
            var params = {schema: self.schema, start: start, limit: limit, sort: sort, filter: filter};

            var promises = {
                "hive": $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + self.datasource.id + "/tables", {params: params}),
                "feedNames": FeedService.getFeedNames()
            };

            $q.all(promises).then(successFn, errorFn);

            return deferred.promise;
        }

        function getHiveTables() {
            var deferred = $q.defer();

            var promises = {
                "hive": $http.get(RestUrlService.HIVE_SERVICE_URL + "/schemas/" + self.schema + "/tables"),
                "feedNames": FeedService.getFeedNames()
            };
            $q.all(promises).then(successFn, errorFn);

            return deferred.promise;
        }

        function init() {
            self.tables =[];
            if (self.datasource.isHive) {
                getHiveTables();
            } else {
                getNonHiveTables();
            }
        }

        function getDatasource(datasourceId) {
            self.loading = true;
            var successFn = function (response) {
                self.datasource = response;
                self.cardTitle = self.datasource.name + " " + self.schema + " " + $filter('translate')('views.TableController.Tables');
                self.loading = false;
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }


        getDatasource(self.datasourceId).then(init);

    };

    angular.module(moduleName).controller('TablesController',["$scope","$http","$q","$transition$","$filter","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","FeedService","StateService","Utils", "DatasourcesService", controller]);



});


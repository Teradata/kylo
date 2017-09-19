define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var controller = function($scope,$http,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, FeedService,StateService,Utils){

        var self = this;
        this.tables =[];
        var ALL_DATABASES = '(All)';
        this.schemas = [ALL_DATABASES];
        this.schemaTables = {};
        this.selectedDatabase = ALL_DATABASES;
        this.selectedTables = [];
        this.loading = true;
        this.cardTitle = "Tables";
        this.pageName = 'Tables';
        self.filterInternal = true;
        this.filterTables = ["_valid", "_invalid", "_profile", "_feed"]

        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'tables';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', 'All']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);

        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        })

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            PaginationDataService.filter(self.pageName, newVal)
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
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
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Schema': 'schema', 'Table': 'tableName'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'schema', 'asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        function getTables(){
            var successFn = function (response) {
              var _tables = response.data;
                var arr = [];
                if(_tables) {
                    angular.forEach(_tables,function(table){
                        var schema = table.substr(0,table.indexOf("."));
                        var tableName= table.substr(table.indexOf(".")+1);
                        arr.push({schema:schema,tableName:tableName});
                        if(self.schemaTables[schema] == undefined){
                            self.schemaTables[schema] = [];
                            self.schemas.push(schema);
                        }
                        self.schemaTables[schema].push({schema:schema,tableName:tableName});
                    })
                }
                self.tables = arr;
                self.selectedTables = _.filter(arr,function(t) {
                    var filterTableEndingIndex = _.findIndex(self.filterTables, function(filterTableEnding) { return Utils.endsWith(t.tableName, filterTableEnding) });
                    if(filterTableEndingIndex > -1) {
                       return _.findIndex(arr, function(tt) { return tt.tableName === t.tableName + self.filterTables[filterTableEndingIndex] }) > -1
                    }
                    return true;
                });
                self.loading = false;

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL+"/tables");
            promise.then(successFn, errorFn);
            return promise;
        }

        self.onDatabaseChange = function(){
            if(self.selectedDatabase == ALL_DATABASES){
                self.selectedTables = self.tables;
            }
            else {
                self.selectedTables = self.schemaTables[self.selectedDatabase];
            }
        }

        self.onClickTable = function(table){
            StateService.FeedManager().Table().navigateToTable(table.schema,table.tableName);
        }
        getTables();

    };

    angular.module(moduleName).controller('TablesController',["$scope","$http","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","FeedService","StateService","Utils",controller]);



});


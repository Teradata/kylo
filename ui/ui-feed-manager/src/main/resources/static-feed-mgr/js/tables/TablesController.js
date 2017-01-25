/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function () {

    var controller = function($scope,$http,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, FeedService,StateService){

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
                self.selectedTables = arr.filter(function(t) { return !(t.tableName.endsWith("_valid") || t.tableName.endsWith("_invalid") || t.tableName.endsWith("_profile") || t.tableName.endsWith("_feed"))});
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
            StateService.navigateToTable(table.schema,table.tableName);
        }
        getTables();

    };

    angular.module(MODULE_FEED_MGR).controller('TablesController',controller);



}());


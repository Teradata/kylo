(function () {

    var controller = function($scope,$http,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, FeedService,StateService){

        var self = this;
        this.tables =[];
        var ALL_DATABASES = '(All)';
        this.schemas = [ALL_DATABASES];
        this.schemaTables = {};
        this.selectedDatabase = ALL_DATABASES;
        this.selectedTables = [];

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
                self.selectedTables = arr;

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


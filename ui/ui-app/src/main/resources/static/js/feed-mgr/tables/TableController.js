define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var controller = function($scope,$transition$,$http,$filter,RestUrlService,DatasourcesService){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;
        self.hql = '';
        self.schema = $transition$.params().schema;
        self.tableName = $transition$.params().tableName;
        self.datasourceId = $transition$.params().datasource;

        var init = function(){
            getTable(self.schema, self.tableName);
        };

        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal){
        });

        function getTable(schema, table) {
            self.loading = true;
            if (self.datasource.isHive) {
                getHiveTable(schema, table);
            } else {
                getNonHiveTable(schema, table);
            }
        }

        var successFn = function (response) {
            self.tableSchema = response.data;
            self.cardTitle = self.datasource.name + "." + self.schema + " " + $filter('translate')('views.TableController.Tables');
            self.loading = false;
        };
        var errorFn = function (err) {
            self.loading = false;

        };

        function getNonHiveTable(schema, table){
            var params = {schema: schema};
            var promise = $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + self.datasource.id + "/tables/" + table, {params: params});
            promise.then(successFn, errorFn);
            return promise;
        }

        function getHiveTable(schema,table){
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL+"/schemas/"+schema+"/tables/"+table);
            promise.then(successFn, errorFn);
            return promise;
        }

        function getDatasource(datasourceId) {
            self.loading = true;
            var successFn = function (response) {
                self.datasource = response;
                self.loading = false;
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }

        getDatasource(self.datasourceId).then(init);
    };

    angular.module(moduleName).controller('TableController',["$scope","$transition$","$http","$filter","RestUrlService", "DatasourcesService",controller]);



});


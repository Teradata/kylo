define(['angular',"feed-mgr/tables/module-name"], function (angular,moduleName) {

    var controller = function($scope,$transition$,$http,RestUrlService){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;
        self.hql = '';

        var init = function(){
            var schema = $transition$.params().schema;
            self.schema = schema;
            self.tableName = $transition$.params().tableName;
            self.datasource = $transition$.params().datasource;

            getTable(self.schema,self.tableName);
        };


        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal){
        });



        function getTable(schema, table) {
            if (self.datasource.isHive) {
                getHiveTable(schema, table);
            } else {
                getNonHiveTable(schema, table);
            }
        }

        var successFn = function (response) {
            self.tableSchema = response.data;
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
        init();
    };

    angular.module(moduleName).controller('TableController',["$scope","$transition$","$http","RestUrlService",controller]);



});


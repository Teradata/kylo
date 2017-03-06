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
            getTable(self.schema,self.tableName);
        }


        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal){
        })



        function getTable(schema,table){
            var successFn = function (response) {
                self.tableSchema = response.data;

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL+"/schemas/"+schema+"/tables/"+table);
            promise.then(successFn, errorFn);
            return promise;
        }
        init();
    };

    angular.module(moduleName).controller('TableController',["$scope","$transition$","$http","RestUrlService",controller]);



});


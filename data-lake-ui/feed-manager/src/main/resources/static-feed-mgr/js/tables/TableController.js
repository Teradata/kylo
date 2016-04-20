(function () {

    var controller = function($scope,$stateParams,$http,RestUrlService,StateService){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;

        var init = function(){
            var schema = $stateParams.schema;
            self.schema = schema;
            self.tableName = $stateParams.tableName;
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

    angular.module(MODULE_FEED_MGR).controller('TableController',controller);



}());


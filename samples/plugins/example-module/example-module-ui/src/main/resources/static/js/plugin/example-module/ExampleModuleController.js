define(['angular', 'plugin/example-module/module-name'], function (angular, moduleName) {

    var controller = function($transition$,$http){
        var self = this;
        this.name = 'This is an example';

        this.foods = [];

        this.selectedFood = '';

        fetchFood();

        function fetchFood(){
            $http.get("/proxy/v1/example/module/food").then(function(response){
                if(response.data){
                    self.foods = response.data;
                }
            })
        }
    };



    angular.module(moduleName).controller('ExampleModuleController',['$transition$','$http',controller]);

});

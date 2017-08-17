define(['angular', 'plugin/example-module/module-name'], function (angular, moduleName) {

    var controller = function($transition$){
        var self = this;
        this.name = 'This is an example'
    };

    angular.module(moduleName).controller('ExampleModuleController',['$transition$',controller]);

});

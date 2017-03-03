define(['angular','ops-mgr/jobs/details/module-name'], function (angular,moduleName) {

    var controller = function($transition$){
    var self = this;
    this.executionId = $transition$.params().executionId;
    };

    angular.module(moduleName).controller('JobDetailsController',['$transition$',controller]);

});

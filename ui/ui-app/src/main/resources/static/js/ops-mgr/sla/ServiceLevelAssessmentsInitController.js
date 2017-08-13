define(['angular','ops-mgr/sla/module-name'], function (angular,moduleName) {

    var controller = function($transition$){
        var self = this;
        this.filter = $transition$.params().filter;
        this.slaId = $transition$.params().slaId;

    };

    angular.module(moduleName).controller('ServiceLevelAssessmentsInitController',['$transition$',controller]);

});

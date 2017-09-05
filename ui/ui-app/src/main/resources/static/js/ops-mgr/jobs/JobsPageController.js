define(['angular','ops-mgr/jobs/module-name'], function (angular,moduleName) {

    var controller = function($transition$){
        var self = this;
        this.filter = $transition$.params().filter;
        this.tab = $transition$.params().tab;

    };

    angular.module(moduleName).controller('JobsPageController',['$transition$',controller]);

});

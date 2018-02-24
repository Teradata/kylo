define(['angular', 'plugin/example-module/module-name'], function (angular, moduleName) {

    var controller = function($transition$,$http,$interval,$timeout){
        var self = this;

        this.templateId = $transition$.params().templateId;
        this.templateName = $transition$.params().templateName;
        this.model = $transition$.params().model;
    };



    angular.module(moduleName).controller('ExampleTemplateLinkModuleController',['$transition$','$http','$interval','$timeout',controller]);

});

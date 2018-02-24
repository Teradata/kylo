define(['angular', 'plugin/example-module/module-name'], function (angular, moduleName) {

    var controller = function($transition$,$http,$interval,$timeout){
        var self = this;

        this.feedId = $transition$.params().feedId;
        this.feedName = $transition$.params().feedName;
        this.model = $transition$.params().model;

    };



    angular.module(moduleName).controller('ExampleFeedLinkModuleController',['$transition$','$http','$interval','$timeout',controller]);

});


/*
 * Copyright (c) 2015.
 */

(function () {

    var controller = function($scope, HttpService ){


        $scope.$on('$destroy', function(){
            HttpService.cancelPendingHttpRequests();
        });
    };

    angular.module(MODULE_OPERATIONS).controller('OverviewController',controller);



}());
/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {

            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/feeds/feed-details-template.html',
            controller: "FeedDetailsController",
            link: function ($scope, element, attrs, controller) {

            }
        };
    }

    var controller = function ($scope,$http, $stateParams, $interval, $timeout, $q,JobData, TableOptionsService, PaginationDataService, AlertsService, StateService, IconService, TabService) {
        var self = this;
        this.pageName = 'feed-details';




    };


    angular.module('app').controller('FeedDetailsController', controller);

    angular.module('app')
        .directive('tbaFeedDetails', directive);

})();

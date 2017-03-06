define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                selectedTabIndex: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-info.html',
            controller: "FeedInfoController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope) {

        var self = this;

    };

    angular.module(moduleName).controller('FeedInfoController', ["$scope",controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedInfo', directive);

});

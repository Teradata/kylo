(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                selectedTabIndex: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-info.html',
            controller: "FeedInfoController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope) {

        var self = this;

    };

    angular.module(MODULE_FEED_MGR).controller('FeedInfoController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedInfo', directive);

})();

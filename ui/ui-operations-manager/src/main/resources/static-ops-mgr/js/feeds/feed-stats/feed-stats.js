(function () {

    var controller = function ($scope, $stateParams) {
        var self = this;

        self.feedName = $stateParams.feedName;

    };

    angular.module(MODULE_OPERATIONS).controller('FeedStatsController', controller);

}());
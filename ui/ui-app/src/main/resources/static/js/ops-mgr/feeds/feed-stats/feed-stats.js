define(['angular','ops-mgr/feeds/feed-stats/module-name'], function (angular,moduleName) {

    var controller = function ($scope, $transition$) {
        var self = this;

        self.feedName = $transition$.params().feedName;

    };

    angular.module(moduleName).controller('FeedStatsController',["$scope", "$transition$", controller]);

});

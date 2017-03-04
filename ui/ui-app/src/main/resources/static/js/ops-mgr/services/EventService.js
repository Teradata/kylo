define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('EventService', function ($rootScope) {

        var self = this;

        this.FEED_HEALTH_CARD_FINISHED = "FEED_HEALTH_CARD_FINISHED";

        this.broadcastFeedHealthCardRendered = function () {
            $rootScope.$broadcast(self.FEED_HEALTH_CARD_FINISHED);
        }

        this.listenFeedHealthCardRendered = function (callback) {
            $rootScope.$on(self.FEED_HEALTH_CARD_FINISHED, callback)
        }

    });
});
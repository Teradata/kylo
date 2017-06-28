define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular, moduleName) {

    var thinkbigFeedInfo = function () {
        return {
            restrict: "EA",
            bindToController: {
                selectedTabIndex: "="
            },
            controllerAs: "vm",
            scope: {},
            templateUrl: "js/feed-mgr/feeds/edit-feed/details/feed-info.html",
            controller: "FeedInfoController"
        };
    };

    var FeedInfoController = function (FeedService) {
        this.model = FeedService.editFeedModel;

        // Determine table option
        if (this.model.registeredTemplate.templateTableOption === null) {
            if (this.model.registeredTemplate.defineTable) {
                this.model.registeredTemplate.templateTableOption = "DEFINE_TABLE";
            } else if (this.model.registeredTemplate.dataTransformation) {
                this.model.registeredTemplate.templateTableOption = "DATA_TRANSFORMATION";
            } else {
                this.model.registeredTemplate.templateTableOption = "NO_TABLE";
            }
        }
    };

    angular.module(moduleName).controller("FeedInfoController", ["FeedService", FeedInfoController]);
    angular.module(moduleName).directive("thinkbigFeedInfo", thinkbigFeedInfo);
});

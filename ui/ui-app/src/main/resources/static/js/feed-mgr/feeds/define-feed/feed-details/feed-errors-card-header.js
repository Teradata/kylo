define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var FeedErrorCardHeader = /** @class */ (function () {
        function FeedErrorCardHeader($scope, FeedCreationErrorService) {
            this.$scope = $scope;
            this.FeedCreationErrorService = FeedCreationErrorService;
        }
        FeedErrorCardHeader.prototype.$onInit = function () {
            this.ngOnInit();
        };
        FeedErrorCardHeader.prototype.ngOnInit = function () {
            var _this = this;
            this.$scope.hasFeedCreationErrors = function () {
                return _this.FeedCreationErrorService.hasErrors();
            };
            this.$scope.showFeedErrorsDialog = this.FeedCreationErrorService.showErrorDialog;
        };
        FeedErrorCardHeader.$inject = ["$scope", "FeedCreationErrorService"];
        return FeedErrorCardHeader;
    }());
    exports.FeedErrorCardHeader = FeedErrorCardHeader;
    angular.module(moduleName).
        component("thinkbigDefineFeedGeneralInfao", {
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/feed-errors-card-header.html',
    });
});
//# sourceMappingURL=feed-errors-card-header.js.map
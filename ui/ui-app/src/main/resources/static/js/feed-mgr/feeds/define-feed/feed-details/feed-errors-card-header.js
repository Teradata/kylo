define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var directive = function (FeedCreationErrorService) {
        return {
            restrict: "EA",
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/feed-errors-card-header.html',
            link: function ($scope, element, attrs) {
                $scope.hasFeedCreationErrors = function () {
                    return FeedCreationErrorService.hasErrors();
                };
                $scope.showFeedErrorsDialog = FeedCreationErrorService.showErrorDialog;
            }
        };
    };
    var FeedErrorCardHeader = /** @class */ (function () {
        function FeedErrorCardHeader() {
        }
        return FeedErrorCardHeader;
    }());
    exports.FeedErrorCardHeader = FeedErrorCardHeader;
    angular.module(moduleName)
        .directive('thinkbigFeedErrorsCardHeader', ["FeedCreationErrorService", directive]);
});
//# sourceMappingURL=feed-errors-card-header.js.map
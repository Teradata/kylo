
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

var directive = function (FeedCreationErrorService:any) {
    return {
        restrict: "EA",
        scope: {
        },
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/feed-errors-card-header.html',
        link: function ($scope:any, element:any, attrs:any) {

            $scope.hasFeedCreationErrors = function() {
                return FeedCreationErrorService.hasErrors();
            };
            $scope.showFeedErrorsDialog = FeedCreationErrorService.showErrorDialog;

        }

    };
}
export class FeedErrorCardHeader{

}
angular.module(moduleName)
    .directive('thinkbigFeedErrorsCardHeader', ["FeedCreationErrorService",directive]);

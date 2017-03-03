define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var directive = function (FeedCreationErrorService) {
        return {
            restrict: "EA",
            scope: {
            },
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/feed-errors-card-header.html',
            link: function ($scope, element, attrs) {

                $scope.hasFeedCreationErrors = function() {
                    return FeedCreationErrorService.hasErrors();
                };
                $scope.showFeedErrorsDialog = FeedCreationErrorService.showErrorDialog;

            }

        };
    }




    angular.module(moduleName)
        .directive('thinkbigFeedErrorsCardHeader', ["FeedCreationErrorService",directive]);

});

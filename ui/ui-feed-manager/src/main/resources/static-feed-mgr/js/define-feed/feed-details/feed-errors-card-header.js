(function () {

    var directive = function (FeedCreationErrorService) {
        return {
            restrict: "EA",
            scope: {
            },
            templateUrl: 'js/define-feed/feed-details/feed-errors-card-header.html',
            link: function ($scope, element, attrs) {

                $scope.hasFeedCreationErrors = function() {
                    return FeedCreationErrorService.hasErrors();
                };
                $scope.showFeedErrorsDialog = FeedCreationErrorService.showErrorDialog;

            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedErrorsCardHeader', directive);

})();

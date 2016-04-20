(function () {

    var directive = function ($http,RestUrlService) {
        return {
            restrict: "EA",
            scope: {
                cronExpression: '='
            },
            templateUrl: 'js/shared/cron-expression-preview/cron-expression-preview.html',
            link: function ($scope, element, attrs) {

                $scope.nextDates = [];

               function getNextDates() {
                    $http.get(RestUrlService.PREVIEW_CRON_EXPRESSION_URL,{params:{cronExpression:$scope.cronExpression}}).then(function (response) {
                        $scope.nextDates = response.data;
                    });
                }

                $scope.$watch('cronExpression',function(newVal) {
                    if(newVal != null && newVal != ''){
                        getNextDates();
                    }
                    else {
                        $scope.nextDates = [];
                    }
                });
                getNextDates();
            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('cronExpressionPreview', ['$http','RestUrlService',directive]);

})();

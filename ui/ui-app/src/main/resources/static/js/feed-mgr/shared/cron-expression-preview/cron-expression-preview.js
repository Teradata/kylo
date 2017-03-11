define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {

    var directive = function ($http,RestUrlService) {
        return {
            restrict: "EA",
            scope: {
                cronExpression: '='
            },
            templateUrl: 'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.html',
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




    angular.module(moduleName)
        .directive('cronExpressionPreview', ['$http','RestUrlService',directive]);

});

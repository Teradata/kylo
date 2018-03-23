
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');



var directive = function ($http:any,RestUrlService:any) {
    return {
        restrict: "EA",
        scope: {
            cronExpression: '='
        },
        templateUrl: 'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.html',
        link: function ($scope:any, element:any, attrs:any) {

            $scope.nextDates = [];

           function getNextDates() {
                $http.get(RestUrlService.PREVIEW_CRON_EXPRESSION_URL,{params:{cronExpression:$scope.cronExpression}}).then(function (response:any) {
                    $scope.nextDates = response.data;
                });
            }

            $scope.$watch('cronExpression',function(newVal:any) {
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

export class CronExpressionPreview {

}

angular.module(moduleName)
    .directive('cronExpressionPreview', ['$http','RestUrlService',directive]);



import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');



export class CronExpressionValidator {
        
}
angular.module(moduleName).directive('cronExpressionValidator', ['RestUrlService', '$q', '$http', function (RestUrlService:any, $q:any, $http:any) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope:any, elm:any, attrs:any, ctrl:any) {
            ctrl.$asyncValidators.cronExpression = function (modelValue:any, viewValue:any) {
                var deferred = $q.defer();
                $http.get(RestUrlService.VALIDATE_CRON_EXPRESSION_URL, {params: {cronExpression: viewValue}}).then(function (response:any) {

                    if (response.data.valid == false) {
                        deferred.reject("Invalid Cron Expression");
                    } else {
                        deferred.resolve()
                    }
                });
                return deferred.promise;

            }
        }
    }
}]);

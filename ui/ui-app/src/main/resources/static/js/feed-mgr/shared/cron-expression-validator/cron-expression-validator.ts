
import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;

var directive = function (RestUrlService:any, $q:angular.IQService, $http:angular.IHttpService) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: (scope:any, elm:any, attrs:any, ctrl:any) => {
            ctrl.$asyncValidators.cronExpression = (modelValue:any, viewValue:any) => {
                var deferred = $q.defer();
                $http.get(RestUrlService.VALIDATE_CRON_EXPRESSION_URL, {params: {cronExpression: viewValue}}).then( (response:any) => {

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
};

export class CronExpressionValidator {
        
}
angular.module(moduleName).directive('cronExpressionValidator', ['RestUrlService', '$q', '$http', directive]);

define(['angular'], function (angular) {
    angular.module("kylo.feedmgr.definefeed").directive('cronExpressionValidator', ['RestUrlService', '$q', '$http', function (RestUrlService, $q, $http) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$asyncValidators.cronExpression = function (modelValue, viewValue) {
                    var deferred = $q.defer();
                    $http.get(RestUrlService.VALIDATE_CRON_EXPRESSION_URL, {params: {cronExpression: viewValue}}).then(function (response) {

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
});
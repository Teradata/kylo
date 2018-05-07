define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var CronExpressionValidator = /** @class */ (function () {
        function CronExpressionValidator() {
        }
        return CronExpressionValidator;
    }());
    exports.CronExpressionValidator = CronExpressionValidator;
    angular.module(moduleName).directive('cronExpressionValidator', ['RestUrlService', '$q', '$http', function (RestUrlService, $q, $http) {
            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, elm, attrs, ctrl) {
                    ctrl.$asyncValidators.cronExpression = function (modelValue, viewValue) {
                        var deferred = $q.defer();
                        $http.get(RestUrlService.VALIDATE_CRON_EXPRESSION_URL, { params: { cronExpression: viewValue } }).then(function (response) {
                            if (response.data.valid == false) {
                                deferred.reject("Invalid Cron Expression");
                            }
                            else {
                                deferred.resolve();
                            }
                        });
                        return deferred.promise;
                    };
                }
            };
        }]);
});
//# sourceMappingURL=cron-expression-validator.js.map
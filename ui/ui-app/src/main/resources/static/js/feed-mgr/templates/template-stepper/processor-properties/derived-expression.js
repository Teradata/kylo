define(["require", "exports", "angular", "../../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var directive = function (RegisterTemplateService) {
        return {
            restrict: "A",
            scope: {
                ngModel: '='
            },
            link: function ($scope, $element, attrs, controller) {
                $scope.$watch('ngModel', function (newValue) {
                    if (newValue != null) {
                        var derivedValue = RegisterTemplateService.deriveExpression(newValue, true);
                        $element.html(derivedValue);
                    }
                    else {
                        $element.html('');
                    }
                });
            }
        };
    };
    angular.module(module_name_1.moduleName)
        .directive('thinkbigDerivedExpression', ['RegisterTemplateService', directive]);
});
//# sourceMappingURL=derived-expression.js.map
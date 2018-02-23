define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/templates/module-name');
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
    angular.module(moduleName)
        .directive('thinkbigDerivedExpression', ['RegisterTemplateService', directive]);
});
//# sourceMappingURL=derived-expression.js.map
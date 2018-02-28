define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("cardLayout", function ($compile) {
        return {
            scope: { headerCss: "@", bodyCss: "@", cardCss: '@', cardToolbar: "=?" },
            transclude: {
                'header1': '?headerSection',
                'body1': '?bodySection'
            },
            templateUrl: 'js/common/card-layout/card-layout.html',
            link: function (scope, iElem, iAttrs, ctrl, transcludeFn) {
                if (angular.isUndefined(scope.cardToolbar)) {
                    scope.cardToolbar = true;
                }
            }
        };
    });
});
//# sourceMappingURL=card-layout.js.map
define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("tbaViewTypeSelection", [function () {
            return {
                restrict: 'E',
                templateUrl: 'js/common/view-type-selection/view-type-selection-template.html',
                scope: {
                    viewType: '='
                },
                link: function ($scope, elem, attr) {
                    $scope.viewTypeChanged = function (viewType) {
                        $scope.viewType = viewType;
                    };
                }
            };
        }
    ]);
});
//# sourceMappingURL=view-type-selection.js.map
define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Draggabilly = require("../../../bower_components/draggabilly/dist/draggabilly.pkgd");
    angular.module(module_name_1.moduleName).directive('ngDraggable', [function () {
            return {
                restrict: 'A',
                scope: {
                    dragOptions: '=ngDraggable'
                },
                link: function (scope, elem, attr) {
                    if (scope.dragOptions == undefined) {
                        scope.dragOptions = {};
                    }
                    var draggable = new Draggabilly(elem[0], scope.dragOptions);
                }
            };
        }]);
});
//# sourceMappingURL=draggable.js.map
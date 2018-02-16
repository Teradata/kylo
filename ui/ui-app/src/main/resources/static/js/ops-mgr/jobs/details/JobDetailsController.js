define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($transition$) {
            this.$transition$ = $transition$;
            this.executionId = $transition$.params().executionId;
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('JobDetailsController', ['$transition$', controller]);
});
//# sourceMappingURL=JobDetailsController.js.map
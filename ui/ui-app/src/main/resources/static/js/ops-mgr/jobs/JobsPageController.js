define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($transition$) {
            this.$transition$ = $transition$;
            this.filter = $transition$.params().filter;
            this.tab = $transition$.params().tab;
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('JobsPageController', ['$transition$', controller]);
});
//# sourceMappingURL=JobsPageController.js.map
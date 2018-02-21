define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($transition$) {
            this.$transition$ = $transition$;
            this.filter = this.$transition$.params().filter;
            this.slaId = this.$transition$.params().slaId;
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('ServiceLevelAssessmentsInitController', ['$transition$', controller]);
});
//# sourceMappingURL=ServiceLevelAssessmentsInitController.js.map
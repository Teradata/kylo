define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ServiceLevelAgreementInitController = /** @class */ (function () {
        function ServiceLevelAgreementInitController($transition$) {
            this.$transition$ = $transition$;
            this.slaId = this.$transition$.params().slaId;
        }
        return ServiceLevelAgreementInitController;
    }());
    exports.default = ServiceLevelAgreementInitController;
    angular.module(module_name_1.moduleName).controller('ServiceLevelAgreementInitController', ['$transition$', ServiceLevelAgreementInitController]);
});
//# sourceMappingURL=ServiceLevelAgreementInitController.js.map
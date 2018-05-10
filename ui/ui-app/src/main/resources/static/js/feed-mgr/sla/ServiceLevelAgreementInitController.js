define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ServiceLevelAgreementInitController = /** @class */ (function () {
        function ServiceLevelAgreementInitController() {
        }
        ServiceLevelAgreementInitController.prototype.ngOnInit = function () {
            this.slaId = this.$transition$.params().slaId;
        };
        ServiceLevelAgreementInitController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        return ServiceLevelAgreementInitController;
    }());
    exports.default = ServiceLevelAgreementInitController;
    angular.module(module_name_1.moduleName).component('serviceLevelAgreementInitComponent', {
        bindings: {
            $transition$: '<'
        },
        controller: ServiceLevelAgreementInitController,
        controllerAs: "vm",
        templateUrl: "js/feed-mgr/sla/service-level-agreements-view.html"
    });
});
//# sourceMappingURL=ServiceLevelAgreementInitController.js.map
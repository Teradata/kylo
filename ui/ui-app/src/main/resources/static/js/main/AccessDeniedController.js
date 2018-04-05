define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessDeniedController = /** @class */ (function () {
        function AccessDeniedController($scope, $mdDialog, AccessControlService, StateService) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.AccessControlService = AccessControlService;
            this.StateService = StateService;
            var attemptedState = this.$transition$.params().attemptedState;
            if (attemptedState == undefined) {
                attemptedState = { displayName: 'the page' };
            }
            else if (attemptedState.displayName == undefined) {
                attemptedState.displayName = attemptedState.name;
            }
        }
        AccessDeniedController.$inject = ["$scope", "$mdDialog", "AccessControlService", "StateService"];
        return AccessDeniedController;
    }());
    exports.AccessDeniedController = AccessDeniedController;
    angular.module('kylo').component("accessDeniedController", {
        controller: AccessDeniedController,
        controllerAs: "vm",
        templateUrl: "js/main/access-denied.html"
    });
});
//.controller('AccessDeniedController',[AccessDeniedController]);
//# sourceMappingURL=AccessDeniedController.js.map
define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessDeniedController = /** @class */ (function () {
        function AccessDeniedController($scope, $mdDialog, AccessControlService, StateService, $transition$) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.AccessControlService = AccessControlService;
            this.StateService = StateService;
            this.$transition$ = $transition$;
            var attemptedState = this.$transition$.params().attemptedState;
            if (attemptedState == undefined) {
                attemptedState = { displayName: 'the page' };
            }
            else if (attemptedState.displayName == undefined) {
                attemptedState.displayName = attemptedState.name;
            }
        }
        return AccessDeniedController;
    }());
    exports.AccessDeniedController = AccessDeniedController;
    angular.module('kylo').controller('AccessDeniedController', ["$scope",
        "$mdDialog",
        "AccessControlService",
        "StateService",
        "$transition$",
        AccessDeniedController]);
});
//# sourceMappingURL=AccessDeniedController.js.map
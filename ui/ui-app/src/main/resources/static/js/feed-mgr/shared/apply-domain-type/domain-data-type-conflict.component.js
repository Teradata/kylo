define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Dialog for resolving a data type conflict between a column and its domain type.
     */
    var DomainDataTypeConflictDialog = /** @class */ (function () {
        function DomainDataTypeConflictDialog($scope, $mdDialog, data) {
            this.$mdDialog = $mdDialog;
            $scope.cancel = this.cancel.bind(this);
            $scope.columnDef = data.columnDef;
            $scope.domainType = data.domainType;
            $scope.keep = this.keep.bind(this);
            $scope.remove = this.remove.bind(this);
        }
        /**
         * Close this dialog and reject promise.
         */
        DomainDataTypeConflictDialog.prototype.cancel = function () {
            this.$mdDialog.cancel();
        };
        /**
         * Close this dialog and resolve promise with {@code true}.
         */
        DomainDataTypeConflictDialog.prototype.keep = function () {
            this.$mdDialog.hide(true);
        };
        /**
         * Close this dialog and resolve promise with {@code false}.
         */
        DomainDataTypeConflictDialog.prototype.remove = function () {
            this.$mdDialog.hide(false);
        };
        DomainDataTypeConflictDialog.$inject = ["$scope", "$mdDialog", "data"];
        return DomainDataTypeConflictDialog;
    }());
    exports.DomainDataTypeConflictDialog = DomainDataTypeConflictDialog;
    angular.module(require("feed-mgr/module-name"))
        .controller("DomainDataTypeConflictDialog", DomainDataTypeConflictDialog);
});
//# sourceMappingURL=domain-data-type-conflict.component.js.map
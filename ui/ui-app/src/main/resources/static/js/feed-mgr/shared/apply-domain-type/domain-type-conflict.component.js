define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Dialog for resolving a data type conflict between a column and its domain type.
     */
    var DomainTypeConflictDialog = /** @class */ (function () {
        function DomainTypeConflictDialog($scope, $mdDialog, data) {
            this.$mdDialog = $mdDialog;
            $scope.cancel = this.cancel.bind(this);
            $scope.columnDef = data.columnDef;
            $scope.domainType = data.domainType;
            $scope.keep = this.keep.bind(this);
            $scope.remove = this.remove.bind(this);
            // Determine conflicting property
            if (data.domainType.field.name && data.columnDef.name !== data.domainType.field.name) {
                $scope.propertyName = "field name";
                $scope.propertyValue = data.domainType.field.name;
            }
            else if (data.domainType.field.derivedDataType && data.columnDef.derivedDataType !== data.domainType.field.derivedDataType) {
                $scope.propertyName = "data type";
                $scope.propertyValue = data.domainType.field.derivedDataType;
            }
        }
        /**
         * Close this dialog and reject promise.
         */
        DomainTypeConflictDialog.prototype.cancel = function () {
            this.$mdDialog.cancel();
        };
        /**
         * Close this dialog and resolve promise with {@code true}.
         */
        DomainTypeConflictDialog.prototype.keep = function () {
            this.$mdDialog.hide(true);
        };
        /**
         * Close this dialog and resolve promise with {@code false}.
         */
        DomainTypeConflictDialog.prototype.remove = function () {
            this.$mdDialog.hide(false);
        };
        DomainTypeConflictDialog.$inject = ["$scope", "$mdDialog", "data"];
        return DomainTypeConflictDialog;
    }());
    exports.DomainTypeConflictDialog = DomainTypeConflictDialog;
    angular.module(require("feed-mgr/module-name"))
        .controller("DomainTypeConflictDialog", DomainTypeConflictDialog);
});
//# sourceMappingURL=domain-type-conflict.component.js.map
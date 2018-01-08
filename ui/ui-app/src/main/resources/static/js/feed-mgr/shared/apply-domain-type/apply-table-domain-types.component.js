define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Dialog for resolving conflicts between detected data type of columns and detected domain type.
     */
    var ApplyTableDomainTypesDialog = /** @class */ (function () {
        function ApplyTableDomainTypesDialog($scope, $mdDialog, $templateCache, data) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.data = data;
            /**
             * Table options.
             */
            this.gridOptions = {
                columnDefs: [
                    { name: "name", displayName: "Column" },
                    { name: "domainType", displayName: "Domain Type", cellTemplate: ApplyTableDomainTypesDialog.DOMAIN_TYPE_CELL_TEMPLATE },
                    { name: "newName", displayName: "New Name" },
                    { name: "dataType", displayName: "Original Type" },
                    { name: "newType", displayName: "New Type" }
                ],
                enableColumnMenus: false,
                enableRowSelection: true,
                enableSelectAll: true,
                minRowsToShow: 5,
                multiSelect: true,
                onRegisterApi: function (gridApi) {
                    _this.gridApi = gridApi;
                    window.setTimeout(function () { return gridApi.selection.selectAllRows(); });
                },
                rowHeight: 35,
                selectionRowHeaderWidth: 35,
                showGridFooter: true
            };
            // Configure scope
            $scope.gridOptions = this.gridOptions;
            $scope.cancel = this.cancel.bind(this);
            $scope.hide = this.hide.bind(this);
            // Add template to cache
            $templateCache.put(ApplyTableDomainTypesDialog.DOMAIN_TYPE_CELL_TEMPLATE, "\n            <div class=\"ui-grid-cell-contents\">\n              <span><ng-md-icon icon=\"{{row.entity.domainType.icon}}\" ng-style=\"{'fill':row.entity.domainType.iconColor}\"></ng-md-icon> {{row.entity.domainType.title}}</span>\n            </div>\n        ");
            // Add table data
            this.gridOptions.data = data.fields.map(function (field, index) {
                var domainType = data.domainTypes[index];
                return {
                    name: field.name,
                    dataType: field.derivedDataType,
                    domainType: domainType,
                    newName: domainType.field.name ? domainType.field.name : field.name,
                    newType: domainType.field.derivedDataType ? domainType.field.derivedDataType : field.derivedDataType
                };
            });
        }
        /**
         * Close the dialog and reject promise.
         */
        ApplyTableDomainTypesDialog.prototype.cancel = function () {
            this.$mdDialog.cancel();
        };
        /**
         * Close the dialog and resolve promise.
         */
        ApplyTableDomainTypesDialog.prototype.hide = function () {
            this.$mdDialog.hide(this.gridApi.selection.getSelectedRows());
        };
        /**
         * Name of cell template for the domain type column.
         */
        ApplyTableDomainTypesDialog.DOMAIN_TYPE_CELL_TEMPLATE = "apply-table-domain-types/domain-type-cell";
        ApplyTableDomainTypesDialog.$inject = ["$scope", "$mdDialog", "$templateCache", "data"];
        return ApplyTableDomainTypesDialog;
    }());
    exports.ApplyTableDomainTypesDialog = ApplyTableDomainTypesDialog;
    angular.module(require("feed-mgr/module-name"))
        .controller("ApplyTableDomainTypesDialog", ApplyTableDomainTypesDialog);
});
//# sourceMappingURL=apply-table-domain-types.component.js.map
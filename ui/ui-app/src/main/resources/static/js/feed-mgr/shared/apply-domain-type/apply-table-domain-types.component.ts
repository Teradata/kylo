import * as angular from "angular";
import * as uiGrid from "ui-grid";

import {DomainType} from "../../services/DomainTypesService";

/**
 * Local data for {@link ApplyTableDomainTypesDialog}.
 */
export interface ApplyTableDomainTypesData {

    /**
     * Detected domain type corresponding to each field.
     */
    domainTypes: DomainType[];

    /**
     * Column definitions.
     */
    fields: any[];
}

/**
 * Dialog for resolving conflicts between detected data type of columns and detected domain type.
 */
export class ApplyTableDomainTypesDialog {

    /**
     * Name of cell template for the domain type column.
     */
    static readonly DOMAIN_TYPE_CELL_TEMPLATE = "apply-table-domain-types/domain-type-cell";

    /**
     * Table listing conflicts.
     */
    gridApi: uiGrid.IGridApi;

    /**
     * Table options.
     */
    gridOptions: uiGrid.IGridOptions = {
        columnDefs: [
            {name: "name", displayName: "Column"},
            {name: "domainType", displayName: "Domain Type", cellTemplate: ApplyTableDomainTypesDialog.DOMAIN_TYPE_CELL_TEMPLATE},
            {name: "newName", displayName: "New Name"},
            {name: "dataType", displayName: "Original Type"},
            {name: "newType", displayName: "New Type"}
        ],
        enableColumnMenus: false,
        enableRowSelection: true,
        enableSelectAll: true,
        minRowsToShow: 5,
        multiSelect: true,
        onRegisterApi: gridApi => {
            this.gridApi = gridApi;
            window.setTimeout(() => gridApi.selection.selectAllRows());
        },
        rowHeight: 35,
        selectionRowHeaderWidth: 35,
        showGridFooter: true
    };

    static readonly $inject: string[] = ["$scope", "$mdDialog", "$templateCache", "data"];

    constructor(private $scope: angular.IScope, private $mdDialog: angular.material.IDialogService, $templateCache: angular.ITemplateCacheService, private data: ApplyTableDomainTypesData) {
        // Configure scope
        ($scope as any).gridOptions = this.gridOptions;
        ($scope as any).cancel = this.cancel.bind(this);
        ($scope as any).hide = this.hide.bind(this);

        // Add template to cache
        $templateCache.put(ApplyTableDomainTypesDialog.DOMAIN_TYPE_CELL_TEMPLATE, `
            <div class="ui-grid-cell-contents">
              <span><ng-md-icon icon="{{row.entity.domainType.icon}}" ng-style="{'fill':row.entity.domainType.iconColor}"></ng-md-icon> {{row.entity.domainType.title}}</span>
            </div>
        `);

        // Add table data
        this.gridOptions.data = data.fields.map((field, index) => {
            const domainType = data.domainTypes[index];
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
    cancel() {
        this.$mdDialog.cancel();
    }

    /**
     * Close the dialog and resolve promise.
     */
    hide() {
        this.$mdDialog.hide(this.gridApi.selection.getSelectedRows());
    }
}

angular.module(require("feed-mgr/module-name"))
    .controller("ApplyTableDomainTypesDialog", ApplyTableDomainTypesDialog);

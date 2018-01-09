import * as angular from "angular";

import {DomainType} from "../../services/DomainTypesService";

/**
 * Local data for {@link DomainTypeConflictDialog}.
 */
export interface DomainTypeConflictData {

    /**
     * Field definition.
     */
    columnDef: any;

    /**
     * Domain type.
     */
    domainType: DomainType;
}

/**
 * Dialog for resolving a data type conflict between a column and its domain type.
 */
export class DomainTypeConflictDialog {

    static readonly $inject: string[] = ["$scope", "$mdDialog", "data"];

    constructor($scope: any, private $mdDialog: angular.material.IDialogService, data: DomainTypeConflictData) {
        $scope.cancel = this.cancel.bind(this);
        $scope.columnDef = data.columnDef;
        $scope.domainType = data.domainType;
        $scope.keep = this.keep.bind(this);
        $scope.remove = this.remove.bind(this);

        // Determine conflicting property
        if (data.domainType.field.name && data.columnDef.name !== data.domainType.field.name) {
            $scope.propertyName = "field name";
            $scope.propertyValue = data.domainType.field.name;
        } else if (data.domainType.field.derivedDataType && data.columnDef.derivedDataType !== data.domainType.field.derivedDataType) {
            $scope.propertyName = "data type";
            $scope.propertyValue = data.domainType.field.derivedDataType;
        }
    }

    /**
     * Close this dialog and reject promise.
     */
    cancel() {
        this.$mdDialog.cancel();
    }

    /**
     * Close this dialog and resolve promise with {@code true}.
     */
    keep() {
        this.$mdDialog.hide(true);
    }

    /**
     * Close this dialog and resolve promise with {@code false}.
     */
    remove() {
        this.$mdDialog.hide(false);
    }
}

angular.module(require("feed-mgr/module-name"))
    .controller("DomainTypeConflictDialog", DomainTypeConflictDialog);

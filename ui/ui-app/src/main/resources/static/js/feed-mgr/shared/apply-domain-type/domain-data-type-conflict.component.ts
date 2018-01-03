import * as angular from "angular";

import {DomainType} from "../../services/DomainTypesService";

/**
 * Local data for {@link DomainDataTypeConflictDialog}.
 */
export interface DomainDataTypeConflictData {

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
export class DomainDataTypeConflictDialog {

    static readonly $inject: string[] = ["$scope", "$mdDialog", "data"];

    constructor($scope: any, private $mdDialog: angular.material.IDialogService, data: DomainDataTypeConflictData) {
        $scope.cancel = this.cancel.bind(this);
        $scope.columnDef = data.columnDef;
        $scope.domainType = data.domainType;
        $scope.keep = this.keep.bind(this);
        $scope.remove = this.remove.bind(this);
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
    .controller("DomainDataTypeConflictDialog", DomainDataTypeConflictDialog);

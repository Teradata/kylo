import {DomainType} from "../../../services/DomainTypesService";
import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";


export enum  DomainTypeConflictDialogResponse {
    Keep = 1, Remove = 2,Cancel = 3
}

/**
 * Local data for {@link DomainTypeConflictDialogComponent}.
 */
export interface DomainTypeConflictDialogData {

    /**
     * Field definition.
     */
    columnDef: TableColumnDefinition;

    /**
     * Domain type.
     */
    domainType: DomainType;

    propertyName: string;

    propertyValue: string;
}

/**
 * Dialog for resolving a data type conflict between a column and its domain type.
 */
@Component({
    selector:"domain-type-conflict-dialog",
    templateUrl: "./domain-type-conflict-dialog.component.html"
})
export class DomainTypeConflictDialogComponent implements OnInit, OnDestroy{

    public propertyName:string;
    public propertyValue:string;

    constructor(
        public dialogRef: MatDialogRef<DomainTypeConflictDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: DomainTypeConflictDialogData) {
     }

    ngOnInit(){
        if (this.data.domainType.field.name && this.data.columnDef.name !== this.data.domainType.field.name) {
            this.propertyName = "field name";
            this.propertyValue = this.data.domainType.field.name;
        }
        else if (this.data.domainType.field.derivedDataType && this.data.columnDef.derivedDataType !== this.data.domainType.field.derivedDataType) {
            this.propertyName = "data type";
            this.propertyValue = this.data.domainType.field.derivedDataType;
        }
    }
    ngOnDestroy() {

    }

    /**
     * Close this dialog and reject promise.
     */
    cancel() {
        this.dialogRef.close(DomainTypeConflictDialogResponse.Cancel);
    }

    /**
     * Close this dialog and resolve promise with {@code true}.
     */
    keep() {
        this.dialogRef.close(DomainTypeConflictDialogResponse.Keep);
    }

    /**
     * Close this dialog and resolve promise with {@code false}.
     */
    remove() {
        this.dialogRef.close(DomainTypeConflictDialogResponse.Remove);
    }
}

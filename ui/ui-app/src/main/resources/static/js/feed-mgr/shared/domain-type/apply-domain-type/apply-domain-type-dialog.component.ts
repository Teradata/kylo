import * as _ from "underscore";
import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {SelectedColumn} from "../../../feeds/define-feed-ng2/steps/define-table/feed-table-selected-column.model";
import {DomainType} from "../../../services/DomainTypesService";


export enum  ApplyDomainTypeDialogDataResponse {
   Apply=1,Cancel=2
}

/**
 * Local data for {@link ApplyDomainTypeDialogComponent}.
 */
export interface ApplyDomainTypeDialogData {

    /**
     * Field definition.
     */
    column: SelectedColumn;

    /**
     * Domain type.
     */
    domainType: DomainType;
}

/**
 * Controller for the dialog that confirms overwriting a field with domain type policies.
 */
@Component({
    selector:"apply-domain-type-dialog",
    templateUrl: "./apply-domain-type-dialog.component.html"
})
export class ApplyDomainTypeDialogComponent implements OnInit, OnDestroy {

    constructor(
        public dialogRef: MatDialogRef<ApplyDomainTypeDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ApplyDomainTypeDialogData) {
    }

    ngOnInit() {


    }

    ngOnDestroy() {

    }

    /**
     * Close this dialog and reject promise.
     */
    cancel() {
        this.dialogRef.close(ApplyDomainTypeDialogDataResponse.Cancel);
    }

    /**
     * Close this dialog and resolve promise with Apply
     */
    apply() {
        this.dialogRef.close(ApplyDomainTypeDialogDataResponse.Apply);
    }

}
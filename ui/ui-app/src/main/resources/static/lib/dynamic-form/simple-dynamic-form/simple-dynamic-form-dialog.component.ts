import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {FormConfig} from "../services/dynamic-form-builder";
import {DynamicFormDialogData} from "./dynamic-form-dialog-data";

@Component({
    selector:"simple-dynamic-form-dialog",
    templateUrl: "./simple-dynamic-form-dialog.component.html"
})
export class SimpleDynamicFormDialogComponent implements OnInit, OnDestroy{

    formConfig:FormConfig;

    constructor(private dialog: MatDialogRef<SimpleDynamicFormDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DynamicFormDialogData) {
      this.formConfig = this.data.formConfig;
    }

    ngOnInit() {

    }
    ngOnDestroy(){

    }


    /**
     * Closes this dialog and returns the form value.
     */
    apply() {
        if(this.formConfig.onApplyFn){
            this.formConfig.onApplyFn(this.formConfig.form.value);
        }
        this.dialog.close(this.formConfig.form.value);
    }

    /**
     * Cancel this dialog.
     */
    cancel() {
        if(this.formConfig.onCancelFn){
            this.formConfig.onCancelFn();
        }
        this.dialog.close();
    }


}

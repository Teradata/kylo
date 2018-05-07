import * as angular from "angular";
import {Component, Inject} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import "rxjs/add/observable/merge";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/do";

import {ImputeMissingConfig} from "../../api/services/dialog.service";

/**
 * A prompt dialog for providing a date format pattern that can be used for formatting or parsing dates or timestamps.
 */
@Component({
    styleUrls: ["js/feed-mgr/visual-query/wrangler/core/columns/impute-missing.component.css"],
    templateUrl: "js/feed-mgr/visual-query/wrangler/core/columns/impute-missing.component.html"
})
export class ImputeMissingDialog {

    /**
     * Main form group.
     */
    form: FormGroup;

    /**
     * Dialog description message.
     */
    message: string;

    /**
     * Type control
     */
    //type: FormControl;

    /**
     * Group by control
     */
    groupBy: FormControl;

    /**
     * Order by control
     */
    orderBy: FormControl;

    /**
     * Dialog title.
     */
    title: string = "Impute Missing";

    /**
     * Field options
     */
    fieldOptions: object[] = [];

    /**
     * Timestamp unit control.
     */
    unit: FormControl;

    constructor(private dialog: MatDialogRef<ImputeMissingDialog>, formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) data: ImputeMissingConfig) {
        //this.allowType = (typeof data.type === "undefined");
        const self = this;
        this.message = data.message;

        // Generate field list
        this.fieldOptions = [];
        angular.forEach(data.fields, value => {
            self.fieldOptions.push({value: value, viewValue: value});
        });

        // Create form
        this.form = formBuilder.group({

        });

        this.groupBy = new FormControl("", Validators.required);
        this.orderBy = new FormControl("", Validators.required);
        this.form.addControl("groupBy",this.groupBy);
        this.form.addControl("orderBy",this.orderBy);
    }

    /**
     * Gets the value of the type control.
     */
    get type() {
        return this.form.get("type");
    }

    /**
     * Closes this dialog and returns the form value.
     */
    apply() {
        this.dialog.close(this.form.value);
    }

    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close();
    }



}

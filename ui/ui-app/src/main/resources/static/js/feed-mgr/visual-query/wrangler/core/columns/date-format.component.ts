import {Component, Inject} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import "rxjs/add/observable/merge";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/do";
import {Observable} from "rxjs/Observable";

import {DateFormatConfig, DateFormatResponse, DateFormatType} from "../../api/services/dialog.service";

/**
 * A prompt dialog for providing a date format pattern that can be used for formatting or parsing dates or timestamps.
 */
@Component({
    styleUrls: ["js/feed-mgr/visual-query/wrangler/core/columns/date-format.component.css"],
    templateUrl: "js/feed-mgr/visual-query/wrangler/core/columns/date-format.component.html"
})
export class DateFormatDialog {

    /**
     * Indicates that the type field is editable.
     */
    allowType = true;

    /**
     * Main form group.
     */
    form: FormGroup;

    /**
     * Dialog description message.
     */
    message: string;

    /**
     * Date format pattern control.
     */
    pattern: FormControl;

    /**
     * Hint text for pattern control.
     */
    patternHint: string;

    /**
     * Value of the preview control.
     */
    preview: string;

    /**
     * Error message for preview control.
     */
    previewError: string;

    /**
     * Validator function
     */
    previewFn: (format: DateFormatResponse) => Observable<string>;

    /**
     * Indicates that the preview is being calculated.
     */
    previewLoading = false;

    /**
     * Dialog title.
     */
    title: string = "Date format";

    /**
     * Timestamp unit control.
     */
    unit: FormControl;

    constructor(private dialog: MatDialogRef<DateFormatDialog>, formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) data: DateFormatConfig) {
        this.allowType = (typeof data.type === "undefined");
        this.message = data.message;
        this.patternHint = data.patternHint;
        this.previewFn = data.preview;

        if (typeof data.title !== "undefined") {
            this.title = data.title;
        }

        // Create form
        this.form = formBuilder.group({
            type: [(typeof data.type !== "undefined") ? data.type : "", Validators.required]
        });
        this.type.valueChanges.forEach(() => this.onTypeChange());

        this.pattern = new FormControl((typeof data.pattern !== "undefined") ? data.pattern : "", Validators.required);
        this.unit = new FormControl((typeof data.unit !== "undefined") ? data.unit : "", Validators.required);
        Observable.merge(this.pattern.valueChanges, this.unit.valueChanges)
            .do(() => this.previewLoading = this.canPreview)
            .debounceTime(300)
            .forEach(() => this.refreshPreview());

        // Update form controls
        this.onTypeChange();
    }

    /**
     * Indicates that the preview field is visible.
     */
    get canPreview() {
        return this.previewFn != null;
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

    /**
     * Called when the type control changes.
     */
    onTypeChange() {
        // Reset preview control
        this.preview = null;
        this.previewError = null;
        this.previewLoading = false;

        // Update pattern and unit controls
        let refreshPreview = false;

        if (this.type.value === DateFormatType.STRING) {
            this.form.addControl("pattern", this.pattern);
            this.form.removeControl("unit");
            refreshPreview = (this.pattern.value != null && this.pattern.value.length > 0);
        } else if (this.type.value === DateFormatType.TIMESTAMP) {
            this.form.removeControl("pattern");
            this.form.addControl("unit", this.unit);
            refreshPreview = (this.unit.value != null && this.unit.value != "");
        }

        // Refresh preview value
        if (this.canPreview && refreshPreview) {
            this.previewLoading = true;
            this.refreshPreview();
        }
    }

    /**
     * Refreshes the preview value.
     */
    private refreshPreview() {
        if (this.canPreview) {
            this.previewFn(this.form.value)
                .subscribe(value => {
                        this.preview = value;
                        this.previewError = null;
                        this.previewLoading = false;
                    },
                    error => {
                        this.preview = null;
                        this.previewError = error;
                        this.previewLoading = false;
                    });
        }
    }
}

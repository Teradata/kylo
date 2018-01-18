var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
define(["require", "exports", "@angular/core", "@angular/forms", "@angular/material/dialog", "rxjs/Observable", "../../api/services/dialog.service", "rxjs/add/observable/merge", "rxjs/add/operator/debounceTime", "rxjs/add/operator/do"], function (require, exports, core_1, forms_1, dialog_1, Observable_1, dialog_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * A prompt dialog for providing a date format pattern that can be used for formatting or parsing dates or timestamps.
     */
    var DateFormatDialog = /** @class */ (function () {
        function DateFormatDialog(dialog, formBuilder, data) {
            var _this = this;
            this.dialog = dialog;
            /**
             * Indicates that the type field is editable.
             */
            this.allowType = true;
            /**
             * Indicates that the preview is being calculated.
             */
            this.previewLoading = false;
            /**
             * Dialog title.
             */
            this.title = "Date format";
            this.allowType = (typeof data.type === "undefined");
            this.message = data.message;
            this.patternHint = data.patternHint;
            this.previewFn = data.preview;
            if (typeof data.title !== "undefined") {
                this.title = data.title;
            }
            // Create form
            this.form = formBuilder.group({
                type: [(typeof data.type !== "undefined") ? data.type : "", forms_1.Validators.required]
            });
            this.type.valueChanges.forEach(function () { return _this.onTypeChange(); });
            this.pattern = new forms_1.FormControl((typeof data.pattern !== "undefined") ? data.pattern : "", forms_1.Validators.required);
            this.unit = new forms_1.FormControl((typeof data.unit !== "undefined") ? data.unit : "", forms_1.Validators.required);
            Observable_1.Observable.merge(this.pattern.valueChanges, this.unit.valueChanges)
                .do(function () { return _this.previewLoading = _this.canPreview; })
                .debounceTime(300)
                .forEach(function () { return _this.refreshPreview(); });
            // Update form controls
            this.onTypeChange();
        }
        Object.defineProperty(DateFormatDialog.prototype, "canPreview", {
            /**
             * Indicates that the preview field is visible.
             */
            get: function () {
                return this.previewFn != null;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DateFormatDialog.prototype, "type", {
            /**
             * Gets the value of the type control.
             */
            get: function () {
                return this.form.get("type");
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Closes this dialog and returns the form value.
         */
        DateFormatDialog.prototype.apply = function () {
            this.dialog.close(this.form.value);
        };
        /**
         * Cancel this dialog.
         */
        DateFormatDialog.prototype.cancel = function () {
            this.dialog.close();
        };
        /**
         * Called when the type control changes.
         */
        DateFormatDialog.prototype.onTypeChange = function () {
            // Reset preview control
            this.preview = null;
            this.previewError = null;
            this.previewLoading = false;
            // Update pattern and unit controls
            var refreshPreview = false;
            if (this.type.value === dialog_service_1.DateFormatType.STRING) {
                this.form.addControl("pattern", this.pattern);
                this.form.removeControl("unit");
                refreshPreview = (this.pattern.value != null && this.pattern.value.length > 0);
            }
            else if (this.type.value === dialog_service_1.DateFormatType.TIMESTAMP) {
                this.form.removeControl("pattern");
                this.form.addControl("unit", this.unit);
                refreshPreview = (this.unit.value != null && this.unit.value != "");
            }
            // Refresh preview value
            if (this.canPreview && refreshPreview) {
                this.previewLoading = true;
                this.refreshPreview();
            }
        };
        /**
         * Refreshes the preview value.
         */
        DateFormatDialog.prototype.refreshPreview = function () {
            var _this = this;
            if (this.canPreview) {
                this.previewFn(this.form.value)
                    .subscribe(function (value) {
                    _this.preview = value;
                    _this.previewError = null;
                    _this.previewLoading = false;
                }, function (error) {
                    _this.preview = null;
                    _this.previewError = error;
                    _this.previewLoading = false;
                });
            }
        };
        DateFormatDialog = __decorate([
            core_1.Component({
                styleUrls: ["js/feed-mgr/visual-query/wrangler/core/columns/date-format.component.css"],
                templateUrl: "js/feed-mgr/visual-query/wrangler/core/columns/date-format.component.html"
            }),
            __param(2, core_1.Inject(dialog_1.MAT_DIALOG_DATA)),
            __metadata("design:paramtypes", [dialog_1.MatDialogRef, forms_1.FormBuilder, Object])
        ], DateFormatDialog);
        return DateFormatDialog;
    }());
    exports.DateFormatDialog = DateFormatDialog;
});
//# sourceMappingURL=date-format.component.js.map
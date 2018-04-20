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
define(["require", "exports", "angular", "@angular/core", "@angular/forms", "@angular/material/dialog", "rxjs/add/observable/merge", "rxjs/add/operator/debounceTime", "rxjs/add/operator/do"], function (require, exports, angular, core_1, forms_1, dialog_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * A prompt dialog for providing a date format pattern that can be used for formatting or parsing dates or timestamps.
     */
    var ImputeMissingDialog = /** @class */ (function () {
        function ImputeMissingDialog(dialog, formBuilder, data) {
            this.dialog = dialog;
            /**
             * Dialog title.
             */
            this.title = "Impute Missing";
            /**
             * Field options
             */
            this.fieldOptions = [];
            //this.allowType = (typeof data.type === "undefined");
            var self = this;
            this.message = data.message;
            // Generate field list
            this.fieldOptions = [];
            angular.forEach(data.fields, function (value) {
                self.fieldOptions.push({ value: value, viewValue: value });
            });
            // Create form
            this.form = formBuilder.group({});
            this.groupBy = new forms_1.FormControl("", forms_1.Validators.required);
            this.orderBy = new forms_1.FormControl("", forms_1.Validators.required);
            this.form.addControl("groupBy", this.groupBy);
            this.form.addControl("orderBy", this.orderBy);
        }
        Object.defineProperty(ImputeMissingDialog.prototype, "type", {
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
        ImputeMissingDialog.prototype.apply = function () {
            this.dialog.close(this.form.value);
        };
        /**
         * Cancel this dialog.
         */
        ImputeMissingDialog.prototype.cancel = function () {
            this.dialog.close();
        };
        ImputeMissingDialog = __decorate([
            core_1.Component({
                styleUrls: ["js/feed-mgr/visual-query/wrangler/core/columns/impute-missing.component.css"],
                templateUrl: "js/feed-mgr/visual-query/wrangler/core/columns/impute-missing.component.html"
            }),
            __param(2, core_1.Inject(dialog_1.MAT_DIALOG_DATA)),
            __metadata("design:paramtypes", [dialog_1.MatDialogRef, forms_1.FormBuilder, Object])
        ], ImputeMissingDialog);
        return ImputeMissingDialog;
    }());
    exports.ImputeMissingDialog = ImputeMissingDialog;
});
//# sourceMappingURL=impute-missing.component.js.map
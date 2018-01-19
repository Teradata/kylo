var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/common", "@angular/common/http", "@angular/core", "@angular/forms", "@angular/material/button", "@angular/material/form-field", "@angular/material/icon", "@angular/material/input", "@angular/material/progress-bar", "@angular/material/select", "@angular/material/toolbar", "@covalent/core", "../api/index", "./columns/date-format.component", "./services/dialog.service"], function (require, exports, common_1, http_1, core_1, forms_1, button_1, form_field_1, icon_1, input_1, progress_bar_1, select_1, toolbar_1, core_2, index_1, date_format_component_1, dialog_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     *
     */
    var WranglerModule = /** @class */ (function () {
        function WranglerModule() {
        }
        WranglerModule = __decorate([
            core_1.NgModule({
                declarations: [
                    date_format_component_1.DateFormatDialog
                ],
                entryComponents: [
                    date_format_component_1.DateFormatDialog
                ],
                imports: [
                    common_1.CommonModule,
                    core_2.CovalentDialogsModule,
                    http_1.HttpClientModule,
                    forms_1.FormsModule,
                    button_1.MatButtonModule,
                    form_field_1.MatFormFieldModule,
                    icon_1.MatIconModule,
                    input_1.MatInputModule,
                    progress_bar_1.MatProgressBarModule,
                    select_1.MatSelectModule,
                    toolbar_1.MatToolbarModule,
                    forms_1.ReactiveFormsModule
                ],
                providers: [
                    { provide: index_1.DIALOG_SERVICE, useClass: dialog_service_1.WranglerDialogService },
                    { provide: index_1.INJECTOR, useExisting: core_1.Injector },
                ]
            })
        ], WranglerModule);
        return WranglerModule;
    }());
    exports.WranglerModule = WranglerModule;
});
//# sourceMappingURL=wrangler.module.js.map
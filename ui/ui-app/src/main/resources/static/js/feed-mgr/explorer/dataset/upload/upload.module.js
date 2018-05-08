var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/common", "@angular/core", "@angular/flex-layout", "@angular/material/button", "@angular/material/card", "@angular/material/divider", "@angular/material/list", "@angular/material/progress-bar", "@covalent/core/file", "@uirouter/angular", "../../../../common/common.module", "../../catalog/catalog.module", "./upload.component", "./upload.states"], function (require, exports, common_1, core_1, flex_layout_1, button_1, card_1, divider_1, list_1, progress_bar_1, file_1, angular_1, common_module_1, catalog_module_1, upload_component_1, upload_states_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var UploadModule = /** @class */ (function () {
        function UploadModule() {
        }
        UploadModule = __decorate([
            core_1.NgModule({
                declarations: [
                    upload_component_1.UploadComponent
                ],
                entryComponents: [
                    upload_component_1.UploadComponent
                ],
                imports: [
                    catalog_module_1.CatalogModule,
                    common_1.CommonModule,
                    file_1.CovalentFileModule,
                    flex_layout_1.FlexLayoutModule,
                    common_module_1.KyloCommonModule,
                    button_1.MatButtonModule,
                    card_1.MatCardModule,
                    divider_1.MatDividerModule,
                    list_1.MatListModule,
                    progress_bar_1.MatProgressBarModule,
                    angular_1.UIRouterModule.forChild({ states: upload_states_1.uploadStates })
                ]
            })
        ], UploadModule);
        return UploadModule;
    }());
    exports.UploadModule = UploadModule;
});
//# sourceMappingURL=upload.module.js.map
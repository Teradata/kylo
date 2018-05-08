var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/common", "@angular/core", "@angular/material/button", "@angular/material/icon", "@angular/material/list", "@angular/material/menu", "@angular/material/progress-spinner", "@covalent/core/loading", "@covalent/core/menu", "@covalent/core/notifications", "../services/services.module", "./kylo-icon/kylo-icon.component", "./notifications/notification-menu.component"], function (require, exports, common_1, core_1, button_1, icon_1, list_1, menu_1, progress_spinner_1, loading_1, menu_2, notifications_1, services_module_1, kylo_icon_component_1, notification_menu_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var KyloCommonModule = /** @class */ (function () {
        function KyloCommonModule() {
        }
        KyloCommonModule = __decorate([
            core_1.NgModule({
                declarations: [
                    kylo_icon_component_1.KyloIconComponent,
                    notification_menu_component_1.NotificationMenuComponent
                ],
                entryComponents: [
                    notification_menu_component_1.NotificationMenuComponent
                ],
                imports: [
                    common_1.CommonModule,
                    loading_1.CovalentLoadingModule,
                    menu_2.CovalentMenuModule,
                    notifications_1.CovalentNotificationsModule,
                    services_module_1.KyloServicesModule,
                    button_1.MatButtonModule,
                    icon_1.MatIconModule,
                    list_1.MatListModule,
                    menu_1.MatMenuModule,
                    progress_spinner_1.MatProgressSpinnerModule
                ],
                exports: [
                    kylo_icon_component_1.KyloIconComponent
                ]
            })
        ], KyloCommonModule);
        return KyloCommonModule;
    }());
    exports.KyloCommonModule = KyloCommonModule;
});
//# sourceMappingURL=common.module.js.map
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/core", "@angular/platform-browser", "@angular/upgrade/static", "./angular2"], function (require, exports, core_1, platform_browser_1, static_1, angular2_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var KyloServicesModule = /** @class */ (function () {
        function KyloServicesModule() {
        }
        KyloServicesModule = __decorate([
            core_1.NgModule({
                imports: [
                    platform_browser_1.BrowserModule,
                    static_1.UpgradeModule
                ],
                providers: [
                    angular2_1.notificationServiceProvider
                ]
            })
        ], KyloServicesModule);
        return KyloServicesModule;
    }());
    exports.KyloServicesModule = KyloServicesModule;
});
//# sourceMappingURL=services.module.js.map
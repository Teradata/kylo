var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "@angular/platform-browser", "@angular/upgrade/static", "routes"], function (require, exports, core_1, platform_browser_1, static_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var KyloModule = (function () {
        function KyloModule(upgrade) {
            this.upgrade = upgrade;
        }
        KyloModule.prototype.ngDoBootstrap = function () {
            this.upgrade.bootstrap(document.documentElement, ["kylo"]);
        };
        return KyloModule;
    }());
    KyloModule = __decorate([
        core_1.NgModule({
            imports: [
                platform_browser_1.BrowserModule,
                static_1.UpgradeModule
            ]
        }),
        __metadata("design:paramtypes", [static_1.UpgradeModule])
    ], KyloModule);
    exports.KyloModule = KyloModule;
});
//# sourceMappingURL=app.module.js.map
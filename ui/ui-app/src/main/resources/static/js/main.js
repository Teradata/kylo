define(["require", "exports", "@angular/platform-browser-dynamic", "@angular/upgrade/static", "@uirouter/angular-hybrid", "@uirouter/core", "./app.module"], function (require, exports, platform_browser_dynamic_1, static_1, angular_hybrid_1, core_1, app_module_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    // Fix @uirouter/core unable to load
    core_1.servicesPlugin(null);
    // Manually bootstrap the Angular app
    platform_browser_dynamic_1.platformBrowserDynamic().bootstrapModule(app_module_1.KyloModule).then(function (platformRef) {
        var injector = platformRef.injector;
        var upgrade = injector.get(static_1.UpgradeModule);
        // The DOM must be already be available
        upgrade.bootstrap(document.body, ["kylo"]);
        // Initialize the Angular Module (get() any UIRouter service from DI to initialize it)
        var url = angular_hybrid_1.getUIRouter(injector).urlService;
        // Instruct UIRouter to listen to URL changes
        url.listen();
        url.sync();
    });
});
//# sourceMappingURL=main.js.map
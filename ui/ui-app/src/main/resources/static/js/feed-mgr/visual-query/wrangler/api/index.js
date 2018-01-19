define(["require", "exports", "@angular/core", "./column", "./rest-model", "./services/dialog.service"], function (require, exports, core_1, column_1, rest_model_1, dialog_service_1) {
    "use strict";
    function __export(m) {
        for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
    }
    Object.defineProperty(exports, "__esModule", { value: true });
    __export(column_1);
    __export(rest_model_1);
    // Services
    __export(dialog_service_1);
    /**
     * Injection token for the Dialog Service.
     */
    exports.DIALOG_SERVICE = new core_1.InjectionToken("WranglerDialogService");
    /**
     * Injection token for the Wrangler injector.
     */
    exports.INJECTOR = new core_1.InjectionToken("$$wranglerInjector");
});
//# sourceMappingURL=index.js.map
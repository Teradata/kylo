define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ConfigurationService = /** @class */ (function () {
        function ConfigurationService() {
            this.MODULE_URLS = "/proxy/v1/configuration/module-urls";
        }
        return ConfigurationService;
    }());
    exports.default = ConfigurationService;
    angular.module(module_name_1.moduleName).service('ConfigurationService', ConfigurationService);
});
//# sourceMappingURL=ConfigurationService.js.map
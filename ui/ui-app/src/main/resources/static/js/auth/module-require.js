define(["require", "exports", "angular", "./module-name", "../services/services.module", "../common/common.module", "./services/UserService", "./shared/permissions-table/permissions-table"], function (require, exports, angular, module_name_1, services_module_1, common_module_1, UserService_1, permissions_table_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.KyloServicesModule = services_module_1.KyloServicesModule;
    exports.KyloCommonModule = common_module_1.KyloCommonModule;
    exports.UserService = UserService_1.UserService;
    exports.PermissionsTableController = permissions_table_1.PermissionsTableController;
    exports.moduleRequire = angular.module(module_name_1.moduleName);
});
//# sourceMappingURL=module-require.js.map
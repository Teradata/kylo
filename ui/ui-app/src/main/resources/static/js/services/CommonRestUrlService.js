define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CommonRestUrlService = /** @class */ (function () {
        function CommonRestUrlService() {
            this.ROOT = "";
            this.SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
            this.SEARCH_URL = this.ROOT + "/proxy/v1/feedmgr/search";
            this.SECURITY_GROUPS_URL = this.SECURITY_BASE_URL + "/groups";
            this.SECURITY_USERS_URL = this.SECURITY_BASE_URL + "/users";
            /**
             * get all roles
             * @type {string}
             */
            this.SECURITY_ROLES_URL = this.SECURITY_BASE_URL + "/roles";
            /**
             * get possible roles for a given Entity type (i.e. Feed, Category, Template)
             * @param entityType
             * @returns {string}
             * @constructor
             */
            this.SECURITY_ENTITY_ROLES_URL = function (entityType) {
                return this.SECURITY_BASE_URL + "/roles/" + entityType;
            };
            this.ENTITY_ACCESS_CONTROLLED_CHECK = this.SECURITY_BASE_URL + "/actions/entity-access-controlled";
            this.ANGULAR_EXTENSION_MODULES_URL = "/api/v1/ui/extension-modules";
        }
        return CommonRestUrlService;
    }());
    exports.default = CommonRestUrlService;
    angular.module(module_name_1.moduleName).service('CommonRestUrlService', CommonRestUrlService);
});
//# sourceMappingURL=CommonRestUrlService.js.map
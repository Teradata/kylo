define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var DBCPTableSchemaService = /** @class */ (function () {
        function DBCPTableSchemaService(RestUrlService) {
            this.ROOT = RestUrlService.ROOT;
            this.LIST_TABLES_URL = function (serviceId) {
                return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
            };
            this.DESCRIBE_TABLE_URL = function (serviceId, tableName) {
                return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
            };
        }
        return DBCPTableSchemaService;
    }());
    exports.DBCPTableSchemaService = DBCPTableSchemaService;
    angular.module(moduleName).service('DBCPTableSchemaService', ["RestUrlService", DBCPTableSchemaService]);
});
//# sourceMappingURL=DBCPTableSchemaService.js.map
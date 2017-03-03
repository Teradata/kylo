define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('DBCPTableSchemaService', ["RestUrlService", function (RestUrlService) {

        this.ROOT = RestUrlService.ROOT;

        this.LIST_TABLES_URL = function (serviceId) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
        };

        this.DESCRIBE_TABLE_URL = function (serviceId, tableName) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
        };
    }]);
});
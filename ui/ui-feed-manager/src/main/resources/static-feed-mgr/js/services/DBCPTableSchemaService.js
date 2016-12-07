angular.module(MODULE_FEED_MGR).service('DBCPTableSchemaService', function(RestUrlService) {

    this.ROOT = RestUrlService.ROOT;

    this.LIST_TABLES_URL = function(serviceId) {
        return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
    };

    this.DESCRIBE_TABLE_URL = function(serviceId, tableName) {
        return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
    };
});

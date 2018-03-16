
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


export class DBCPTableSchemaService {
   
    ROOT:any;
    LIST_TABLES_URL:any;
    DESCRIBE_TABLE_URL:any;
   
    constructor (RestUrlService:any) {

        this.ROOT = RestUrlService.ROOT;

        this.LIST_TABLES_URL = function (serviceId:any) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
        };

        this.DESCRIBE_TABLE_URL = function (serviceId:any, tableName:any) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
        };
    }
}
angular.module(moduleName).service('DBCPTableSchemaService', ["RestUrlService", DBCPTableSchemaService]);
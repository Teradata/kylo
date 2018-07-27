
import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";;


export class DBCPTableSchemaService {
   
    ROOT:any;
    LIST_TABLES_URL:any;
    DESCRIBE_TABLE_URL:any;
   
    static readonly $inject = ["RestUrlService"];

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
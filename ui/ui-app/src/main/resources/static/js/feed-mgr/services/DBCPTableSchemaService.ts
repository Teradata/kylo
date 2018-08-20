import { Injectable } from '@angular/core';
import { RestUrlConstants } from './RestUrlConstants';


@Injectable()
export class DBCPTableSchemaService {
   
    ROOT:any;
    LIST_TABLES_URL:any;
    DESCRIBE_TABLE_URL:any;
   
    constructor () {

        this.ROOT = RestUrlConstants.ROOT;

        this.LIST_TABLES_URL = function (serviceId:any) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
        };

        this.DESCRIBE_TABLE_URL = function (serviceId:any, tableName:any) {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
        };
    }
}

/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
angular.module(MODULE_FEED_MGR).service('DBCPTableSchemaService', function(RestUrlService) {

    this.ROOT = RestUrlService.ROOT;

    this.LIST_TABLES_URL = function(serviceId) {
        return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables";
    };

    this.DESCRIBE_TABLE_URL = function(serviceId, tableName) {
        return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + serviceId + "/tables/" + tableName;
    };
});

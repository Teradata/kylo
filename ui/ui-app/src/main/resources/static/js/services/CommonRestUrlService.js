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

define(['angular','services/module-name'], function (angular,moduleName) {
    return angular.module(moduleName).service('CommonRestUrlService', function () {

        var self = this;

        this.ROOT = "";
        this.SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";

        this.SEARCH_URL = this.ROOT + "/proxy/v1/feedmgr/search";


        this.SECURITY_GROUPS_URL = self.SECURITY_BASE_URL + "/groups";

        this.SECURITY_USERS_URL = self.SECURITY_BASE_URL + "/users";

        /**
         * get all roles
         * @type {string}
         */
        this.SECURITY_ROLES_URL = self.SECURITY_BASE_URL + "/roles";

        /**
         * get possible roles for a given Entity type (i.e. Feed, Category, Template)
         * @param entityType
         * @returns {string}
         * @constructor
         */
        this.SECURITY_ENTITY_ROLES_URL = function(entityType){
            return self.SECURITY_BASE_URL + "/roles/"+entityType;
        }

        this.ENTITY_ACCESS_CONTROLLED_CHECK = self.SECURITY_BASE_URL+"/actions/entity-access-controlled";

        this.ANGULAR_EXTENSION_MODULES_URL = "/api/v1/ui/extension-modules"



    });
});

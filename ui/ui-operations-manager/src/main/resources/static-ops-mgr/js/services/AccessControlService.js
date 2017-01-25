/*-
 * #%L
 * thinkbig-ui-operations-manager
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
/**
 * Service for interacting with the Access Control REST API.
 */

/**
 * A permission available to users and groups.
 *
 * @typedef {Object} Action
 * @property {Array.<Action>} [actions] child actions
 * @property {string} description a human-readable summary
 * @property {string} systemName unique identifier
 * @property {string} title a human-readable name
 */


/**
 * A collection of permissions available to users and groups.
 *
 * @typedef {Object} ActionSet
 * @property {Array.<Action> actions the set of permissions
 * @property {string} name the module name
 */

angular.module(MODULE_OPERATIONS).factory("AccessControlService", function($http, $q, RestUrlService) {

    var DEFAULT_MODULE = "services";

    /**
     * Interacts with the Access Control REST API.
     *
     * @constructor
     */
    function AccessControlService() {
    }

    angular.extend(AccessControlService.prototype, {

        /**
         * Allows administration of operations, such as stopping and abandoning them.
         * @type {string}
         */
        OPERATIONS_ADMIN: "adminOperations",

        /**
         * Allows access to operational information like active feeds and execution history, etc.
         * @type {string}
         */
        OPERATIONS_MANAGER_ACCESS: "accessOperations",

        /**
         * List of available actions
         *
         * @private
         * @type {Promise|null}
         */
        AVAILABLE_ACTIONS_: null,

        /**
         * Gets the list of allowed actions for the current user.
         *
         * @param {string|null} [opt_module] name of the access module, or {@code null}
         * @returns {Promise} containing an {@link ActionSet} with the allowed actions
         */
        getAllowedActions: function(opt_module) {
            var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : DEFAULT_MODULE;
            return $http.get(RestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed")
                    .then(function(response) {
                        if (angular.isUndefined(response.data.actions)) {
                            response.data.actions = [];
                        }
                        return response.data;
                    });
        },

        /**
         * Determines if the specified action is allowed.
         *
         * @param {string} name the name of the action
         * @param {Array.<Action>} actions the list of allowed actions
         * @returns {boolean} {@code true} if the action is allowed, or {@code false} if denied
         */
        hasAction: function(name, actions) {
            var self = this;
            return _.some(actions, function(action) {
                if (action.systemName === name) {
                    return true;
                } else if (angular.isArray(action.actions)) {
                    return self.hasAction(name, action.actions);
                }
                return false;
            });
        }
    });

    return new AccessControlService();
});

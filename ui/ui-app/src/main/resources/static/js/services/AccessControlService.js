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

/**
 * A collection of permission changes for a set of users or groups.
 *
 * @typedef {Object} PermissionsChange
 * @property {ActionSet} actionSet the set of permissions that should be added or removed
 * @property {string} change indicates how to process the change; one of: ADD, REMOVE, or REPLACE
 * @property {Array.<string>} groups the groups that should have their permissions changed
 * @property {Array.<string>} users the users that should have their permissions changed
 */
define(['angular', 'services/module-name', 'constants/AccessConstants'], function (angular, moduleName,AccessConstants) {




    return angular.module(moduleName).factory("AccessControlService", ["$http", "$q", "CommonRestUrlService", "UserGroupService", function ($http, $q, CommonRestUrlService, UserGroupService) {

        var DEFAULT_MODULE = "services";

        var currentUser = null;

        var ROLE_CACHE = [];

        /**
         * Interacts with the Access Control REST API.
         *
         * @constructor
         */
        function AccessControlService() {
        }

        var svc = angular.extend(AccessControlService.prototype, AccessConstants);

        return angular.extend(svc, {

            /**
             * List of available actions
             *
             * @private
             * @type {Promise|null}
             */
            AVAILABLE_ACTIONS_: null,

            executingAllowedActions: {},

            cachedUserAllowedActions: {},

            initialized: false,

            ACCESS_MODULES:{
                SERVICES:"services"
            },

            /**
             * Initialize the service
             */
            init:function(){
                var self = this;

                //build the user access and role/permission cache
                var requests = {userActions:this.getUserAllowedActions(DEFAULT_MODULE,true),roles:this.getRoles(), currentUser:this.getCurrentUser()};

                $q.all(requests).then(function(response){
                    self.initialized = true;
                    currentUser= response.currentUser;
                });

            },
            hasEntityAccess:function(requiredPermissions,entity){

                //all entities should have the object .accessControl with the correct {owner:systemName,roles:[{name:'role1'},{name:'role2'}]}
                //ASSUMES initialize will build up ROLE_CACHE

                var permissions = [];

                if(entity.accessControl == undefined){
                    return false;
                }
                //short circuit if the owner matches
                if(entity.accessControl.owner == currentUser.systemName){
                    return true;
                }
                //check the permissions on the entity roles
               _.each(entity.accessControl.roles,function(role){
                    var rolePermissions = ROLE_CACHE[role] != undefined ? ROLE_CACHE[role].permissions : [];
                    permissions.concat(rolePermissions);
                });

               return this.hasAnyAction(requiredPermissions,permissions);
            },

            /**
             * Determines if a user has access to a give ui-router state transition
             * This is accessed via the 'routes.js' ui-router listener
             * @param transition a ui-router state transition.
             * @returns {boolean} true if has access, false if access deined
             */
            hasAccess: function (transition) {
                var self = this;
                var valid = false;
                if (transition) {
                    var toState = transition.to();
                    var toStateName = toState.name;
                    var data = toState.data;
                    if(data == undefined){
                        //if there is nothing there, treat it as valid
                        return true;
                    }
                    var requiredPermissions = data.permissions || null;
                    //if its a future lazy loaded state, allow it
                    if (toStateName.endsWith(".**")) {
                        valid = true;
                    }else {
                        //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object

                        if (self.initialized) {
                            var allowedActions = self.cachedUserAllowedActions[DEFAULT_MODULE];
                            if(angular.isArray(requiredPermissions)){
                                //find the first match
                                 valid = self.hasAnyAction(requiredPermissions,allowedActions)
                            }
                            else {
                                valid = self.hasAction(requiredPermissions,allowedActions);
                            }
                        }
                    }
                }
                return valid;

            },
            /**
             * Gets the current user from the server
             * @returns {*}
             */
            getCurrentUser: function () {
                return UserGroupService.getCurrentUser();
            },
            /**
             * Gets the list of allowed actions for the specified users or groups. If no users or groups are specified, then gets the allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {string|Array.<string>|null} [opt_users] user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} [opt_groups] group name or list of group names or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getAllowedActions: function (opt_module, opt_users, opt_groups) {

                // Prepare query parameters
                var params = {};
                if (angular.isArray(opt_users) || angular.isString(opt_users)) {
                    params.user = opt_users;
                }
                if (angular.isArray(opt_groups) || angular.isString(opt_groups)) {
                    params.group = opt_groups;
                }

                // Send request
                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : DEFAULT_MODULE;
                return $http({
                    method: "GET",
                    params: params,
                    url: CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed"
                }).then(function (response) {
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            },

            /**
             * Gets the list of allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {boolean|null} true to save the data in a cache, false or underfined to not.  default is false
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getUserAllowedActions: function (opt_module, cache) {
                var self = this;
                var defer = null;

                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : DEFAULT_MODULE;
                var isExecuting = self.executingAllowedActions[safeModule] != undefined;
                if(cache == true && !isExecuting && self.cachedUserAllowedActions[safeModule] != undefined) {
                    defer = $q.defer();
                    defer.resolve(self.cachedUserAllowedActions[safeModule]);
                }
               else if (!isExecuting) {
                    defer = $q.defer();
                    self.executingAllowedActions[safeModule] = defer;

                    var promise = $http.get(CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed")
                        .then(function (response) {
                            if (angular.isUndefined(response.data.actions)) {
                                response.data.actions = [];
                            }
                            defer.resolve(response.data);
                            //add it to the cache
                            self.cachedUserAllowedActions[safeModule] = response.data;
                            //remove the executing request
                            delete self.executingAllowedActions[safeModule];
                            return response.data;
                        });
                }
                else {
                    defer = self.executingAllowedActions[safeModule];
                }
                return defer.promise;
            },

            /**
             * Gets all available actions.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getAvailableActions: function (opt_module) {
                // Send request
                if (this.AVAILABLE_ACTIONS_ === null) {
                    var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : DEFAULT_MODULE;
                    this.AVAILABLE_ACTIONS_ = $http.get(CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/available")
                        .then(function (response) {
                            return response.data;
                        });
                }
                return this.AVAILABLE_ACTIONS_;
            },

            /**
             * Determines if any name in array of names is included in the allowed actions it will return true, otherwise false
             *
             * @param names an array of names
             * @param actions An array of allowed actions
             * @returns {boolean}
             */
            hasAnyAction: function (names, actions) {
                var self = this;
                var valid = _.some(names, function (name) {
                    return self.hasAction(name.trim(), actions);
                });
                return valid;
            },

            /**
             * returns a promise with a value of true/false if the user has any of the required permissions
             * @param requiredPermissions array of required permission strings
             */
            doesUserHavePermission: function (requiredPermissions) {
                var self = this;
                var d = $q.defer();

                self.getUserAllowedActions()
                    .then(function (actionSet) {
                        var allowed = self.hasAnyAction(requiredPermissions, actionSet.actions);
                        d.resolve(allowed);
                    });
                return d.promise;
            },

            /**
             * Determines if the specified action is allowed.
             *
             * @param {string} name the name of the action
             * @param {Array.<Action>} actions the list of allowed actions
             * @returns {boolean} {@code true} if the action is allowed, or {@code false} if denied
             */
            hasAction: function (name, actions) {
                var self = this;
                return _.some(actions, function (action) {
                    if (action.systemName === name) {
                        return true;
                    }  else if (angular.isArray(action)) {
                        return self.hasAction(name, action);
                    }else if (angular.isArray(action.actions)) {
                        return self.hasAction(name, action.actions);
                    }
                    return false;
                });
            },

            /**
             * Sets the allowed actions for the specified users and groups.
             *
             * @param {string|null} module name of the access module, or {@code null}
             * @param {string|Array.<string>|null} users user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} groups group name or list of group names or {@code null}
             * @param {Array.<Action>} actions list of actions to allow
             * @returns {Promise} containing an {@link ActionSet} with the saved actions
             */
            setAllowedActions: function (module, users, groups, actions) {
                // Build the request body
                var safeModule = angular.isString(module) ? module : DEFAULT_MODULE;
                var data = {actionSet: {name: safeModule, actions: actions}, change: "REPLACE"};

                if (angular.isArray(users)) {
                    data.users = users;
                } else if (angular.isString(users)) {
                    data.users = [users];
                }

                if (angular.isArray(groups)) {
                    data.groups = groups;
                } else if (angular.isString(groups)) {
                    data.groups = [groups];
                }

                // Send the request
                return $http({
                    data: angular.toJson(data),
                    method: "POST",
                    url: CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + encodeURIComponent(safeModule) + "/allowed"
                }).then(function (response) {
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            },
            /**
             * Gets the roles from the server with their assigned permissions
             * TODO should it populate a map of {entityType: rolesArr} ??
             */
            getRoles:function(){
                var df = $q.defer();
                var rolesArr = [];
                if(ROLE_CACHE.length >0) {
                    df.resolve(angular.copy(ROLE_CACHE));
                }
                else {
                    //TODO CALL OUT TO SERVER via REST endpoint to get all roles and permissions
                    rolesArr.push({systemName: 'readOnly', name: 'Read Only', permissions: ["perm1", "perm2"]});
                    rolesArr.push({systemName: 'editor', name: 'Editor', permissions: ["perm1", "perm2"]});

                    ROLE_CACHE = rolesArr;
                    df.resolve(rolesArr);
                }
                return df.promise;
            }
        });

        return new AccessControlService();
    }]);
});

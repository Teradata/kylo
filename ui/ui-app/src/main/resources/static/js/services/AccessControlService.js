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
define(['angular', 'services/module-name', 'constants/AccessConstants', 'kylo-services-module'], function (angular, moduleName,AccessConstants) {




    return angular.module(moduleName).factory("AccessControlService", ["$http", "$q","$timeout", "CommonRestUrlService", "UserGroupService", function ($http, $q,$timeout, CommonRestUrlService, UserGroupService) {

        var DEFAULT_MODULE = "services";

        var currentUser = null;


        /**
         * Time allowed before the getAllowedActions refreshes from the server
         * Default to refresh the cache every 3 minutes
         */
        var cacheUserAllowedActionsTime = 3000*60;

        var lastUserAllowedCacheAccess = {};

        var userAllowedActionsNeedsRefresh = function(module){
            if(angular.isUndefined(lastUserAllowedCacheAccess[module])) {
                return true;
            }
            else {
                var diff = new Date().getTime() - lastUserAllowedCacheAccess[module];
                if(diff > cacheUserAllowedActionsTime){
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        /**
         * Key: Entity Type, Value: [{systemName:'ROLE1',permissions:['perm1','perm2']},...]
         * @type {{}}
         */
        var ROLE_CACHE = {};

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

                //build the user access and role/permission cache//  roles:self.getRoles()
                var requests = {userActions:self.getUserAllowedActions(DEFAULT_MODULE,true),
                    roles:self.getRoles(),
                    currentUser:self.getCurrentUser(),
                    entityAccessControlled:self.checkEntityAccessControlled()};
                var defer = $q.defer();
                $q.all(requests).then(function(response){
                    self.initialized = true;
                    currentUser= response.currentUser;
                        defer.resolve(true);
                });
                return defer.promise;

            },
            hasEntityAccess:function(requiredPermissions,entity,entityType){
                var self = this;

                //all entities should have the object .allowedActions and .owner
                if(entity == undefined){
                    return false;
                }

                //short circuit if the owner matches
                if(entity.owner && entity.owner.systemName == currentUser.systemName){
                    return true;
                }

                if(!angular.isArray(requiredPermissions)){
                    requiredPermissions = [requiredPermissions];
                }

                return self.hasAnyAction(requiredPermissions, entity.allowedActions);
            },
            isFutureState:function(state){
                return state.endsWith(".**");
            },
            /**
             * Check to see if we are using entity access control or not
             * @returns {*}
             */
            isEntityAccessControlled:function(){
                var self = this;
                return this.entityAccessControlled;
            },

            checkEntityAccessControlled:function(){
                var self = this;
                if(angular.isDefined(this.entityAccessControlled)){
                    return self.entityAccessControlled;
                }
                else {
                    return $http.get(CommonRestUrlService.ENTITY_ACCESS_CONTROLLED_CHECK).then(function(response){
                        self.entityAccessControlled = response.data;
                    });
                }
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
                    if (self.isFutureState(toStateName)) {
                        valid = true;
                    }else {
                        //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object

                        if (self.initialized) {
                            var allowedActions = self.cachedUserAllowedActions[DEFAULT_MODULE];
                            if(angular.isArray(requiredPermissions)){

                                //find the first match
                                 valid = requiredPermissions.length ==0 || self.hasAnyAction(requiredPermissions,allowedActions)
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
                if(angular.isUndefined(cache)){
                    cache = true;
                }
                var isExecuting = self.executingAllowedActions[safeModule] != undefined;
                if(cache == true && !isExecuting && self.cachedUserAllowedActions[safeModule] != undefined && !userAllowedActionsNeedsRefresh(safeModule)) {
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
                            lastUserAllowedCacheAccess[safeModule] = new Date().getTime();
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
                if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                    return true;
                }
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
                if (requiredPermissions == null || requiredPermissions == undefined || (angular.isArray(requiredPermissions) && requiredPermissions.length == 0)) {
                    d.resolve(true);
                }
                else {

                self.getUserAllowedActions()
                    .then(function (actionSet) {
                        var allowed = self.hasAnyAction(requiredPermissions, actionSet.actions);
                        d.resolve(allowed);
                    });
                }
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
                if(name == null){
                    return true;
                }
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
             * Gets all roles abd populates the ROLE_CACHE
             * @returns {*|Request}
             */
            getRoles:function(){
                  return  $http.get(CommonRestUrlService.SECURITY_ROLES_URL).then(function (response) {
                        _.each(response.data,function(roles,entityType){
                            ROLE_CACHE[entityType] = roles;
                        });
                    });
            },
            /**
             * For a given entity type (i.e. FEED) return the roles/permissions
             * @param entityType the type of entity
             */
            getEntityRoles:function(entityType){
                var df = $q.defer();
                var useCache = false; // disable the cache for now

                if(useCache && ROLE_CACHE[entityType] != undefined) {
                    df.resolve(angular.copy(ROLE_CACHE[entityType]));
                }
                else {
                    var rolesArr = [];
                    $http.get(CommonRestUrlService.SECURITY_ENTITY_ROLES_URL(entityType)).then(function (response) {
                        _.each(response.data,function(role){
                            role.name = role.title;
                            rolesArr.push(role);
                        });
                        ROLE_CACHE[entityType] = rolesArr;
                        df.resolve(rolesArr);
                    });

                }
                return df.promise;
            },
            /**
             * Check if the user has access checking both the functional page/section access as well as entity access permission.
             * If Entity access is not enabled for the app it will bypass the entity access check.
             * This will return a promise with a boolean as the response/resolved value.
             * Callers need to wrap this in $q.when.
             *
             * Example:
             *
             * $q.when(AccessControlService.hasPermission(...)).then(function(hasAccess){
             *  if(hasAccess){
             *
             *  }
             *   ...
             * }
             *
             * @param functionalPermission a permission string to check
             * @param entity the entity to check
             * @param entityPermissions  a string or an array of entity permissions to check against the user and supplied entity
             * @return a promise with a boolean value as the response
             */
            hasPermission: function(functionalPermission,entity,entityPermissions) {
                var self = this;
                var entityAccessControlled = entity != null && entityPermissions != null && this.isEntityAccessControlled();
                var defer = $q.defer();
                var requests = {
                    entityAccess: entityAccessControlled == true ? this.hasEntityAccess(entityPermissions, entity) : true,
                    functionalAccess: this.getUserAllowedActions()
                }
                $q.all(requests).then(function (response) {
                    defer.resolve(response.entityAccess && self.hasAction(functionalPermission, response.functionalAccess.actions));
                });
                return defer.promise;
            }

        });

        return new AccessControlService();
    }]);
});

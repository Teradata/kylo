var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "angular", "./module-name", "../constants/AccessConstants", "underscore", "./CommonRestUrlService", "./UserGroupService", "kylo-services-module"], function (require, exports, angular, module_name_1, AccessConstants_1, _, CommonRestUrlService_1, UserGroupService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessControlService = /** @class */ (function (_super) {
        __extends(AccessControlService, _super);
        //AccessControlService(){}
        function AccessControlService($http, $q, $timeout, CommonRestUrlService, UserGroupService) {
            var _this = 
            /**
             * Time allowed before the getAllowedActions refreshes from the server
             * Default to refresh the cache every 3 minutes
             */
            _super.call(this) || this;
            _this.$http = $http;
            _this.$q = $q;
            _this.$timeout = $timeout;
            _this.CommonRestUrlService = CommonRestUrlService;
            _this.UserGroupService = UserGroupService;
            /**
            * Interacts with the Access Control REST API.
            * @constructor
            */
            _this.DEFAULT_MODULE = "services";
            _this.currentUser = null;
            _this.cacheUserAllowedActionsTime = 3000 * 60;
            _this.lastUserAllowedCacheAccess = {};
            _this.ROLE_CACHE = {};
            // svc: any= angular.extend(AccessControlService.prototype, AccessConstants.default); // 
            // return angular.extend(svc, {
            /**
             * List of available actions
             * @private
             * @type {Promise|null}
             */
            _this.AVAILABLE_ACTIONS_ = null;
            _this.executingAllowedActions = {};
            _this.cachedUserAllowedActions = {};
            _this.initialized = false;
            _this.ACCESS_MODULES = {
                SERVICES: "services"
            };
            /**
             * Initialize the service
             */
            _this.init = function () {
                //build the user access and role/permission cache//  roles:this.getRoles()
                var requests = { userActions: _this.getUserAllowedActions(_this.DEFAULT_MODULE, true),
                    roles: _this.getRoles(),
                    currentUser: _this.getCurrentUser(),
                    entityAccessControlled: _this.checkEntityAccessControlled() };
                var defer = _this.$q.defer();
                _this.$q.all(requests).then(function (response) {
                    _this.initialized = true;
                    _this.currentUser = response.currentUser;
                    defer.resolve(true);
                });
                return defer.promise;
            };
            _this.hasEntityAccess = function (requiredPermissions, entity, entityType) {
                //all entities should have the object .allowedActions and .owner
                if (entity == undefined) {
                    return false;
                }
                //short circuit if the owner matches
                if (entity.owner && entity.owner.systemName == _this.currentUser.systemName) {
                    return true;
                }
                if (!angular.isArray(requiredPermissions)) {
                    requiredPermissions = [requiredPermissions];
                }
                return _this.hasAnyAction(requiredPermissions, entity.allowedActions);
            };
            _this.isFutureState = function (state) {
                return state.endsWith(".**");
            };
            /**
             * Check to see if we are using entity access control or not
             * @returns {*}
             */
            _this.isEntityAccessControlled = function () {
                return _this.entityAccessControlled;
            };
            _this.checkEntityAccessControlled = function () {
                if (angular.isDefined(_this.entityAccessControlled)) {
                    return _this.entityAccessControlled;
                }
                else {
                    return _this.$http.get(_this.CommonRestUrlService.ENTITY_ACCESS_CONTROLLED_CHECK).then(function (response) {
                        _this.entityAccessControlled = response.data;
                    });
                }
            };
            /**
             * Determines if a user has access to a give ui-router state transition
             * This is accessed via the 'routes.js' ui-router listener
             * @param transition a ui-router state transition.
             * @returns {boolean} true if has access, false if access deined
             */
            _this.hasAccess = function (transition) {
                var valid = false;
                if (transition) {
                    var toState = transition.to();
                    var toStateName = toState.name;
                    var data = toState.data;
                    if (data == undefined) {
                        //if there is nothing there, treat it as valid
                        return true;
                    }
                    var requiredPermissions = data.permissions || null;
                    //if its a future lazy loaded state, allow it
                    if (_this.isFutureState(toStateName)) {
                        valid = true;
                    }
                    else {
                        //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object
                        if (_this.initialized) {
                            var allowedActions = _this.cachedUserAllowedActions[_this.DEFAULT_MODULE];
                            if (angular.isArray(requiredPermissions)) {
                                //find the first match
                                valid = requiredPermissions.length == 0 || _this.hasAnyAction(requiredPermissions, allowedActions);
                            }
                            else {
                                valid = _this.hasAction(requiredPermissions, allowedActions);
                            }
                        }
                    }
                }
                return valid;
            };
            /**
             * Gets the current user from the server
             * @returns {*}
             */
            _this.getCurrentUser = function () {
                return _this.UserGroupService.getCurrentUser();
            };
            /**
             * Gets the list of allowed actions for the specified users or groups. If no users or groups are specified, then gets the allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {string|Array.<string>|null} [opt_users] user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} [opt_groups] group name or list of group names or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            _this.getAllowedActions = function (opt_module, opt_users, opt_groups) {
                // Prepare query parameters
                var params = {};
                if (angular.isArray(opt_users) || angular.isString(opt_users)) {
                    params.user = opt_users;
                }
                if (angular.isArray(opt_groups) || angular.isString(opt_groups)) {
                    params.group = opt_groups;
                }
                // Send request
                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : _this.DEFAULT_MODULE;
                return _this.$http({
                    method: "GET",
                    params: params,
                    url: _this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed"
                }).then(function (response) {
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            };
            /**
             * Gets the list of allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {boolean|null} true to save the data in a cache, false or underfined to not.  default is false
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            _this.getUserAllowedActions = function (opt_module, cache) {
                var defer = null;
                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : _this.DEFAULT_MODULE;
                if (angular.isUndefined(cache)) {
                    cache = true;
                }
                var isExecuting = _this.executingAllowedActions[safeModule] != undefined;
                if (cache == true && !isExecuting && _this.cachedUserAllowedActions[safeModule] != undefined && !_this.userAllowedActionsNeedsRefresh(safeModule)) {
                    defer = _this.$q.defer();
                    defer.resolve(_this.cachedUserAllowedActions[safeModule]);
                }
                else if (!isExecuting) {
                    defer = _this.$q.defer();
                    _this.executingAllowedActions[safeModule] = defer;
                    var promise = _this.$http.get(_this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed")
                        .then(function (response) {
                        if (angular.isUndefined(response.data.actions)) {
                            response.data.actions = [];
                        }
                        defer.resolve(response.data);
                        //add it to the cache
                        _this.lastUserAllowedCacheAccess[safeModule] = new Date().getTime();
                        _this.cachedUserAllowedActions[safeModule] = response.data;
                        //remove the executing request
                        delete _this.executingAllowedActions[safeModule];
                        return response.data;
                    });
                }
                else {
                    defer = _this.executingAllowedActions[safeModule];
                }
                return defer.promise;
            };
            /**
             * Gets all available actions.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            _this.getAvailableActions = function (opt_module) {
                // Send request
                if (_this.AVAILABLE_ACTIONS_ === null) {
                    var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : _this.DEFAULT_MODULE;
                    _this.AVAILABLE_ACTIONS_ = _this.$http.get(_this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/available")
                        .then(function (response) {
                        return response.data;
                    });
                }
                return _this.AVAILABLE_ACTIONS_;
            };
            /**
             * Determines if any name in array of names is included in the allowed actions it will return true, otherwise false
             *
             * @param names an array of names
             * @param actions An array of allowed actions
             * @returns {boolean}
             */
            _this.hasAnyAction = function (names, actions) {
                if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                    return true;
                }
                var valid = _.some(names, function (name) {
                    return _this.hasAction(name.trim(), actions);
                });
                return valid;
            };
            /**
             * returns a promise with a value of true/false if the user has any of the required permissions
             * @param requiredPermissions array of required permission strings
             */
            _this.doesUserHavePermission = function (requiredPermissions) {
                var d = _this.$q.defer();
                if (requiredPermissions == null || requiredPermissions == undefined || (angular.isArray(requiredPermissions) && requiredPermissions.length == 0)) {
                    d.resolve(true);
                }
                else {
                    _this.getUserAllowedActions()
                        .then(function (actionSet) {
                        var allowed = _this.hasAnyAction(requiredPermissions, actionSet.actions);
                        d.resolve(allowed);
                    });
                }
                return d.promise;
            };
            /**
             * Determines if the specified action is allowed.
             *
             * @param {string} name the name of the action
             * @param {Array.<Action>} actions the list of allowed actions
             * @returns {boolean} {@code true} if the action is allowed, or {@code false} if denied
             */
            _this.hasAction = function (name, actions) {
                if (name == null) {
                    return true;
                }
                return _.some(actions, function (action) {
                    if (action.systemName === name) {
                        return true;
                    }
                    else if (angular.isArray(action)) {
                        return _this.hasAction(name, action);
                    }
                    else if (angular.isArray(action.actions)) {
                        return _this.hasAction(name, action.actions);
                    }
                    return false;
                });
            };
            /**
             * Sets the allowed actions for the specified users and groups.
             *
             * @param {string|null} module name of the access module, or {@code null}
             * @param {string|Array.<string>|null} users user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} groups group name or list of group names or {@code null}
             * @param {Array.<Action>} actions list of actions to allow
             * @returns {Promise} containing an {@link ActionSet} with the saved actions
             */
            _this.setAllowedActions = function (module, users, groups, actions) {
                // Build the request body
                var safeModule = angular.isString(module) ? module : _this.DEFAULT_MODULE;
                var data = { actionSet: { name: safeModule, actions: actions }, change: "REPLACE" };
                if (angular.isArray(users)) {
                    data.users = users;
                }
                else if (angular.isString(users)) {
                    data.users = [users];
                }
                if (angular.isArray(groups)) {
                    data.groups = groups;
                }
                else if (angular.isString(groups)) {
                    data.groups = [groups];
                }
                // Send the request
                return _this.$http({
                    data: angular.toJson(data),
                    method: "POST",
                    url: _this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + encodeURIComponent(safeModule) + "/allowed"
                }).then(function (response) {
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            };
            /**
             * Gets all roles abd populates the ROLE_CACHE
             * @returns {*|Request}
             */
            _this.getRoles = function () {
                return _this.$http.get(_this.CommonRestUrlService.SECURITY_ROLES_URL).then(function (response) {
                    _.each(response.data, function (roles, entityType) {
                        _this.ROLE_CACHE[entityType] = roles;
                    });
                });
            };
            /**
             * For a given entity type (i.e. FEED) return the roles/permissions
             * @param entityType the type of entity
             */
            _this.getEntityRoles = function (entityType) {
                var df = _this.$q.defer();
                var useCache = false; // disable the cache for now
                if (useCache && _this.ROLE_CACHE[entityType] != undefined) {
                    df.resolve(angular.copy(_this.ROLE_CACHE[entityType]));
                }
                else {
                    var rolesArr = [];
                    _this.$http.get(_this.CommonRestUrlService.SECURITY_ENTITY_ROLES_URL(entityType)).then(function (response) {
                        _.each(response.data, function (role) {
                            role.name = role.title;
                            rolesArr.push(role);
                        });
                        _this.ROLE_CACHE[entityType] = rolesArr;
                        df.resolve(rolesArr);
                    });
                }
                return df.promise;
            };
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
            _this.hasPermission = function (functionalPermission, entity, entityPermissions) {
                var entityAccessControlled = entity != null && entityPermissions != null && _this.isEntityAccessControlled();
                var defer = _this.$q.defer();
                var requests = {
                    entityAccess: entityAccessControlled == true ? _this.hasEntityAccess(entityPermissions, entity) : true,
                    functionalAccess: _this.getUserAllowedActions()
                };
                _this.$q.all(requests).then(function (response) {
                    defer.resolve(response.entityAccess && _this.hasAction(functionalPermission, response.functionalAccess.actions));
                });
                return defer.promise;
            };
            _this.userAllowedActionsNeedsRefresh = function (module) {
                if (angular.isUndefined(_this.lastUserAllowedCacheAccess[module])) {
                    return true;
                }
                else {
                    var diff = new Date().getTime() - _this.lastUserAllowedCacheAccess[module];
                    if (diff > _this.cacheUserAllowedActionsTime) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };
            return _this;
            /**
             * Key: Entity Type, Value: [{systemName:'ROLE1',permissions:['perm1','perm2']},...]
             * @type {{}}
             */
            //return new AccessControlService(); // constructor returning two things;
        }
        return AccessControlService;
    }(AccessConstants_1.default));
    exports.default = AccessControlService;
    angular.module(module_name_1.moduleName)
        .service('CommonRestUrlService', CommonRestUrlService_1.default)
        .service("UserGroupService", ['$http', '$q', 'CommonRestUrlService', UserGroupService_1.default])
        .factory("AccessControlService", ["$http", "$q", "$timeout", "CommonRestUrlService", "UserGroupService",
        function ($http, $q, $timeout, CommonRestUrlService, UserGroupService) {
            return new AccessControlService($http, $q, $timeout, CommonRestUrlService, UserGroupService);
        }
    ]);
});
//# sourceMappingURL=AccessControlService.js.map
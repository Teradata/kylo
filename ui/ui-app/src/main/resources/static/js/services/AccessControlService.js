define(["require", "exports", "angular", "./module-name", "../constants/AccessConstants", "underscore", "./CommonRestUrlService", "./UserGroupService", "kylo-services-module"], function (require, exports, angular, module_name_1, AccessConstants_1, _, CommonRestUrlService_1, UserGroupService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessControlService = /** @class */ (function () {
        function AccessControlService($http, $q, $timeout, CommonRestUrlService, UserGroupService) {
            this.$http = $http;
            this.$q = $q;
            this.$timeout = $timeout;
            this.CommonRestUrlService = CommonRestUrlService;
            this.UserGroupService = UserGroupService;
            var DEFAULT_MODULE = "services";
            var currentUser = null;
            /**
             * Time allowed before the getAllowedActions refreshes from the server
             * Default to refresh the cache every 3 minutes
             */
            var cacheUserAllowedActionsTime = 3000 * 60;
            var lastUserAllowedCacheAccess = {};
            var userAllowedActionsNeedsRefresh = function (module) {
                if (angular.isUndefined(lastUserAllowedCacheAccess[module])) {
                    return true;
                }
                else {
                    var diff = new Date().getTime() - lastUserAllowedCacheAccess[module];
                    if (diff > cacheUserAllowedActionsTime) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };
            /**
             * Key: Entity Type, Value: [{systemName:'ROLE1',permissions:['perm1','perm2']},...]
             * @type {{}}
             */
            var ROLE_CACHE = {};
            var svc = angular.extend(AccessControlService.prototype, AccessConstants_1.default); //.default
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
                ACCESS_MODULES: {
                    SERVICES: "services"
                },
                /**
                 * Initialize the service
                 */
                init: function () {
                    var _this = this;
                    //build the user access and role/permission cache//  roles:this.getRoles()
                    var requests = { userActions: this.getUserAllowedActions(DEFAULT_MODULE, true),
                        roles: this.getRoles(),
                        currentUser: this.getCurrentUser(),
                        entityAccessControlled: this.checkEntityAccessControlled() };
                    var defer = $q.defer();
                    $q.all(requests).then(function (response) {
                        _this.initialized = true;
                        currentUser = response.currentUser;
                        defer.resolve(true);
                    });
                    return defer.promise;
                },
                hasEntityAccess: function (requiredPermissions, entity, entityType) {
                    //all entities should have the object .allowedActions and .owner
                    if (entity == undefined) {
                        return false;
                    }
                    //short circuit if the owner matches
                    if (entity.owner && entity.owner.systemName == currentUser.systemName) {
                        return true;
                    }
                    if (!angular.isArray(requiredPermissions)) {
                        requiredPermissions = [requiredPermissions];
                    }
                    return this.hasAnyAction(requiredPermissions, entity.allowedActions);
                },
                isFutureState: function (state) {
                    return state.endsWith(".**");
                },
                /**
                 * Check to see if we are using entity access control or not
                 * @returns {*}
                 */
                isEntityAccessControlled: function () {
                    return this.entityAccessControlled;
                },
                checkEntityAccessControlled: function () {
                    var _this = this;
                    if (angular.isDefined(this.entityAccessControlled)) {
                        return this.entityAccessControlled;
                    }
                    else {
                        return $http.get(CommonRestUrlService.ENTITY_ACCESS_CONTROLLED_CHECK).then(function (response) {
                            _this.entityAccessControlled = response.data;
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
                        if (this.isFutureState(toStateName)) {
                            valid = true;
                        }
                        else {
                            //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object
                            if (this.initialized) {
                                var allowedActions = this.cachedUserAllowedActions[DEFAULT_MODULE];
                                if (angular.isArray(requiredPermissions)) {
                                    //find the first match
                                    valid = requiredPermissions.length == 0 || this.hasAnyAction(requiredPermissions, allowedActions);
                                }
                                else {
                                    valid = this.hasAction(requiredPermissions, allowedActions);
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
                    var _this = this;
                    var defer = null;
                    var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : DEFAULT_MODULE;
                    if (angular.isUndefined(cache)) {
                        cache = true;
                    }
                    var isExecuting = this.executingAllowedActions[safeModule] != undefined;
                    if (cache == true && !isExecuting && this.cachedUserAllowedActions[safeModule] != undefined && !userAllowedActionsNeedsRefresh(safeModule)) {
                        defer = $q.defer();
                        defer.resolve(this.cachedUserAllowedActions[safeModule]);
                    }
                    else if (!isExecuting) {
                        defer = $q.defer();
                        this.executingAllowedActions[safeModule] = defer;
                        var promise = $http.get(CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed")
                            .then(function (response) {
                            if (angular.isUndefined(response.data.actions)) {
                                response.data.actions = [];
                            }
                            defer.resolve(response.data);
                            //add it to the cache
                            lastUserAllowedCacheAccess[safeModule] = new Date().getTime();
                            _this.cachedUserAllowedActions[safeModule] = response.data;
                            //remove the executing request
                            delete _this.executingAllowedActions[safeModule];
                            return response.data;
                        });
                    }
                    else {
                        defer = this.executingAllowedActions[safeModule];
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
                    var _this = this;
                    if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                        return true;
                    }
                    var valid = _.some(names, function (name) {
                        return _this.hasAction(name.trim(), actions);
                    });
                    return valid;
                },
                /**
                 * returns a promise with a value of true/false if the user has any of the required permissions
                 * @param requiredPermissions array of required permission strings
                 */
                doesUserHavePermission: function (requiredPermissions) {
                    var _this = this;
                    var d = $q.defer();
                    if (requiredPermissions == null || requiredPermissions == undefined || (angular.isArray(requiredPermissions) && requiredPermissions.length == 0)) {
                        d.resolve(true);
                    }
                    else {
                        this.getUserAllowedActions()
                            .then(function (actionSet) {
                            var allowed = _this.hasAnyAction(requiredPermissions, actionSet.actions);
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
                    var _this = this;
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
                getRoles: function () {
                    return $http.get(CommonRestUrlService.SECURITY_ROLES_URL).then(function (response) {
                        _.each(response.data, function (roles, entityType) {
                            ROLE_CACHE[entityType] = roles;
                        });
                    });
                },
                /**
                 * For a given entity type (i.e. FEED) return the roles/permissions
                 * @param entityType the type of entity
                 */
                getEntityRoles: function (entityType) {
                    var df = $q.defer();
                    var useCache = false; // disable the cache for now
                    if (useCache && ROLE_CACHE[entityType] != undefined) {
                        df.resolve(angular.copy(ROLE_CACHE[entityType]));
                    }
                    else {
                        var rolesArr = [];
                        $http.get(CommonRestUrlService.SECURITY_ENTITY_ROLES_URL(entityType)).then(function (response) {
                            _.each(response.data, function (role) {
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
                hasPermission: function (functionalPermission, entity, entityPermissions) {
                    var _this = this;
                    var entityAccessControlled = entity != null && entityPermissions != null && this.isEntityAccessControlled();
                    var defer = $q.defer();
                    var requests = {
                        entityAccess: entityAccessControlled == true ? this.hasEntityAccess(entityPermissions, entity) : true,
                        functionalAccess: this.getUserAllowedActions()
                    };
                    $q.all(requests).then(function (response) {
                        defer.resolve(response.entityAccess && _this.hasAction(functionalPermission, response.functionalAccess.actions));
                    });
                    return defer.promise;
                }
            });
            return new AccessControlService(); // constructor returning two things;
        }
        /**
        * Interacts with the Access Control REST API.
        * @constructor
        */
        AccessControlService.prototype.AccessControlService = function () { };
        return AccessControlService;
    }());
    exports.default = AccessControlService;
    angular.module(module_name_1.moduleName)
        .service('CommonRestUrlService', CommonRestUrlService_1.default)
        .service("UserGroupService", ['$http', '$q', 'CommonRestUrlService', UserGroupService_1.default])
        .factory("AccessControlService", ["$http", "$q", "$timeout", "CommonRestUrlService", "UserGroupService", AccessControlService]);
});
//# sourceMappingURL=AccessControlService.js.map
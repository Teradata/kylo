import * as angular from 'angular';
import {moduleName} from './module-name';
import  AccessConstants from '../constants/AccessConstants';
import 'kylo-services-module';
import * as _ from "underscore";
import CommonRestUrlService from "./CommonRestUrlService";
import {UserGroupService} from "./UserGroupService";
import {EntityAccessControlService} from "../feed-mgr/shared/entity-access-control/EntityAccessControlService";

export class AccessControlService extends AccessConstants {
 /**
 * Interacts with the Access Control REST API.
 * @constructor
 */
 DEFAULT_MODULE: any = "services";
 currentUser: any = null;
 entityAccessControlled: any;
 cacheUserAllowedActionsTime: any = 3000*60;
 lastUserAllowedCacheAccess: any = {};
 userAllowedActionsNeedsRefresh: any;
//AccessControlService(){}


static readonly $inject = ["$http","$q","$timeout","CommonRestUrlService","UserGroupService"];
 constructor(private $http: angular.IHttpService,
            private $q: angular.IQService,
            private $timeout: angular.ITimeoutService,
            private CommonRestUrlService: CommonRestUrlService,
            private UserGroupService: UserGroupService){        
        /**
         * Time allowed before the getAllowedActions refreshes from the server
         * Default to refresh the cache every 3 minutes
         */
        super();
        this.userAllowedActionsNeedsRefresh = (module: any)=>{
            if(angular.isUndefined(this.lastUserAllowedCacheAccess[module])) {
                return true;
            }
            else {
                var diff = new Date().getTime() - this.lastUserAllowedCacheAccess[module];
                if(diff > this.cacheUserAllowedActionsTime){
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
        //return new AccessControlService(); // constructor returning two things;
    }
    
        ROLE_CACHE: any = {};
            // svc: any= angular.extend(AccessControlService.prototype, AccessConstants.default); // 
            // return angular.extend(svc, {
            /**
             * List of available actions
             * @private
             * @type {Promise|null}
             */
            AVAILABLE_ACTIONS_: any= null;
            executingAllowedActions: any= {};
            cachedUserAllowedActions: any= {};
            initialized: boolean= false;
            ACCESS_MODULES:any={
                SERVICES:"services"
            };
            /**
             * Initialize the service
             */
            init=()=>{
                //build the user access and role/permission cache//  roles:this.getRoles()
                var requests: any = {userActions:this.getUserAllowedActions(this.DEFAULT_MODULE,true),
                    roles:this.getRoles(),
                    currentUser:this.getCurrentUser(),
                    entityAccessControlled:this.checkEntityAccessControlled()};
                var defer: any = this.$q.defer();
                this.$q.all(requests).then((response: any)=>{
                    this.initialized = true;
                    this.currentUser= response.currentUser;
                        defer.resolve(true);
                });
                return defer.promise;
            }
            hasEntityAccess=(requiredPermissions: any,entity: any,entityType?: any)=>{
                //all entities should have the object .allowedActions and .owner
                if(entity == undefined){
                    return false;
                }

                //short circuit if the owner matches
                if(entity.owner && entity.owner.systemName == this.currentUser.systemName){
                    return true;
                }

                if(!angular.isArray(requiredPermissions)){
                    requiredPermissions = [requiredPermissions];
                }

                return this.hasAnyAction(requiredPermissions, entity.allowedActions);
            }
            isFutureState=(state: any)=>{
                return state.endsWith(".**");
            }
            /**
             * Check to see if we are using entity access control or not
             * @returns {*}
             */
            isEntityAccessControlled=()=>{
                return this.entityAccessControlled;
            }

            checkEntityAccessControlled=()=>{
                if(angular.isDefined(this.entityAccessControlled)){
                    return this.entityAccessControlled;
                }
                else {
                    return this.$http.get(this.CommonRestUrlService.ENTITY_ACCESS_CONTROLLED_CHECK).then((response: any)=>{
                        this.entityAccessControlled = response.data;
                    });
                }
            }

            /**
             * Determines if a user has access to a give ui-router state transition
             * This is accessed via the 'routes.js' ui-router listener
             * @param transition a ui-router state transition.
             * @returns {boolean} true if has access, false if access deined
             */
            hasAccess= (transition: any)=>{
                var valid: boolean = false;
                if (transition) {
                    var toState = transition.to();
                    var toStateName = toState.name;
                    var data = toState.data;
                    if(data == undefined){
                        //if there is nothing there, treat it as valid
                        return true;
                    }
                    var requiredPermissions = data.permissions || null;
                    if(requiredPermissions == null && data.permissionsKey) {
                        requiredPermissions = AccessControlService.getStatePermissions(data.permissionsKey);
                    }
                    //if its a future lazy loaded state, allow it
                    if (this.isFutureState(toStateName)) {
                        valid = true;
                    }else {
                        //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object

                        if (this.initialized) {
                            var allowedActions = this.cachedUserAllowedActions[this.DEFAULT_MODULE];
                            if(angular.isArray(requiredPermissions)){

                                //find the first match
                                 valid = requiredPermissions.length ==0 || this.hasAllActions(requiredPermissions,allowedActions)
                            }
                            else {
                                valid = this.hasAction(requiredPermissions,allowedActions);
                            }
                        }
                    }
                }
                return valid;

            };

            /**
             * Find the missing actions required for a given transition
             * @param transition
             */
            findMissingPermissions = (requiredPermissions: string[])=>{
                let missingPermissions = [];
                var allowedActions = this.cachedUserAllowedActions[this.DEFAULT_MODULE];
                    if(requiredPermissions != null) {
                        missingPermissions  = this.findMissingActions(requiredPermissions, allowedActions)
                    }
                    return missingPermissions
             };
            /**
             * Gets the current user from the server
             * @returns {*}
             */
            getCurrentUser= ()=> {
                return this.UserGroupService.getCurrentUser();
            }
            /**
             * Gets the list of allowed actions for the specified users or groups. If no users or groups are specified, then gets the allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {string|Array.<string>|null} [opt_users] user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} [opt_groups] group name or list of group names or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getAllowedActions= (opt_module: any, opt_users: any, opt_groups: any)=> {

                // Prepare query parameters
                var params: any = {};
                if (angular.isArray(opt_users) || angular.isString(opt_users)) {
                    params.user = opt_users;
                }
                if (angular.isArray(opt_groups) || angular.isString(opt_groups)) {
                    params.group = opt_groups;
                }

                // Send request
                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
                return this.$http({
                    method: "GET",
                    params: params,
                    url: this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed"
                }).then((response: any)=>{
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            }

            /**
             * Gets the list of allowed actions for the current user.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @param {boolean|null} true to save the data in a cache, false or underfined to not.  default is false
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getUserAllowedActions(opt_module?: any, cache?: any):Promise<any> {
                var defer: any = null;

                var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
                if(angular.isUndefined(cache)){
                    cache = true;
                }
                var isExecuting = this.executingAllowedActions[safeModule] != undefined;
                if(cache == true && !isExecuting && this.cachedUserAllowedActions[safeModule] != undefined && !this.userAllowedActionsNeedsRefresh(safeModule)) {
                    defer = this.$q.defer();
                    defer.resolve(this.cachedUserAllowedActions[safeModule]);
                }
               else if (!isExecuting) {
                    defer = this.$q.defer();
                    this.executingAllowedActions[safeModule] = defer;

                    var promise = this.$http.get(this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed")
                        .then((response: any)=>{
                            if (angular.isUndefined(response.data.actions)) {
                                response.data.actions = [];
                            }
                            defer.resolve(response.data);
                            //add it to the cache
                            this.lastUserAllowedCacheAccess[safeModule] = new Date().getTime();
                            this.cachedUserAllowedActions[safeModule] = response.data;
                            //remove the executing request
                            delete this.executingAllowedActions[safeModule];
                            return response.data;
                        });
                }
                else {
                    defer = this.executingAllowedActions[safeModule];
                }
                return defer.promise;
            }
            /**
             * Gets all available actions.
             *
             * @param {string|null} [opt_module] name of the access module, or {@code null}
             * @returns {Promise} containing an {@link ActionSet} with the allowed actions
             */
            getAvailableActions= (opt_module?: any)=> {
                // Send request
                if (this.AVAILABLE_ACTIONS_ === null) {
                    var safeModule = angular.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
                    this.AVAILABLE_ACTIONS_ = this.$http.get(this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/available")
                        .then((response: any)=>{
                            return response.data;
                        });
                }
                return this.AVAILABLE_ACTIONS_;
            }
            findMissingActions= (names: any, actions: any)=>{
                if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                    return [];
                }
                var missing = _.filter(names,(name: any)=>{
                    return !this.hasAction(name.trim(), actions);
                });
                return missing;
            }
            /**
             * Determines if any name in array of names is included in the allowed actions it will return true, otherwise false
             *
             * @param names an array of names
             * @param actions An array of allowed actions
             * @returns {boolean}
             */
            hasAllActions= (names: any, actions: any)=>{
                if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                    return true;
                }
                var valid = _.every(names,(name: any)=>{
                    return this.hasAction(name.trim(), actions);
                });
                return valid;
            }
            /**
             * Determines if any name in array of names is included in the allowed actions it will return true, otherwise false
             *
             * @param names an array of names
             * @param actions An array of allowed actions
             * @returns {boolean}
             */
            hasAnyAction= (names: any, actions: any)=>{
                if (names == "" || names == null || names == undefined || (angular.isArray(names) && names.length == 0)) {
                    return true;
                }
                var valid = _.some(names,(name: any)=>{
                    return this.hasAction(name.trim(), actions);
                });
                return valid;
            }
            /**
             * returns a promise with a value of true/false if the user has any of the required permissions
             * @param requiredPermissions array of required permission strings
             */
            doesUserHavePermission= (requiredPermissions: any)=>{
                var d = this.$q.defer();
                if (requiredPermissions == null || requiredPermissions == undefined || (angular.isArray(requiredPermissions) && requiredPermissions.length == 0)) {
                    d.resolve(true);
                }
                else {

                this.getUserAllowedActions()
                    .then((actionSet: any)=> {
                        var allowed = this.hasAnyAction(requiredPermissions, actionSet.actions);
                        d.resolve(allowed);
                    });
                }
                return d.promise;
            }

            /**
             * Determines if the specified action is allowed.
             *
             * @param {string} name the name of the action
             * @param {Array.<Action>} actions the list of allowed actions
             * @returns {boolean} {@code true} if the action is allowed, or {@code false} if denied
             */
            hasAction=(name: any, actions: any): boolean=>{
                if(name == null){
                    return true;
                }
                return _.some(actions,(action: any)=>{
                    if (action.systemName === name) {
                        return true;
                    }  else if (angular.isArray(action)) {
                        return this.hasAction(name, action);
                    }else if (angular.isArray(action.actions)) {
                        return this.hasAction(name, action.actions);
                    }
                    return false;
                });
            }

            /**
             * Sets the allowed actions for the specified users and groups.
             *
             * @param {string|null} module name of the access module, or {@code null}
             * @param {string|Array.<string>|null} users user name or list of user names or {@code null}
             * @param {string|Array.<string>|null} groups group name or list of group names or {@code null}
             * @param {Array.<Action>} actions list of actions to allow
             * @returns {Promise} containing an {@link ActionSet} with the saved actions
             */
            setAllowedActions= (module: any, users: any, groups: any, actions: any)=> {
                // Build the request body
                var safeModule: any = angular.isString(module) ? module : this.DEFAULT_MODULE;
                var data: any = {actionSet: {name: safeModule, actions: actions}, change: "REPLACE"};

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
                return this.$http({
                    data: angular.toJson(data),
                    method: "POST",
                    url: this.CommonRestUrlService.SECURITY_BASE_URL + "/actions/" + encodeURIComponent(safeModule) + "/allowed"
                }).then((response: any)=>{
                    if (angular.isUndefined(response.data.actions)) {
                        response.data.actions = [];
                    }
                    return response.data;
                });
            }
            /**
             * Gets all roles abd populates the ROLE_CACHE
             * @returns {*|Request}
             */
            getRoles=()=>{
                  return  this.$http.get(this.CommonRestUrlService.SECURITY_ROLES_URL).then((response: any)=>{
                        _.each(response.data,(roles: any,entityType: any)=>{
                            this.ROLE_CACHE[entityType] = roles;
                        });
                    });
            }
            /**
             * For a given entity type (i.e. FEED) return the roles/permissions
             * @param entityType the type of entity
             */
            getEntityRoles=(entityType: any)=>{
                var df = this.$q.defer();
                var useCache = false; // disable the cache for now

                if(useCache && this.ROLE_CACHE[entityType] != undefined) {
                    df.resolve(angular.copy(this.ROLE_CACHE[entityType]));
                }
                else {
                    var rolesArr: any = [];
                    this.$http.get(this.CommonRestUrlService.SECURITY_ENTITY_ROLES_URL(entityType)).then((response: any)=>{
                        _.each(response.data,(role: any)=>{
                            role.name = role.title;
                            rolesArr.push(role);
                        });
                        this.ROLE_CACHE[entityType] = rolesArr;
                        df.resolve(rolesArr);
                    });

                }
                return df.promise;
            }
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
            hasPermission=(functionalPermission: any,entity?: any,entityPermissions?: any)=> {
                var entityAccessControlled = entity != null && entityPermissions != null && this.isEntityAccessControlled();
                var defer = this.$q.defer();
                var requests = {
                    entityAccess: entityAccessControlled == true ? this.hasEntityAccess(entityPermissions, entity) : true,
                    functionalAccess: this.getUserAllowedActions()
                }
                this.$q.all(requests).then((response: any)=>{
                    defer.resolve(response.entityAccess && this.hasAction(functionalPermission, response.functionalAccess.actions));
                });
                return defer.promise;
            }

        //});
}
angular.module(moduleName).service("AccessControlService",AccessControlService );
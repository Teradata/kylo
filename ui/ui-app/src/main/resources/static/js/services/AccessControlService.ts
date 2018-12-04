import {tap} from "rxjs/operators/tap";
import AccessConstants from '../constants/AccessConstants';
import * as _ from "underscore";
import {CommonRestUrlService} from "./CommonRestUrlService";
import {UserGroupService} from "./UserGroupService";
import {EntityAccessControlService} from "../feed-mgr/shared/entity-access-control/EntityAccessControlService";

import { Injectable } from '@angular/core';
import { ObjectUtils } from '../../lib/common/utils/object-utils';
import { CloneUtil } from '../common/utils/clone-util';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';

@Injectable()
export class AccessControlService extends AccessConstants {
    /**
    * Interacts with the Access Control REST API.
    * @constructor
    */
    DEFAULT_MODULE: any = "services";
    currentUser: any = null;
    entityAccessControlled: any;
    cacheUserAllowedActionsTime: any = 3000 * 60;
    lastUserAllowedCacheAccess: any = {};


    constructor(private http: HttpClient,
        private commonRestUrlService: CommonRestUrlService,
        private userGroupService: UserGroupService) {
        /**
         * Time allowed before the getAllowedActions refreshes from the server
         * Default to refresh the cache every 3 minutes
         */
        super();
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
    AVAILABLE_ACTIONS_: any = null;
    executingAllowedActions: any = {};
    cachedUserAllowedActions: any = {};
    initialized: boolean = false;
    ACCESS_MODULES: any = {
        SERVICES: "services"
    };
    userAllowedActionsNeedsRefresh(module: any) {
        if (ObjectUtils.isUndefined(this.lastUserAllowedCacheAccess[module])) {
            return true;
        }
        else {
            var diff = new Date().getTime() - this.lastUserAllowedCacheAccess[module];
            if (diff > this.cacheUserAllowedActionsTime) {
                return true;
            }
            else {
                return false;
            }
        }
    };
    /**
     * Initialize the service
     */
    init() {
        //build the user access and role/permission cache//  roles:this.getRoles()
        Observable.forkJoin(this.getUserAllowedActions(this.DEFAULT_MODULE, true), this.getRoles(), this.getCurrentUser()/*, this.checkEntityAccessControlled()*/)
            .subscribe((result) => {
                this.initialized = true;
                this.currentUser = result[2];
            });
    }
    hasEntityAccess(requiredPermissions: any, entity: any, entityType?: any) {
        //all entities should have the object .allowedActions and .owner
        if (entity == undefined) {
            return false;
        }

        //short circuit if the owner matches
        if (entity.owner && entity.owner.systemName == this.currentUser.systemName) {
            return true;
        }

        if (!Array.isArray(requiredPermissions)) {
            requiredPermissions = [requiredPermissions];
        }

        return this.hasAnyAction(requiredPermissions, entity.allowedActions);
    }
    isFutureState(state: any) {
        return state.endsWith(".**");
    }
    /**
     * Check to see if we are using entity access control or not
     * @returns {*}
     */
    isEntityAccessControlled() {
        return this.entityAccessControlled;
    }

    checkEntityAccessControlled() {
        if (ObjectUtils.isDefined(this.entityAccessControlled)) {
            return Observable.of(this.entityAccessControlled);
        }
        else {
            return this.http.get(this.commonRestUrlService.ENTITY_ACCESS_CONTROLLED_CHECK).pipe(tap(response => this.entityAccessControlled = response));
        }
    }

    /**
     * Determines if a user has access to a give ui-router state transition
     * This is accessed via the 'routes.js' ui-router listener
     * @param transition a ui-router state transition.
     * @returns {boolean} true if has access, false if access deined
     */
    hasAccess(transition: any) {
        var valid: boolean = false;
        if (transition) {
            var toState = transition.to();
            var toStateName = toState.name;
            var data = toState.data;
            if (data == undefined) {
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
            } else {
                //check to see if the user has the required permission(s) in the String or [array] from the data.permissions object

                if (this.initialized) {
                    var allowedActions = this.cachedUserAllowedActions[this.DEFAULT_MODULE];
                    if (Array.isArray(requiredPermissions)) {

                        //find the first match
                        valid = requiredPermissions.length == 0 || this.hasAllActions(requiredPermissions, allowedActions)
                    }
                    else {
                        valid = this.hasAction(requiredPermissions, allowedActions);
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
    getCurrentUser() {
        return this.userGroupService.getCurrentUser();
    }
    /**
     * Gets the list of allowed actions for the specified users or groups. If no users or groups are specified, then gets the allowed actions for the current user.
     *
     * @param {string|null} [opt_module] name of the access module, or {@code null}
     * @param {string|Array.<string>|null} [opt_users] user name or list of user names or {@code null}
     * @param {string|Array.<string>|null} [opt_groups] group name or list of group names or {@code null}
     * @returns {Promise} containing an {@link ActionSet} with the allowed actions
     */
    getAllowedActions(opt_module: any, opt_users: any, opt_groups: any) {

        // Prepare query parameters
        var params: any = {};
        if (Array.isArray(opt_users) || ObjectUtils.isString(opt_users)) {
            params.user = opt_users;
        }
        if (Array.isArray(opt_groups) || ObjectUtils.isString(opt_groups)) {
            params.group = opt_groups;
        }

        // Send request
        var safeModule = ObjectUtils.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
        return this.http.get
            (this.commonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed",
            { params: params }).toPromise().then((response: any) => {
                if (ObjectUtils.isUndefined(response.actions)) {
                    response.actions = [];
                }
                return response;
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

        var safeModule = ObjectUtils.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
        if (ObjectUtils.isUndefined(cache)) {
            cache = true;
        }
        var isExecuting = this.executingAllowedActions[safeModule] != undefined;
        return new Promise((resolve, reject) => {
            if (cache == true && !isExecuting && this.cachedUserAllowedActions[safeModule] != undefined && !this.userAllowedActionsNeedsRefresh(safeModule)) {
                resolve(this.cachedUserAllowedActions[safeModule]);
            }
            else if (!isExecuting) {
                var observable = this.http.get(this.commonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/allowed").toPromise();
                this.executingAllowedActions[safeModule] = observable.then((response: any) => {
                    if (ObjectUtils.isUndefined(response.actions)) {
                        response.actions = [];
                    }
                    resolve(response);
                    //add it to the cache
                    this.lastUserAllowedCacheAccess[safeModule] = new Date().getTime();
                    this.cachedUserAllowedActions[safeModule] = response;
                    //remove the executing request
                    delete this.executingAllowedActions[safeModule];
                    return response;
                });
            }
            else {
                return this.executingAllowedActions[safeModule];
            }
        });
    }
    /**
     * Gets all available actions.
     *
     * @param {string|null} [opt_module] name of the access module, or {@code null}
     * @returns {Promise} containing an {@link ActionSet} with the allowed actions
     */
    getAvailableActions(opt_module?: any) {
        // Send request
        if (this.AVAILABLE_ACTIONS_ === null) {
            var safeModule = ObjectUtils.isString(opt_module) ? encodeURIComponent(opt_module) : this.DEFAULT_MODULE;
            this.AVAILABLE_ACTIONS_ = this.http.get(this.commonRestUrlService.SECURITY_BASE_URL + "/actions/" + safeModule + "/available").toPromise()
                .then((response: any) => {
                    return response;
                });
        }
        return this.AVAILABLE_ACTIONS_;
    };
    findMissingActions= (names: any, actions: any)=>{
                if (names == "" || names == null || names == undefined || (Array.isArray(names) && names.length == 0)) {
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
                if (names == "" || names == null || names == undefined || (Array.isArray(names) && names.length == 0)) {
                    return true;
                }
                var valid = _.every(names,(name: any)=>{
                    return this.hasAction(name.trim(), actions);
                });
                return valid;
            }/**
     * Determines if any name in array of names is included in the allowed actions it will return true, otherwise false
     *
     * @param names an array of names
     * @param actions An array of allowed actions
     * @returns {boolean}
     */
    hasAnyAction(names: any, actions: any) {
        if (names == "" || names == null || names == undefined || (Array.isArray(names) && names.length == 0)) {
            return true;
        }
        var valid = _.some(names, (name: any) => {
            return this.hasAction(name.trim(), actions);
        });
        return valid;
    };
    /**
     * returns a promise with a value of true/false if the user has any of the required permissions
     * @param requiredPermissions array of required permission strings
     */
    doesUserHavePermission(requiredPermissions: any) {
        return new Promise((resolve, reject) => {
            if (requiredPermissions == null || requiredPermissions == undefined || (Array.isArray(requiredPermissions) && requiredPermissions.length == 0)) {
                resolve(true);
            }
            else {
                this.getUserAllowedActions()
                    .then((actionSet: any) => {
                        var allowed = this.hasAnyAction(requiredPermissions, actionSet.actions);
                        resolve(allowed);
                    });
            }
        });
    };
    /**
     * Determines if the specified action is allowed.
     *
     * @param {string} name the name of the action
     * @param {Array.<Action>} actions the list of allowed actions
     * @returns {boolean} {@code true} if the action is allowed, or {@code false} if denied
     */
    hasAction(name: any, actions: any): boolean {
        if (name == null) {
            return true;
        }
        return _.some(actions, (action: any) => {
            if (action.systemName === name) {
                return true;
            } else if (Array.isArray(action)) {
                return this.hasAction(name, action);
            } else if (Array.isArray(action.actions)) {
                return this.hasAction(name, action.actions);
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
    setAllowedActions(module: any, users: any, groups: any, actions: any) {
        // Build the request body
        var safeModule: any = ObjectUtils.isString(module) ? module : this.DEFAULT_MODULE;
        var data: any = { actionSet: { name: safeModule, actions: actions }, change: "REPLACE" };

        if (Array.isArray(users)) {
            data.users = users;
        } else if (ObjectUtils.isString(users)) {
            data.users = [users];
        }

        if (Array.isArray(groups)) {
            data.groups = groups;
        } else if (ObjectUtils.isString(groups)) {
            data.groups = [groups];
        }

        // Send the request
        return this.http.post(this.commonRestUrlService.SECURITY_BASE_URL + "/actions/" + encodeURIComponent(safeModule) + "/allowed", ObjectUtils.toJson(data))
            .toPromise().then((response: any) => {
                if (ObjectUtils.isUndefined(response.actions)) {
                    response.actions = [];
                }
                return response;
            });

    }
    /**
     * Gets all roles abd populates the ROLE_CACHE
     * @returns {*|Request}
     */
    getRoles() {
        return this.http.get(this.commonRestUrlService.SECURITY_ROLES_URL).toPromise().then((response: any) => {
            _.each(response, (roles: any, entityType: any) => {
                this.ROLE_CACHE[entityType] = roles;
            });
        });
    };
    /**
     * For a given entity type (i.e. FEED) return the roles/permissions
     * @param entityType the type of entity
     */
    getEntityRoles(entityType: any) {
        var useCache = false; // disable the cache for now
        return new Promise((resolve, reject) => {
            if (useCache && this.ROLE_CACHE[entityType] != undefined) {
                resolve(CloneUtil.deepCopy(this.ROLE_CACHE[entityType]));
            } else {
                var rolesArr: any = [];
                this.http.get(this.commonRestUrlService.SECURITY_ENTITY_ROLES_URL(entityType)).toPromise().then((response: any) => {
                    _.each(response, (role: any) => {
                        role.name = role.title;
                        rolesArr.push(role);
                    });
                    this.ROLE_CACHE[entityType] = rolesArr;
                    resolve(rolesArr);
                });
            }
        });
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
    hasPermission(functionalPermission: any, entity?: any, entityPermissions?: any) {
        var entityAccessControlled = entity != null && entityPermissions != null && this.isEntityAccessControlled();
        var entityAcces = entityAccessControlled == true ? this.hasEntityAccess(entityPermissions, entity) : true;
        return new Promise((resolve, reject) => {
            this.getUserAllowedActions().then((response: any) => {
                resolve(entityAcces && this.hasAction(functionalPermission, response.actions));
            });
        });
    };
}

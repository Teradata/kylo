/**
 * Metadata for a user with access to Kylo.
 *
 * @typedef {Object} UserPrincipal
 * @property {string|null} displayName display name for this user
 * @property {string|null} email email address for this user
 * @property {boolean} enabled indicates if user is active or disabled
 * @property {Array.<string>} groups system names of groups the user belongs to
 * @property {string} systemName username for this user
 */

/**
 * Metadata for a user group in Kylo.
 *
 * @typedef {Object} GroupPrincipal
 * @property {string|null} description a human-readable summary
 * @property {number} memberCount number of users and groups within the group
 * @property {string} systemName unique name
 * @property {string|null} title human-readable name
 */
import * as angular from 'angular';
import {moduleName} from './module-name';
import CommonRestUrlService from "./CommonRestUrlService";

export class UserGroupService{
    currentUser: any = null;
/**
 * Interacts with the Users REST API.
 * @constructor
 */
static readonly $inject = ["$http","$q","CommonRestUrlService"];
constructor (private $http: any,
            private $q: any,
            private CommonRestUrlService: any) {
}
           getCurrentUser(): Promise<any>{
                var deferred: any = this.$q.defer();
                var user : any= {
                    "displayName": null,
                    "email": null,
                    "enabled": false,
                    "groups": [],
                    "systemName": null
                };

                if(this.currentUser == null){
                    this.$http.get("/proxy/v1/about/me").then((response: any)=>{
                            this.currentUser = response.data;
                            deferred.resolve(this.currentUser);
                    },(response: any)=> {
                        deferred.reject(response);
                    });
                }
                else {
                    deferred.resolve(this.currentUser);
                }
                return deferred.promise;
            }

            /**
             * Gets metadata for the specified group.
             *
             * @param {string} groupId the system name
             * @returns {GroupPrincipal} the group
             */
            getGroup(groupId: any) {
                return this.$http.get(this.CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId))
                    .then(function (response: any) {
                        return response.data;
                    });
            }

            /**
             * Gets metadata on all groups.
             *
             * @returns {Promise} with the list of groups
             */
            getGroups() {
                return this.$http.get(this.CommonRestUrlService.SECURITY_GROUPS_URL)
                    .then(function (response: any) {
                        return response.data;
                    });
            }

            /**
             * Gets metadata for the specified user.
             *
             * @param {string} userId the system name
             * @returns {UserPrincipal} the user
             */
            getUser(userId: any) {
                return this.$http.get(this.CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId))
                    .then(function (response: any) {
                        return response.data;
                    });
            }

            /**
             * Gets metadata on all users.
             *
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsers() {
                return this.$http.get(this.CommonRestUrlService.SECURITY_USERS_URL)
                    .then(function (response: any) {
                        return response.data;
                    });
            }

            /**
             * Gets metadata for all users in the specified group.
             *
             * @param groupId the system name of the group
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsersByGroup(groupId: any) {
                return this.$http.get(this.CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId) + "/users")
                    .then(function (response: any) {
                        return response.data;
                    });
            }
}
angular.module(moduleName).service("UserGroupService",UserGroupService)
 

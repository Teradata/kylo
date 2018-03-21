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

export default class UserGroupService{
/**
 * Interacts with the Users REST API.
 * @constructor
 */
UserGroupService() {
}
constructor (private $http?: any,
            private $q?: any,
            private CommonRestUrlService?: any) {

        var currentUser: any = null;
        angular.extend(UserGroupService.prototype, {

            getCurrentUser: function(){
                var deferred: any = $q.defer();

                var user : any= {
                    "displayName": null,
                    "email": null,
                    "enabled": false,
                    "groups": [],
                    "systemName": null
                };

                if(currentUser == null){
                    $http.get("/proxy/v1/about/me").then(function (response: any) {
                            currentUser = response.data;
                            deferred.resolve(currentUser);
                    },function(response: any) {
                        deferred.reject(response);
                    });
                }
                else {
                    deferred.resolve(currentUser);
                }
                return deferred.promise;
            },

            /**
             * Gets metadata for the specified group.
             *
             * @param {string} groupId the system name
             * @returns {GroupPrincipal} the group
             */
            getGroup: function (groupId: any) {
                return $http.get(CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId))
                    .then(function (response: any) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata on all groups.
             *
             * @returns {Promise} with the list of groups
             */
            getGroups: function () {
                return $http.get(CommonRestUrlService.SECURITY_GROUPS_URL)
                    .then(function (response: any) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata for the specified user.
             *
             * @param {string} userId the system name
             * @returns {UserPrincipal} the user
             */
            getUser: function (userId: any) {
                return $http.get(CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId))
                    .then(function (response: any) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata on all users.
             *
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsers: function () {
                return $http.get(CommonRestUrlService.SECURITY_USERS_URL)
                    .then(function (response: any) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata for all users in the specified group.
             *
             * @param groupId the system name of the group
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsersByGroup: function (groupId: any) {
                return $http.get(CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId) + "/users")
                    .then(function (response: any) {
                        return response.data;
                    });
            },
        });
}
}
 angular.module(moduleName)
 .service('CommonRestUrlService',CommonRestUrlService)
 .factory("UserGroupService", ['$http','$q', 'CommonRestUrlService', UserGroupService]);
 

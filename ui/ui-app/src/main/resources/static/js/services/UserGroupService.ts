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
import CommonRestUrlService from "./CommonRestUrlService";
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export default class UserGroupService {
    currentUser: any = null;
    /**
     * Interacts with the Users REST API.
     * @constructor
     */
    constructor(private http: HttpClient,
        private commonRestUrlService: CommonRestUrlService) {
    }
    getCurrentUser() {
        return new Promise((resolve,reject) => {
            var user: any = {
                "displayName": null,
                "email": null,
                "enabled": false,
                "groups": [],
                "systemName": null
            };
    
            if (this.currentUser == null) {
                this.http.get("/proxy/v1/about/me").toPromise().then((response: any) => {
                    this.currentUser = response;
                    resolve(this.currentUser);
                }, (response: any) => {
                    reject(response);
                });
            }
            else {
                resolve(this.currentUser);
            }
        });
    }

    /**
     * Gets metadata for the specified group.
     *
     * @param {string} groupId the system name
     * @returns {GroupPrincipal} the group
     */
    getGroup(groupId: any) {
        return this.http.get(this.commonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId)).toPromise()
            .then((response: any) => {
                return response;
            });
    }

    /**
     * Gets metadata on all groups.
     *
     * @returns {Promise} with the list of groups
     */
    getGroups() {
        return this.http.get(this.commonRestUrlService.SECURITY_GROUPS_URL).toPromise()
            .then((response: any) => {
                return response;
            });
    }

    /**
     * Gets metadata for the specified user.
     *
     * @param {string} userId the system name
     * @returns {UserPrincipal} the user
     */
    getUser(userId: any) {
        return this.http.get(this.commonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId)).toPromise()
            .then((response: any) => {
                return response;
            });
    }

    /**
     * Gets metadata on all users.
     *
     * @returns {Array.<UserPrincipal>} the users
     */
    getUsers() {
        return this.http.get(this.commonRestUrlService.SECURITY_USERS_URL).toPromise()
            .then((response: any) => {
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
        return this.http.get(this.commonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId) + "/users").toPromise()
            .then((response: any) => {
                return response.data;
            });
    }
}
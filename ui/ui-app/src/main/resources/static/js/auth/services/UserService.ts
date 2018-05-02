import * as angular from 'angular';
/*import 'rxjs/add/operator/toPromise';*/
import {Headers, RequestOptions, Response} from '@angular/http';
//const moduleName = require('auth/module-name');
import {moduleName} from "../module-name";

//@Injectable()
export class UserService {
    headers: Headers;
    options: RequestOptions;

    static $inject = ['$http', 'CommonRestUrlService', 'UserGroupService']

    constructor(private http: any, private CommonRestUrlService: any, private UserGroupService: any) {
    }

    private extractData(res: Response) {
        let body = res.json();
        return body || {};
    }


    private handleError(error: any): Promise<any> {
        //  console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }

    /**
     * Deletes the group with the specified system name.
     *
     * @param {string} groupId the system name
     * @returns {Promise} for when the group is deleted
     */
    deleteGroup(groupId: string): Promise<any> {
        return this.http.delete(this.CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId));
    }

    /**
     * Deletes the user with the specified system name.
     *
     * @param {string} userId the system name
     * @returns {Promise} for when the user is deleted
     */
    deleteUser(userId: any): Promise<any> {
        return this.http.delete(this.CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId)/*,this.options*/)
            /*   .toPromise().then(this.extractData)
               .catch(this.handleError);    */
            ;
    }

    /**
     * Gets metadata for the specified group.
     *
     * @param {string} groupId the system name
     * @returns {GroupPrincipal} the group
     */
    getGroup(groupId: any) {
        return this.UserGroupService.getGroup(groupId);
    }

    /**
     * Gets metadata on all groups.
     *
     * @returns {Promise} with the list of groups
     */
    getGroups(): Promise<any> {
        return this.UserGroupService.getGroups();
        /*   return  this.http.get(this.CommonRestUrlService.SECURITY_GROUPS_URL)
                    .toPromise().then(this.extractData)*/
    }

    /* this.UserGroupService.getGroups()
    /**
     * Gets metadata for the specified user.
     *
     * @param {string} userId the system name
     * @returns {UserPrincipal} the user
     */
    getUser(userId: any) {
        return this.UserGroupService.getUser(userId);
    }

    /**
     * Gets metadata on all users.
     *
     * @returns {Array.<UserPrincipal>} the users
     */
    getUsers(): Promise<any> {
        return this.UserGroupService.getUsers();
        /*return  this.http.get(this.CommonRestUrlService.SECURITY_USERS_URL)
                 .toPromise().then(this.extractData)*/
    }

    /*{
               return this.UserGroupService.getUsers();
           }*/

    /**
     * Gets metadata for all users in the specified group.
     *
     * @param groupId the system name of the group
     * @returns {Array.<UserPrincipal>} the users
     */
    getUsersByGroup(groupId: any): any[] {
        return this.UserGroupService.getUsersByGroup(groupId);
    }

    /**
     * Saves the specified group.
     *
     * @param {GroupPrincipal} group the group
     * @returns {Promise} for when the group is saved
     */
    saveGroup(group: any): Promise<any> {
        return this.http.post(this.CommonRestUrlService.SECURITY_GROUPS_URL, angular.toJson(group)/*,this.options*/)
            /* .toPromise().then(this.extractData)
             .catch(this.handleError); */
            ;
    }

    /**
     * Saves the specified user.
     *
     * @param {UserPrincipal} user the user
     * @returns {Promise} for when the user is saved
     */
    saveUser(user: any): Promise<any> {
        return this.http.post(this.CommonRestUrlService.SECURITY_USERS_URL, angular.toJson(user)/*,this.options*/)
            /* .toPromise().then(this.extractData)
             .catch(this.handleError);  */
            ;

    }
}


angular.module(moduleName)
    .service('UserService', UserService);
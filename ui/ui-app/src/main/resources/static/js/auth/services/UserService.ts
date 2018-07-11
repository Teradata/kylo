import * as angular from 'angular';
/*import 'rxjs/add/operator/toPromise';*/
import { RequestOptions, Response} from '@angular/http';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import CommonRestUrlService from '../../services/CommonRestUrlService';
import UserGroupService from '../../services/UserGroupService';

@Injectable()
export default class UserService {
    headers = new HttpHeaders({'Content-Type':'application/json; charset=utf-8'});
    options: RequestOptions;

    constructor(private http: HttpClient, private commonRestUrlService: CommonRestUrlService, private userGroupService: UserGroupService) {
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
        return this.http.delete(this.commonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId)).toPromise();
    }

    /**
     * Deletes the user with the specified system name.
     *
     * @param {string} userId the system name
     * @returns {Promise} for when the user is deleted
     */
    deleteUser(userId: any): Promise<any> {
        return this.http.delete(this.commonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId)).toPromise()/*,this.options*/
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
        return this.userGroupService.getGroup(groupId);
    }

    /**
     * Gets metadata on all groups.
     *
     * @returns {Promise} with the list of groups
     */
    getGroups(): Promise<any> {
        return this.userGroupService.getGroups();
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
        return this.userGroupService.getUser(userId);
    }

    /**
     * Gets metadata on all users.
     *
     * @returns {Array.<UserPrincipal>} the users
     */
    getUsers(): Promise<any> {
        return this.userGroupService.getUsers();
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
    getUsersByGroup(groupId: any): any {
        return this.userGroupService.getUsersByGroup(groupId);
    }

    /**
     * Saves the specified group.
     *
     * @param {GroupPrincipal} group the group
     * @returns {Promise} for when the group is saved
     */
    saveGroup(group: any): Promise<any> {
        return this.http.post(this.commonRestUrlService.SECURITY_GROUPS_URL, angular.toJson(group),{headers :this.headers}/*,this.options*/).toPromise()
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
        return this.http.post(this.commonRestUrlService.SECURITY_USERS_URL, angular.toJson(user),{headers :this.headers}/*,this.options*/).toPromise()
            /* .toPromise().then(this.extractData)
             .catch(this.handleError);  */
            ;

    }
} 
define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    //@Injectable()
    var UserService = /** @class */ (function () {
        function UserService(http, CommonRestUrlService, UserGroupService) {
            this.http = http;
            this.CommonRestUrlService = CommonRestUrlService;
            this.UserGroupService = UserGroupService;
        }
        UserService.prototype.extractData = function (res) {
            var body = res.json();
            return body || {};
        };
        UserService.prototype.handleError = function (error) {
            //  console.error('An error occurred', error);
            return Promise.reject(error.message || error);
        };
        /**
         * Deletes the user with the specified system name.
         *
         * @param {string} userId the system name
         * @returns {Promise} for when the user is deleted
         */
        UserService.prototype.deleteUser = function (userId) {
            return this.http.delete(this.CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId) /*,this.options*/);
        };
        /**
         * Gets metadata for the specified group.
         *
         * @param {string} groupId the system name
         * @returns {GroupPrincipal} the group
         */
        UserService.prototype.getGroup = function (groupId) {
            return this.UserGroupService.getGroup(groupId);
        };
        /**
         * Gets metadata on all groups.
         *
         * @returns {Promise} with the list of groups
         */
        UserService.prototype.getGroups = function () {
            return this.UserGroupService.getGroups();
            /*   return  this.http.get(this.CommonRestUrlService.SECURITY_GROUPS_URL)
                        .toPromise().then(this.extractData)*/
        };
        /* this.UserGroupService.getGroups()
        /**
         * Gets metadata for the specified user.
         *
         * @param {string} userId the system name
         * @returns {UserPrincipal} the user
         */
        UserService.prototype.getUser = function (userId) {
            return this.UserGroupService.getUser(userId);
        };
        /**
         * Gets metadata on all users.
         *
         * @returns {Array.<UserPrincipal>} the users
         */
        UserService.prototype.getUsers = function () {
            return this.UserGroupService.getUsers();
            /*return  this.http.get(this.CommonRestUrlService.SECURITY_USERS_URL)
                     .toPromise().then(this.extractData)*/
        }; /*{
return this.UserGroupService.getUsers();
}*/
        /**
         * Gets metadata for all users in the specified group.
         *
         * @param groupId the system name of the group
         * @returns {Array.<UserPrincipal>} the users
         */
        UserService.prototype.getUsersByGroup = function (groupId) {
            return this.UserGroupService.getUsersByGroup(groupId);
        };
        /**
         * Saves the specified group.
         *
         * @param {GroupPrincipal} group the group
         * @returns {Promise} for when the group is saved
         */
        UserService.prototype.saveGroup = function (group) {
            return this.http.post(this.CommonRestUrlService.SECURITY_GROUPS_URL, angular.toJson(group) /*,this.options*/);
        };
        /**
         * Saves the specified user.
         *
         * @param {UserPrincipal} user the user
         * @returns {Promise} for when the user is saved
         */
        UserService.prototype.saveUser = function (user) {
            return this.http.post(this.CommonRestUrlService.SECURITY_USERS_URL, angular.toJson(user) /*,this.options*/);
        };
        return UserService;
    }());
    exports.UserService = UserService;
    /*angular.module(moduleName)
           .service('UserService',
                    [
                        //'$http',
                    // 'CommonRestUrlService',
                    // 'UserGroupService',
                     ()=>{return new UserService('$http',
                                                'CommonRestUrlService',
                                                'UserGroupService');}
                    ]
                );*/
    angular.module(module_name_1.moduleName)
        .service('UserService', ['$http',
        'CommonRestUrlService',
        'UserGroupService', UserService]);
});
//# sourceMappingURL=UserService.js.map
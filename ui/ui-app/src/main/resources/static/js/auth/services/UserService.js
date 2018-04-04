var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "angular", "@angular/core", "../module-name", "../../services/UserGroupService", "../../services/CommonRestUrlService"], function (require, exports, angular, core_1, module_name_1, UserGroupService_1, CommonRestUrlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var UserService = /** @class */ (function () {
        function UserService($http, CommonRestUrlService, UserGroupService) {
            this.$http = $http;
            this.CommonRestUrlService = CommonRestUrlService;
            this.UserGroupService = UserGroupService;
        }
        UserService.prototype.extractData = function (res) {
            var body = res.json();
            return body || {};
        };
        UserService.prototype.handleError = function (error) {
            return Promise.reject(error.message || error);
        };
        /**
         * Deletes the group with the specified system name.
         *
         * @param {string} groupId the system name
         * @returns {Promise} for when the group is deleted
         */
        UserService.prototype.deleteGroup = function (groupId) {
            return Promise.resolve(this.$http({
                method: "DELETE",
                url: this.CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId)
            }));
        };
        /**
         * Deletes the user with the specified system name.
         * @param {string} userId the system name
         * @returns {Promise} for when the user is deleted
         */
        UserService.prototype.deleteUser = function (userId) {
            return Promise.resolve(this.$http({
                method: "DELETE",
                url: this.CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId)
            }));
            // return this.http.delete(this.CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId));
        };
        /**
         * Gets metadata for the specified group.
         * @param {string} groupId the system name
         * @returns {GroupPrincipal} the group
         */
        UserService.prototype.getGroup = function (groupId) {
            return this.UserGroupService.getGroup(groupId);
        };
        /**
         * Gets metadata on all groups.
         * @returns {Promise} with the list of groups
         */
        UserService.prototype.getGroups = function () {
            return this.UserGroupService.getGroups();
        };
        /**
         * Gets metadata for the specified user.
         * @param {string} userId the system name
         * @returns {UserPrincipal} the user
         */
        UserService.prototype.getUser = function (userId) {
            return this.UserGroupService.getUser(userId);
        };
        /**
         * Gets metadata on all users.
         * @returns {Array.<UserPrincipal>} the users
         */
        UserService.prototype.getUsers = function () {
            return this.UserGroupService.getUsers();
        };
        /**
         * Gets metadata for all users in the specified group.
         * @param groupId the system name of the group
         * @returns {Array.<UserPrincipal>} the users
         */
        UserService.prototype.getUsersByGroup = function (groupId) {
            return this.UserGroupService.getUsersByGroup(groupId);
        };
        /**
         * Saves the specified group.
         * @param {GroupPrincipal} group the group
         * @returns {Promise} for when the group is saved
         */
        UserService.prototype.saveGroup = function (group) {
            return Promise.resolve(this.$http({
                data: angular.toJson(group),
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                method: "POST",
                url: this.CommonRestUrlService.SECURITY_GROUPS_URL
            })).then(function () {
                return group;
            });
            //            return this.http.post(this.CommonRestUrlService.SECURITY_GROUPS_URL,angular.toJson(group));
        };
        /**
         * Saves the specified user.
         * @param {UserPrincipal} user the user
         * @returns {Promise} for when the user is saved
         */
        UserService.prototype.saveUser = function (user) {
            return Promise.resolve(this.$http({
                data: angular.toJson(user),
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                method: "POST",
                url: this.CommonRestUrlService.SECURITY_USERS_URL
            })).then(function () {
                return user;
            });
            //        return this.http.post(this.CommonRestUrlService.SECURITY_USERS_URL,angular.toJson(user));
        };
        UserService.$inject = ['$http', 'CommonRestUrlService', 'UserGroupService'];
        UserService = __decorate([
            core_1.Injectable(),
            __metadata("design:paramtypes", [Function, CommonRestUrlService_1.default,
                UserGroupService_1.default])
        ], UserService);
        return UserService;
    }());
    exports.default = UserService;
    angular.module(module_name_1.moduleName).service('UserService', UserService);
});
//# sourceMappingURL=UserService.js.map
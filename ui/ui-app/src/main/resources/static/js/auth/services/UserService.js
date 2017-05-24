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
define(['angular','auth/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory("UserService", ['$http', 'CommonRestUrlService','UserGroupService', function ($http, CommonRestUrlService,UserGroupService) {

        /**
         * Interacts with the Users REST API.
         *
         * @constructor
         */
        function UserService() {

        }

        angular.extend(UserService.prototype, {
            /**
             * Deletes the group with the specified system name.
             *
             * @param {string} groupId the system name
             * @returns {Promise} for when the group is deleted
             */
            deleteGroup: function (groupId) {
                return $http({
                    method: "DELETE",
                    url: CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId)
                });
            },

            /**
             * Deletes the user with the specified system name.
             *
             * @param {string} userId the system name
             * @returns {Promise} for when the user is deleted
             */
            deleteUser: function (userId) {
                return $http({
                    method: "DELETE",
                    url: CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId)
                });
            },

            /**
             * Gets metadata for the specified group.
             *
             * @param {string} groupId the system name
             * @returns {GroupPrincipal} the group
             */
            getGroup: function (groupId) {
                return UserGroupService.getGroup(groupId);
            },

            /**
             * Gets metadata on all groups.
             *
             * @returns {Promise} with the list of groups
             */
            getGroups: function () {
                return UserGroupService.getGroups();
            },

            /**
             * Gets metadata for the specified user.
             *
             * @param {string} userId the system name
             * @returns {UserPrincipal} the user
             */
            getUser: function (userId) {
                return UserGroupService.getUser(userId);
            },

            /**
             * Gets metadata on all users.
             *
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsers: function () {
                return UserGroupService.getUsers();
            },

            /**
             * Gets metadata for all users in the specified group.
             *
             * @param groupId the system name of the group
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsersByGroup: function (groupId) {
                return UserGroupService.getUsersByGroup(groupId);
            },

            /**
             * Saves the specified group.
             *
             * @param {GroupPrincipal} group the group
             * @returns {Promise} for when the group is saved
             */
            saveGroup: function (group) {
                return $http({
                    data: angular.toJson(group),
                    headers: {"Content-Type": "application/json; charset=UTF-8"},
                    method: "POST",
                    url: CommonRestUrlService.SECURITY_GROUPS_URL
                }).then(function () {
                    return group;
                });
            },

            /**
             * Saves the specified user.
             *
             * @param {UserPrincipal} user the user
             * @returns {Promise} for when the user is saved
             */
            saveUser: function (user) {
                return $http({
                    data: angular.toJson(user),
                    headers: {"Content-Type": "application/json; charset=UTF-8"},
                    method: "POST",
                    url: CommonRestUrlService.SECURITY_USERS_URL
                }).then(function () {
                    return user;
                });
            }
        });

        return new UserService();
    }]);
});

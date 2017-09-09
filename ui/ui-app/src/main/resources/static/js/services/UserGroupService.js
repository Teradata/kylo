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
define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory("UserGroupService", ['$http','$q', 'CommonRestUrlService', function ($http, $q,CommonRestUrlService) {


        var currentUser = null;
        /**
         * Interacts with the Users REST API.
         *
         * @constructor
         */
        function UserGroupService() {

        }

        angular.extend(UserGroupService.prototype, {

            getCurrentUser: function(){
                var deferred = $q.defer();

                var user = {
                    "displayName": null,
                    "email": null,
                    "enabled": false,
                    "groups": [],
                    "systemName": null
                };

                if(currentUser == null){
                    $http.get("/proxy/v1/about/me").then(function (response) {
                            currentUser = response.data;
                            deferred.resolve(currentUser);
                    },function(response) {
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
            getGroup: function (groupId) {
                return $http.get(CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId))
                    .then(function (response) {
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
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata for the specified user.
             *
             * @param {string} userId the system name
             * @returns {UserPrincipal} the user
             */
            getUser: function (userId) {
                return $http.get(CommonRestUrlService.SECURITY_USERS_URL + "/" + encodeURIComponent(userId))
                    .then(function (response) {
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
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Gets metadata for all users in the specified group.
             *
             * @param groupId the system name of the group
             * @returns {Array.<UserPrincipal>} the users
             */
            getUsersByGroup: function (groupId) {
                return $http.get(CommonRestUrlService.SECURITY_GROUPS_URL + "/" + encodeURIComponent(groupId) + "/users")
                    .then(function (response) {
                        return response.data;
                    });
            },


        });

        return new UserGroupService();
    }]);
});

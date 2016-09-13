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

angular.module(MODULE_FEED_MGR).factory("UserService", function($http, RestUrlService) {

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
        deleteGroup: function(groupId) {
            return $http({
                method: "DELETE",
                url: RestUrlService.SECURITY_GROUPS_URL + "/" + groupId
            });
        },

        /**
         * Deletes the user with the specified system name.
         *
         * @param {string} userId the system name
         * @returns {Promise} for when the user is deleted
         */
        deleteUser: function(userId) {
            return $http({
                method: "DELETE",
                url: RestUrlService.SECURITY_USERS_URL + "/" + userId
            });
        },

        /**
         * Gets metadata for the specified group.
         *
         * @param {string} groupId the system name
         * @returns {GroupPrincipal} the group
         */
        getGroup: function(groupId) {
            return $http.get(RestUrlService.SECURITY_GROUPS_URL + "/" + groupId)
                    .then(function(response) {
                        return response.data;
                    });
        },

        /**
         * Gets metadata on all groups.
         *
         * @returns {Promise} with the list of groups
         */
        getGroups: function() {
            return $http.get(RestUrlService.SECURITY_GROUPS_URL)
                    .then(function(response) {
                        return response.data;
                    });
        },

        /**
         * Gets metadata for the specified user.
         *
         * @param {string} userId the system name
         * @returns {UserPrincipal} the user
         */
        getUser: function(userId) {
            return $http.get(RestUrlService.SECURITY_USERS_URL + "/" + userId)
                    .then(function(response) {
                        return response.data;
                    });
        },

        /**
         * Gets metadata on all users.
         *
         * @returns {Array.<UserPrincipal>} the users
         */
        getUsers: function() {
            return $http.get(RestUrlService.SECURITY_USERS_URL)
                    .then(function(response) {
                        return response.data;
                    });
        },

        /**
         * Saves the specified group.
         *
         * @param {GroupPrincipal} group the group
         * @returns {Promise} for when the group is saved
         */
        saveGroup: function(group) {
            return $http({
                data: angular.toJson(group),
                headers: {"Content-Type": "application/json; charset=UTF-8"},
                method: "POST",
                url: RestUrlService.SECURITY_GROUPS_URL
            }).then(function() {
                return group;
            });
        },

        /**
         * Saves the specified user.
         *
         * @param {UserPrincipal} user the user
         * @returns {Promise} for when the user is saved
         */
        saveUser: function(user) {
            return $http({
                data: angular.toJson(user),
                headers: {"Content-Type": "application/json; charset=UTF-8"},
                method: "POST",
                url: RestUrlService.SECURITY_USERS_URL
            }).then(function() {
                return user;
            });
        }
    });

    return new UserService();
});

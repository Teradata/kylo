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
 * @property {string} description a human-readable summary
 * @property {string} systemName unique name
 * @property {string} title human-readable name
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
         * Gets metadata on all groups.
         *
         * @returns {Array.<GroupPrincipal>} the groups
         */
        getGroups: function() {
            return $http.get(RestUrlService.SECURITY_GROUPS_URL)
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
        }
    });

    return new UserService();
});

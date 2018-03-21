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
define(["require", "exports", "angular", "./module-name", "./CommonRestUrlService"], function (require, exports, angular, module_name_1, CommonRestUrlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var UserGroupService = /** @class */ (function () {
        function UserGroupService($http, $q, CommonRestUrlService) {
            this.$http = $http;
            this.$q = $q;
            this.CommonRestUrlService = CommonRestUrlService;
            var currentUser = null;
            angular.extend(UserGroupService.prototype, {
                getCurrentUser: function () {
                    var deferred = $q.defer();
                    var user = {
                        "displayName": null,
                        "email": null,
                        "enabled": false,
                        "groups": [],
                        "systemName": null
                    };
                    if (currentUser == null) {
                        $http.get("/proxy/v1/about/me").then(function (response) {
                            currentUser = response.data;
                            deferred.resolve(currentUser);
                        }, function (response) {
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
        }
        /**
         * Interacts with the Users REST API.
         * @constructor
         */
        UserGroupService.prototype.UserGroupService = function () {
        };
        return UserGroupService;
    }());
    exports.default = UserGroupService;
    angular.module(module_name_1.moduleName)
        .service('CommonRestUrlService', CommonRestUrlService_1.default)
        .factory("UserGroupService", ['$http', '$q', 'CommonRestUrlService', UserGroupService]);
});
//# sourceMappingURL=UserGroupService.js.map
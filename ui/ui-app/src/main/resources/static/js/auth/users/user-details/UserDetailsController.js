define(["require", "exports", "angular", "underscore", "../../module-name", "../../../constants/AccessConstants", "../../module", "../../module-require"], function (require, exports, angular, _, module_name_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var UserDetailsController = /** @class */ (function () {
        function UserDetailsController($scope, $mdDialog, $mdToast, 
            //private $transition$: any,
            accessControlService, UserService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.accessControlService = accessControlService;
            this.UserService = UserService;
            this.StateService = StateService;
            this.$error = { duplicateUser: false, missingGroup: false, missingUser: false };
            /**
             * Indicates that admin operations are allowed.
             * @type {boolean}
             */
            this.allowAdmin = false;
            /**
             * User model for the edit view.
             * @type {UserPrincipal}
             */
            this.editModel = {};
            /**
             * List of group system names.
             * @type {Array.<string>}
             */
            this.groupList = [];
            /**
             * Map of group system names to group objects.
             * @type {Object.<string, GroupPrincipal>}
             */
            this.groupMap = {};
            /**
             * Autocomplete search text for group input.
             * @type {string}
             */
            this.groupSearchText = "";
            /**
             * Indicates if the edit view is displayed.
             * @type {boolean}
             */
            this.isEditable = false;
            /**
             * Indicates if the edit form is valid.
             * @type {boolean}
             */
            this.isValid = false;
            /**
             * Indicates that the user is currently being loaded.
             * @type {boolean}
             */
            this.loading = true;
            /**
             * User model for the read-only view.
             * @type {UserPrincipal}
             */
            this.model = { displayName: null, email: null, enabled: true, groups: [], systemName: null };
            /**
             * Lookup map for detecting duplicate user names.
             * @type {Object.<string, boolean>}
             */
            this.userMap = {};
            /**
             * Gets the title for the specified group.
             *
             * @param group the group
             * @returns {string} the group title
             */
            this.getGroupTitle = function (group) {
                if (angular.isDefined(_this.groupMap[group]) && angular.isString(_this.groupMap[group].title)) {
                    return _this.groupMap[group].title;
                }
                else {
                    return group;
                }
            };
            $scope.$watch(function () { return _this.$error; }, function () {
                _this.isValid = _.reduce(_this.$error, function (memo, value) {
                    return memo && !value;
                }, true);
            }, true);
            // Update $error.missingGroup when the edit model changes
            $scope.$watch(function () { return _this.editModel.groups; }, function () { _this.$error.missingGroup = (angular.isUndefined(_this.editModel.groups) || _this.editModel.groups.length === 0); }, true);
            // Update $error when the system name changes
            $scope.$watch(function () { return _this.editModel.systemName; }, function () {
                _this.$error.duplicateUser = (angular.isString(_this.editModel.systemName) && _this.userMap[_this.editModel.systemName]);
                _this.$error.missingUser = (!angular.isString(_this.editModel.systemName) || _this.editModel.systemName.length === 0);
            });
            this.onLoad();
        }
        UserDetailsController.prototype.ngOnInit = function () {
        };
        /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
         */
        UserDetailsController.prototype.canDelete = function () {
            return (this.model.systemName !== null);
        };
        ;
        /**
 * Finds the substring of the title for the specified group that matches the query term.
 *
 * @param group the group
 * @returns {string} the group title substring
 */
        UserDetailsController.prototype.findGroupSearchText = function (group) {
            var safeQuery = this.groupSearchText.toLocaleUpperCase();
            if (angular.isString(this.groupMap[group].title)) {
                var titleIndex = this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery);
                return (titleIndex > -1) ? this.groupMap[group].title.substr(titleIndex, safeQuery.length) : this.groupSearchText;
            }
            else {
                var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                return (nameIndex > -1) ? group.substr(nameIndex, safeQuery.length) : this.groupSearchText;
            }
        };
        ;
        /**
         * Gets the titles for every group this user belongs to.
         *
         * @returns {Array.<string>} the group titles for this user
         */
        UserDetailsController.prototype.getGroupTitles = function () {
            return _.map(this.model.groups, this.getGroupTitle);
        };
        ;
        /**
         * Cancels the current edit operation. If a new user is being created then redirects to the users page.
         */
        UserDetailsController.prototype.onCancel = function () {
            if (this.model.systemName === null) {
                this.StateService.Auth().navigateToUsers();
            }
        };
        ;
        /**
         * Deletes the current user.
         */
        UserDetailsController.prototype.onDelete = function () {
            var _this = this;
            var name = (angular.isString(this.model.displayName) && this.model.displayName.length > 0) ? this.model.displayName : this.model.systemName;
            this.UserService.deleteUser(encodeURIComponent(this.model.systemName))
                .then(function () {
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent("Successfully deleted the user " + name)
                    .hideDelay(3000));
                _this.StateService.Auth().navigateToUsers();
            }, function () {
                _this.$mdDialog.show(_this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Delete Failed")
                    .textContent("The user " + name + " could not be deleted. ") //+ err.data.message
                    .ariaLabel("Failed to delete user")
                    .ok("Got it!"));
            });
        };
        ;
        /**
        //  * Creates a copy of the user model for editing.
         */
        UserDetailsController.prototype.onEdit = function () {
            this.editModel = angular.copy(this.model);
        };
        ;
        /**
         * Loads the user details.
         */
        UserDetailsController.prototype.onLoad = function () {
            var _this = this;
            // Get the list of groups
            this.UserService.getGroups()
                .then(function (groups) {
                _this.groupList = [];
                _this.groupMap = {};
                angular.forEach(groups, function (group) {
                    _this.groupList.push(group.systemName);
                    _this.groupMap[group.systemName] = group;
                });
            });
            // Load allowed permissions
            this.accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = _this.accessControlService.hasAction(AccessConstants_1.default.USERS_ADMIN, actionSet.actions);
            });
            // Load the user details
            if (angular.isString(this.$transition$.params().userId)) {
                this.UserService.getUser(this.$transition$.params().userId)
                    .then(function (user) {
                    _this.model = user;
                    _this.loading = false;
                });
            }
            else {
                this.onEdit();
                this.isEditable = true;
                this.loading = false;
                this.UserService.getUsers()
                    .then(function (users) {
                    _this.userMap = {};
                    angular.forEach(users, function (user) {
                        _this.userMap[user.systemName] = true;
                    });
                });
            }
        };
        ;
        /**
         * Saves the current user.
         */
        UserDetailsController.prototype.onSave = function () {
            var _this = this;
            var model = angular.copy(this.editModel);
            this.UserService.saveUser(model)
                .then(function () {
                _this.model = model;
            });
        };
        ;
        /**
         * Filters the list of groups to those matching the specified query.
         *
         * @param {string} query the query string
         * @returns {Array.<string>} the list of matching groups
         */
        UserDetailsController.prototype.queryGroups = function (query) {
            var _this = this;
            var safeQuery = query.toLocaleUpperCase();
            return this.groupList
                .filter(function (group) {
                return (_this.editModel.groups.indexOf(group) === -1);
            })
                .map(function (group) {
                var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                var titleIndex = angular.isString(_this.groupMap[group].title) ? _this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery) : -1;
                var index = (titleIndex > -1 && (nameIndex === -1 || nameIndex > titleIndex)) ? titleIndex : nameIndex;
                return { name: group, index: index };
            })
                .filter(function (item) {
                return item.index > -1;
            })
                .sort(function (a, b) {
                return a.index - b.index;
            })
                .map(function (item) {
                return item.name;
            });
        };
        ;
        UserDetailsController.$inject = ["$scope", "$mdDialog", "$mdToast",
            "AccessControlService", "UserService", "StateService"];
        return UserDetailsController;
    }());
    exports.default = UserDetailsController;
    angular.module(module_name_1.moduleName)
        .component("userDetailsController", {
        bindings: {
            $transition$: '<'
        },
        controller: UserDetailsController,
        controllerAs: "vm",
        templateUrl: "js/auth/users/user-details/user-details.html"
    });
});
//.controller('UserDetailsController', ["$scope","$mdDialog","$mdToast","$transition$","AccessControlService","UserService","StateService",UserDetailsController]);
//# sourceMappingURL=UserDetailsController.js.map
define(["require", "exports", "angular", "underscore", "../../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var UserDetailsController = /** @class */ (function () {
        function UserDetailsController($scope, $mdDialog, $mdToast, $transition$, AccessControlService, UserService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$transition$ = $transition$;
            this.AccessControlService = AccessControlService;
            this.UserService = UserService;
            this.StateService = StateService;
            this.$error = { duplicateUser: false, missingGroup: false, missingUser: false };
            this.allowAdmin = false; //Indicates that admin operations are allowed  {boolean}
            this.editModel = {}; //User model for the edit view. {UserPrincipal}
            this.groupList = []; //List of group system names {Array.<string>}
            this.groupMap = {}; //Map of group system names to group objects {Object.<string, GroupPrincipal>}
            this.groupSearchText = ""; //Autocomplete search text for group input  {string}
            this.isEditable = false; //Indicates if the edit view is displayed {boolean}
            this.isValid = false; //Indicates if the edit form is valid {boolean}
            this.loading = true; //Indicates that the user is currently being loaded {boolean}
            this.model = { displayName: null, email: null, enabled: true, groups: [], systemName: null }; //User model for the read-only view {UserPrincipal}
            this.userMap = {}; //Lookup map for detecting duplicate user names {Object.<string, boolean>}
            /**
             * Indicates if the user can be deleted. The main requirement is that the user exists.
             * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
             */
            this.canDelete = function () {
                return (_this.model.systemName !== null);
            };
            /**
             * Finds the substring of the title for the specified group that matches the query term.
             * @param group the group
             * @returns {string} the group title substring
             */
            this.findGroupSearchText = function (group) {
                var safeQuery = _this.groupSearchText.toLocaleUpperCase();
                if (angular.isString(_this.groupMap[group].title)) {
                    var titleIndex = _this.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery);
                    return (titleIndex > -1) ? _this.groupMap[group].title.substr(titleIndex, safeQuery.length) : _this.groupSearchText;
                }
                else {
                    var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                    return (nameIndex > -1) ? group.substr(nameIndex, safeQuery.length) : _this.groupSearchText;
                }
            };
            /**
             * Gets the title for the specified group.
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
            $scope.$watch(// Update $error.missingGroup when the edit model changes
            function () { return _this.editModel.groups; }, function () { _this.$error.missingGroup = (angular.isUndefined(_this.editModel.groups) || _this.editModel.groups.length === 0); }, true);
            $scope.$watch(// Update $error when the system name changes
            function () { return _this.editModel.systemName; }, function () {
                _this.$error.duplicateUser = (angular.isString(_this.editModel.systemName) && _this.userMap[_this.editModel.systemName]);
                _this.$error.missingUser = (!angular.isString(_this.editModel.systemName) || _this.editModel.systemName.length === 0);
            });
            this.onLoad();
        } // end of constructor
        UserDetailsController.prototype.ngOnInit = function () { };
        /**
         * Gets the titles for every group this user belongs to.
         * @returns {Array.<string>} the group titles for this user
         */
        UserDetailsController.prototype.getGroupTitles = function () {
            return _.map(this.model.groups, this.getGroupTitle);
        };
        ;
        UserDetailsController.prototype.onCancel = function () {
            if (this.model.systemName === null) {
                this.StateService.Auth().navigateToUsers();
            }
        };
        ;
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
        UserDetailsController.prototype.onEdit = function () {
            this.editModel = angular.copy(this.model);
        };
        ;
        UserDetailsController.prototype.onLoad = function () {
            var _this = this;
            this.UserService.getGroups() // Get the list of groups
                .then(function (groups) {
                _this.groupList = [];
                _this.groupMap = {};
                angular.forEach(groups, function (group) {
                    _this.groupList.push(group.systemName);
                    _this.groupMap[group.systemName] = group;
                });
            });
            this.AccessControlService.getUserAllowedActions() // Load allowed permissions
                .then(function (actionSet) {
                _this.allowAdmin = _this.AccessControlService.hasAction(_this.AccessControlService.USERS_ADMIN, actionSet.actions);
            });
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
        return UserDetailsController;
    }());
    exports.default = UserDetailsController;
    angular.module(module_name_1.moduleName).controller('UserDetailsController', ["$scope", "$mdDialog", "$mdToast", "$transition$", "AccessControlService", "UserService", "StateService", UserDetailsController]);
});
//# sourceMappingURL=UserDetailsController.js.map
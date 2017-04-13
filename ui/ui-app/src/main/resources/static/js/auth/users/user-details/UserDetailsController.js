define(['angular', "auth/module-name"], function (angular, moduleName) {
    /**
     * Adds or updates user details.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdDialog the dialog service
     * @param $mdToast the toast notification service
     * @param $transition$ the URL parameters
     * @param {AccessControlService} AccessControlService the access control service
     * @param UserService the user service
     * @param StateService manages system state
     */
    function UserDetailsController($scope, $mdDialog, $mdToast, $transition$, AccessControlService, UserService, StateService) {
        var self = this;

        /**
         * Error message object that maps keys to a boolean indicating the error state.
         * @type {{duplicateUser: boolean, missingGroup: boolean, missingUser: boolean}}
         */
        self.$error = {duplicateUser: false, missingGroup: false, missingUser: false};

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        /**
         * User model for the edit view.
         * @type {UserPrincipal}
         */
        self.editModel = {};

        /**
         * List of group system names.
         * @type {Array.<string>}
         */
        self.groupList = [];

        /**
         * Map of group system names to group objects.
         * @type {Object.<string, GroupPrincipal>}
         */
        self.groupMap = {};

        /**
         * Autocomplete search text for group input.
         * @type {string}
         */
        self.groupSearchText = "";

        /**
         * Indicates if the edit view is displayed.
         * @type {boolean}
         */
        self.isEditable = false;

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

        /**
         * Indicates that the user is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * User model for the read-only view.
         * @type {UserPrincipal}
         */
        self.model = {displayName: null, email: null, enabled: true, groups: [], systemName: null};

        /**
         * Lookup map for detecting duplicate user names.
         * @type {Object.<string, boolean>}
         */
        self.userMap = {};

        // Update isValid when $error is updated
        $scope.$watch(
                function() {return self.$error},
                function() {
                    self.isValid = _.reduce(self.$error, function(memo, value) {
                        return memo && !value;
                    }, true);
                },
                true
        );

        // Update $error.missingGroup when the edit model changes
        $scope.$watch(
                function() {return self.editModel.groups},
                function() {self.$error.missingGroup = (angular.isUndefined(self.editModel.groups) || self.editModel.groups.length === 0)},
                true
        );

        // Update $error when the system name changes
        $scope.$watch(
                function() {return self.editModel.systemName},
                function() {
                    self.$error.duplicateUser = (angular.isString(self.editModel.systemName) && self.userMap[self.editModel.systemName]);
                    self.$error.missingUser = (!angular.isString(self.editModel.systemName) || self.editModel.systemName.length === 0);
                }
        );

        /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
         */
        self.canDelete = function() {
            return (self.model.systemName !== null);
        };

        /**
         * Finds the substring of the title for the specified group that matches the query term.
         *
         * @param group the group
         * @returns {string} the group title substring
         */
        self.findGroupSearchText = function(group) {
            var safeQuery = self.groupSearchText.toLocaleUpperCase();
            if (angular.isString(self.groupMap[group].title)) {
                var titleIndex = self.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery);
                return (titleIndex > -1) ? self.groupMap[group].title.substr(titleIndex, safeQuery.length) : self.groupSearchText;
            } else {
                var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                return (nameIndex > -1) ? group.substr(nameIndex, safeQuery.length) : self.groupSearchText;
            }
        };

        /**
         * Gets the title for the specified group.
         *
         * @param group the group
         * @returns {string} the group title
         */
        self.getGroupTitle = function(group) {
            if (angular.isDefined(self.groupMap[group]) && angular.isString(self.groupMap[group].title)) {
                return self.groupMap[group].title;
            } else {
                return group;
            }
        };

        /**
         * Gets the titles for every group this user belongs to.
         *
         * @returns {Array.<string>} the group titles for this user
         */
        self.getGroupTitles = function() {
            return _.map(self.model.groups, self.getGroupTitle);
        };

        /**
         * Cancels the current edit operation. If a new user is being created then redirects to the users page.
         */
        self.onCancel = function() {
            if (self.model.systemName === null) {
                StateService.Auth().navigateToUsers();
            }
        };

        /**
         * Deletes the current user.
         */
        self.onDelete = function() {
            var name = (angular.isString(self.model.displayName) && self.model.displayName.length > 0) ? self.model.displayName : self.model.systemName;
            UserService.deleteUser(encodeURIComponent(self.model.systemName))
                    .then(function() {
                        $mdToast.show(
                                $mdToast.simple()
                                        .textContent("Successfully deleted the user " + name)
                                        .hideDelay(3000)
                        );
                        StateService.Auth().navigateToUsers();
                    }, function() {
                        $mdDialog.show(
                                $mdDialog.alert()
                                        .clickOutsideToClose(true)
                                        .title("Delete Failed")
                                        .textContent("The user " + name + " could not be deleted. " + err.data.message)
                                        .ariaLabel("Failed to delete user")
                                        .ok("Got it!")
                        );
                    });
        };

        /**
         * Creates a copy of the user model for editing.
         */
        self.onEdit = function() {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Loads the user details.
         */
        self.onLoad = function() {
            // Get the list of groups
            UserService.getGroups()
                    .then(function(groups) {
                        self.groupList = [];
                        self.groupMap = {};
                        angular.forEach(groups, function(group) {
                            self.groupList.push(group.systemName);
                            self.groupMap[group.systemName] = group;
                        });
                    });

            // Load allowed permissions
            AccessControlService.getUserAllowedActions()
                    .then(function(actionSet) {
                        self.allowAdmin = AccessControlService.hasAction(AccessControlService.USERS_ADMIN, actionSet.actions);
                    });

            // Load the user details
            if (angular.isString($transition$.params().userId)) {
                UserService.getUser($transition$.params().userId)
                        .then(function(user) {
                            self.model = user;
                            self.loading = false;
                        });
            } else {
                self.onEdit();
                self.isEditable = true;
                self.loading = false;

                UserService.getUsers()
                        .then(function(users) {
                            self.userMap = {};
                            angular.forEach(users, function(user) {
                                self.userMap[user.systemName] = true;
                            });
                        });
            }
        };

        /**
         * Saves the current user.
         */
        self.onSave = function() {
            var model = angular.copy(self.editModel);
            UserService.saveUser(model)
                    .then(function() {
                        self.model = model;
                    });
        };

        /**
         * Filters the list of groups to those matching the specified query.
         *
         * @param {string} query the query string
         * @returns {Array.<string>} the list of matching groups
         */
        self.queryGroups = function(query) {
            var safeQuery = query.toLocaleUpperCase();
            return self.groupList
                    // Filter groups that are already selected
                    .filter(function(group) {
                        return (self.editModel.groups.indexOf(group) === -1);
                    })
                    // Find position of query term
                    .map(function(group) {
                        var nameIndex = group.toLocaleUpperCase().indexOf(safeQuery);
                        var titleIndex = angular.isString(self.groupMap[group].title) ? self.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery) : -1;
                        var index = (titleIndex > -1 && (nameIndex === -1 || nameIndex > titleIndex)) ? titleIndex : nameIndex;
                        return {name: group, index: index};
                    })
                    // Filter groups without query term
                    .filter(function(item) {
                        return item.index > -1;
                    })
                    // Sort based on position of query term
                    .sort(function(a, b) {
                        return a.index - b.index;
                    })
                    // Map back to just the name
                    .map(function(item) {
                        return item.name;
                    });
        };

        // Load the user details
        self.onLoad();
    }

    angular.module(moduleName).controller('UserDetailsController', ["$scope","$mdDialog","$mdToast","$transition$","AccessControlService","UserService","StateService",UserDetailsController]);
});

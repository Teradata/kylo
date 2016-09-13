(function() {
    /**
     * Adds or updates user details.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdDialog the dialog service
     * @param $mdToast the toast notification service
     * @param $stateParams the URL parameters
     * @param UserService the user service
     * @param StateService manages system state
     */
    function UserDetailsController($scope, $mdDialog, $mdToast, $stateParams, UserService, StateService) {
        var self = this;

        /**
         * Error message object that maps keys to a boolean indicating the error state.
         * @type {Object.<string, boolean>}
         */
        self.$error = {missingGroup: false};

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

        /**
         * Indicates if the user can be deleted. The main requirement is that the user exists.
         *
         * @returns {boolean} {@code true} if the user can be deleted, or {@code false} otherwise
         */
        self.canDelete = function() {
            return (self.model.systemName !== null);
        };

        /**
         * Gets the title for the specified group.
         *
         * @param group the group
         * @returns {string} the group title
         */
        self.getGroupTitle = function(group) {
            if (angular.isDefined(self.groupMap[group]) && angular.isDefined(self.groupMap[group].title)) {
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
                StateService.navigateToUsers();
            }
        };

        /**
         * Deletes the current user.
         */
        self.onDelete = function() {
            var name = (angular.isString(self.model.displayName) && self.model.displayName.length > 0) ? self.model.displayName : self.model.systemName;
            UserService.deleteUser(self.model.systemName)
                    .then(function() {
                        $mdToast.show(
                                $mdToast.simple()
                                        .textContent("Successfully deleted the user " + name)
                                        .hideDelay(3000)
                        );
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

            // Load the user details
            if (angular.isString($stateParams.userId)) {
                UserService.getUser($stateParams.userId)
                        .then(function(user) {
                            self.model = user;
                            self.loading = false;
                        });
            } else {
                self.onEdit();
                self.isEditable = true;
                self.loading = false;
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
            return self.groupList.filter(function(group) {
                if (self.editModel.groups.indexOf(group) !== -1) {
                    return false;
                }

                return (group.toLocaleUpperCase().indexOf(safeQuery) > -1 || (angular.isString(self.groupMap[group].title) && self.groupMap[group].title.toLocaleUpperCase().indexOf(safeQuery) > -1));
            });
        };

        // Load the user details
        self.onLoad();
    }

    angular.module(MODULE_FEED_MGR).controller('UserDetailsController', UserDetailsController);
}());

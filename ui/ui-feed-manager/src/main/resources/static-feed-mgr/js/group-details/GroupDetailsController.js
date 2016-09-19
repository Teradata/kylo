(function() {
    /**
     * Adds or updates user details.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param $mdToast the toast notification service
     * @param {Object} $stateParams the URL parameters
     * @param {string} [$stateParams.groupId] the group name in the url
     * @param {AccessControlService} AccessControlService the access control service
     * @param {UserService} UserService the user service
     * @param StateService manages system state
     */
    function GroupDetailsController($scope, $mdDialog, $mdToast, $stateParams, AccessControlService, UserService, StateService) {
        var self = this;

        /**
         * List of actions allowed to the group.
         *
         * @type {Array.<Action>}
         */
        self.actions = [];

        /**
         * Editable list of actions allowed to the group.
         *
         * @type {Array.<Action>}
         */
        self.editActions = [];

        /**
         * Group model for the edit view.
         *
         * @type {GroupPrincipal}
         */
        self.editModel = {};

        /**
         * Indicates if the edit view is displayed.
         * @type {boolean}
         */
        self.isEditable = false;

        /**
         * Indicates if the permissions edit view is displayed.
         * @type {boolean}
         */
        self.isPermissionsEditable = false;

        /**
         * Indicates that the group is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Group model for the read-only view.
         * @type {GroupPrincipal}
         */
        self.model = {description: null, memberCount: 0, systemName: null, title: null};

        /**
         * Users in the group.
         * @type {Array.<UserPrincipal>}
         */
        self.users = [];

        /**
         * Indicates if the group can be deleted. The main requirement is that the group exists.
         *
         * @returns {boolean} {@code true} if the group can be deleted, or {@code false} otherwise
         */
        self.canDelete = function() {
            return (self.model.systemName !== null);
        };

        /**
         * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
         *
         * @param user the user
         * @returns {string} the display name
         */
        self.getUserName = function(user) {
            return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
        };

        /**
         * Cancels the current edit operation. If a new group is being created then redirects to the groups page.
         */
        self.onCancel = function() {
            if (self.model.systemName === null) {
                StateService.navigateToGroups();
            }
        };

        /**
         * Deletes the current group.
         */
        self.onDelete = function() {
            var name = (angular.isString(self.model.title) && self.model.title.length > 0) ? self.model.title : self.model.systemName;
            UserService.deleteGroup(self.model.systemName)
                    .then(function() {
                        $mdToast.show(
                                $mdToast.simple()
                                        .textContent("Successfully deleted the group " + name + ".")
                                        .hideDelay(3000)
                        );
                    }, function() {
                        $mdDialog.show(
                                $mdDialog.alert()
                                        .clickOutsideToClose(true)
                                        .title("Delete Failed")
                                        .textContent("The group '" + name + " could not be deleted. " + err.data.message)
                                        .ariaLabel("Failed to delete group")
                                        .ok("Got it!")
                        );
                    });
        };

        /**
         * Creates a copy of the group model for editing.
         */
        self.onEdit = function() {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Creates a copy of the permissions for editing.
         */
        self.onEditPermissions = function() {
            self.editActions = angular.copy(self.actions);
        };

        /**
         * Loads the group details.
         */
        self.onLoad = function() {
            if (angular.isString($stateParams.groupId)) {
                UserService.getGroup($stateParams.groupId)
                        .then(function(group) {
                            self.model = group;
                            self.loading = false;
                        });
                UserService.getUsersByGroup($stateParams.groupId)
                        .then(function(users) {
                            self.users = users;
                        });
                AccessControlService.getAllowedActions(null, null, $stateParams.groupId)
                        .then(function(actionSet) {
                            self.actions = actionSet.actions;
                        });
            } else {
                self.onEdit();
                self.isEditable = true;
                self.loading = false;
            }
        };

        /**
         * Saves the current group.
         */
        self.onSave = function() {
            var model = angular.copy(self.editModel);
            UserService.saveGroup(model)
                    .then(function() {
                        self.model = model;
                    });
        };

        /**
         * Saves the current permissions.
         */
        self.onSavePermissions = function() {
            var actions = angular.copy(self.editActions);
            AccessControlService.setAllowedActions(null, null, self.model.systemName, actions)
                    .then(function(actionSet) {
                        self.actions = actionSet.actions;
                    });
        };

        /**
         * Navigates to the details page for the specified user.
         *
         * @param user the user
         */
        self.onUserClick = function(user) {
            StateService.navigateToUserDetails(user.systemName);
        };

        // Load the user details
        self.onLoad();
    }

    angular.module(MODULE_FEED_MGR).controller('GroupDetailsController', GroupDetailsController);
}());

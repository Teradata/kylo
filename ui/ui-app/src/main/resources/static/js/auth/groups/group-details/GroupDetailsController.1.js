define(['angular', "auth/module-name"], function (angular, moduleName) {
    /**
     * Adds or updates user details.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param $mdToast the toast notification service
     * @param {Object} $transition$ the URL parameters
     * @param {string} [$transition$.params().groupId] the group name in the url
     * @param {AccessControlService} AccessControlService the access control service
     * @param {UserService} UserService the user service
     * @param StateService manages system state
     */
    function GroupDetailsController($scope, $mdDialog, $mdToast, $transition$, AccessControlService, UserService, StateService) {
        var self = this;

        /**
         * Error message object that maps keys to a boolean indicating the error state.
         * @type {{duplicateName: boolean, missingName: boolean}}
         */
        self.$error = {duplicateName: false, missingName: false};

        /**
         * List of actions allowed to the group.
         *
         * @type {Array.<Action>}
         */
        self.actions = [];

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        /**
         * Indicates that user operations are allowed.
         * @type {boolean}
         */
        self.allowUsers = false;

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
         * Lookup map for detecting duplicate group names.
         * @type {Object.<string, boolean>}
         */
        self.groupMap = {};

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
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

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

        self.groupId = $transition$.params().groupId;

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

        // Update $error when the system name changes
        $scope.$watch(
                function() {return self.editModel.systemName},
                function() {
                    self.$error.duplicateName = (angular.isString(self.editModel.systemName) && self.groupMap[self.editModel.systemName]);
                    self.$error.missingName = (!angular.isString(self.editModel.systemName) || self.editModel.systemName.length === 0);
                }
        );

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
                StateService.Auth().navigateToGroups();
            }
        };

        /**
         * Deletes the current group.
         */
        self.onDelete = function() {
            var name = (angular.isString(self.model.title) && self.model.title.length > 0) ? self.model.title : self.model.systemName;
            UserService.deleteGroup(encodeURIComponent($transition$.params().groupId))
                    .then(function() {
                        $mdToast.show(
                                $mdToast.simple()
                                        .textContent("Successfully deleted the group " + name + ".")
                                        .hideDelay(3000)
                        );
                        StateService.Auth().navigateToGroups();
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
            // Fetch allowed permissions
            AccessControlService.getUserAllowedActions()
                    .then(function(actionSet) {
                        self.allowAdmin = AccessControlService.hasAction(AccessControlService.GROUP_ADMIN, actionSet.actions);
                        self.allowUsers = AccessControlService.hasAction(AccessControlService.USERS_ACCESS, actionSet.actions);
                    });

            // Fetch group details
            if (angular.isString($transition$.params().groupId)) {
                UserService.getGroup($transition$.params().groupId)
                        .then(function(group) {
                            self.model = group;
                            self.loading = false;
                        });
                UserService.getUsersByGroup($transition$.params().groupId)
                        .then(function(users) {
                            self.users = users;
                        });
                AccessControlService.getAllowedActions(null, null, $transition$.params().groupId)
                        .then(function(actionSet) {
                            self.actions = actionSet.actions;
                        });
            } else {
                self.onEdit();
                self.isEditable = true;
                self.loading = false;

                UserService.getGroups()
                        .then(function(groups) {
                            self.groupMap = {};
                            angular.forEach(groups, function(group) {
                                self.groupMap[group.systemName] = true;
                            });
                        });
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
                        self.groupId = self.model.systemName;
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
            StateService.Auth().navigateToUserDetails(user.systemName);
        };

        // Load the user details
        self.onLoad();
    }

    angular.module(moduleName).controller('GroupDetailsController', ["$scope","$mdDialog","$mdToast","$transition$","AccessControlService","UserService","StateService",GroupDetailsController]);
});

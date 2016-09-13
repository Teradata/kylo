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
    function GroupDetailsController($scope, $mdDialog, $mdToast, $stateParams, UserService, StateService) {
        var self = this;

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
         * Indicates that the group is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Group model for the read-only view.
         * @type {GroupPrincipal}
         */
        self.model = {description: null, systemName: null, title: null};

        /**
         * Indicates if the group can be deleted. The main requirement is that the group exists.
         *
         * @returns {boolean} {@code true} if the group can be deleted, or {@code false} otherwise
         */
        self.canDelete = function() {
            return (self.model.systemName !== null);
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
         * Loads the group details.
         */
        self.onLoad = function() {
            if (angular.isString($stateParams.groupId)) {
                UserService.getGroup($stateParams.groupId)
                        .then(function(group) {
                            self.model = group;
                            self.loading = false;
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

        // Load the user details
        self.onLoad();
    }

    angular.module(MODULE_FEED_MGR).controller('GroupDetailsController', GroupDetailsController);
}());

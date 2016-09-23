(function() {
    /**
     * Displays the home page.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService the state service
     */
    function HomeController($scope, $mdDialog, AccessControlService, StateService) {
        var self = this;

        /**
         * Indicates that the page is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Determines the home page based on the specified allowed actions.
         *
         * @param actions the allowed actions
         */
        self.onLoad = function(actions) {
            // Determine if Feed Manager is allowed at all
            if (!AccessControlService.hasAction(AccessControlService.FEED_MANAGER_ACCESS, actions) && !AccessControlService.hasAction(AccessControlService.USERS_GROUPS_ACCESS, actions)) {
                self.loading = false;
                $mdDialog.show(
                        $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("Access Denied")
                                .textContent("You do not have access to the Feed Manager.")
                                .ariaLabel("Access denied to feed manager")
                                .ok("OK")
                );
                return;
            }

            // Determine the home page
            if (AccessControlService.hasAction(AccessControlService.FEEDS_ACCESS, actions)) {
                return StateService.navigateToFeeds();
            }
            if (AccessControlService.hasAction(AccessControlService.CATEGORIES_ACCESS, actions)) {
                return StateService.navigateToCategories();
            }
            if (AccessControlService.hasAction(AccessControlService.TEMPLATES_ACCESS, actions)) {
                return StateService.navigateToRegisteredTemplates();
            }
            if (AccessControlService.hasAction(AccessControlService.USERS_ACCESS, actions)) {
                return StateService.navigateToUsers();
            }
            if (AccessControlService.hasAction(AccessControlService.GROUP_ACCESS, actions)) {
                return StateService.navigateToGroups();
            }

            // Otherwise, let the user pick
            self.loading = false;
        };

        // Fetch the list of allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.onLoad(actionSet.actions);
                });
    }

    angular.module(MODULE_FEED_MGR).controller('HomeController', HomeController);
}());

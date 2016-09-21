(function() {
    /**
     * Displays the Overview page.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param {AccessControlService} AccessControlService the access control service
     * @param HttpService
     */
    function OverviewController($scope, $mdDialog, AccessControlService, HttpService) {
        var self = this;

        /**
         * Indicates that the user is allowed to access the Operations Manager.
         * @type {boolean}
         */
        self.allowed = false;

        /**
         * Indicates that the page is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        // Stop polling on destroy
        $scope.$on("$destroy", function() {
            HttpService.cancelPendingHttpRequests();
        });

        // Fetch allowed permissions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.OPERATIONS_MANAGER_ACCESS, actionSet.actions)) {
                        self.allowed = true;
                    } else {
                        $mdDialog.show(
                                $mdDialog.alert()
                                        .clickOutsideToClose(true)
                                        .title("Access Denied")
                                        .textContent("You do not have access to the Operations Manager.")
                                        .ariaLabel("Access denied to operations manager")
                                        .ok("OK")
                        );
                    }
                    self.loading = false;
                });
    }

    angular.module(MODULE_OPERATIONS).controller("OverviewController", OverviewController);
}());

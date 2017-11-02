define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {
    /**
     * Displays the Overview page.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param {AccessControlService} AccessControlService the access control service
     * @param HttpService
     */
    function OverviewController($scope, $mdDialog,$interval, AccessControlService, HttpService,OpsManagerDashboardService) {
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

        /**
         * Refresh interval for the Services, Feed Health, Data Confidence, and Alerts   (Job Activity KPI is not using this value.  it is set to every second)
         * @type {number}
         */
        self.refreshInterval = 5000;

        /**
         * Refresh interval object for the dashboard
         * @type {null}
         */
        var interval = null;



        // Stop polling on destroy
        $scope.$on("$destroy", function() {
            HttpService.cancelPendingHttpRequests();
            if(interval != null){
                $interval.cancel(interval);
                interval = null;
            }

        });

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
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

        function init(){
            OpsManagerDashboardService.fetchDashboard();

            interval = $interval(OpsManagerDashboardService.fetchDashboard,self.refreshInterval);

        }

        init();

    }

    angular.module(moduleName).controller("OverviewController", ["$scope","$mdDialog","$interval","AccessControlService","HttpService","OpsManagerDashboardService",OverviewController]);
});

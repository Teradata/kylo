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
    function OverviewController($scope, $mdDialog,$interval,$timeout, AccessControlService, HttpService,OpsManagerDashboardService) {
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

        /**
         * The millis allowed for the refresh interval vs the actual data timestamp to be off.
         * You want the dashboard to query as close to the data time as possible to provide a realtime  feel for the page refreshing
         * @type {number}
         */
        this.DIFF_TIME_ALLOWED = 2000;

        /**
         * The max times a successful refresh has been done.
         * After reaching this number it will recycle back to 0
         * @type {number}
         */
        this.MAX_REFRESH_COUNTER = 30;

        /**
         * Track each Refresh attempt
         * @type {number}
         */
        this.refreshCounter = 0;

        /**
         * Track the number of times we need to reset/align the refresh interval closer to the data
         * @type {number}
         */
        this.resetRefreshCounter = 0;

        /**
         * The max times during a given set of MAX_REFRESH_COUNTER attempts
         * @type {number}
         */
        this.MAX_RESET_REFRESH_INTERVALS = 4;

        this.startRefreshTime = null;

        /**
         * Attempt to align the data time with refresh interval to provide better user realtime refresh
         * Experimental
         */
        function checkAndAlignDataWithRefreshInterval(){
            var dataTime = response.data.time;
            var diff = Math.abs(dataTime - start);

            //if we are off by more than 2 seconds and havent reset our interval for at least 4 times then reset the interval
          //  console.log('time off ', diff)
            if (diff > self.DIFF_TIME_ALLOWED && self.resetRefreshCounter < self.MAX_RESET_REFRESH_INTERVALS) {
                var nextTime = dataTime;
                var checkDate = new Date().getTime() + 1000;
                while (nextTime <= checkDate) {
                    nextTime += 5000;
                }
                var waitTime = Math.abs(nextTime - new Date().getTime());
                self.resetRefreshCounter++;

                //reset the refresh interval to be closer to the data time to
              //  console.log('WAITING ', waitTime, 'to sync the refresh interval to be closer to the data  on ', nextTime, ' diff is ', diff)
                if (interval != null) {
                    $interval.cancel(interval);
                    $timeout(function () {
                        setDashboardRefreshInterval();
                    }, waitTime)
                }
            }
            else {
                self.refreshCounter++;
                //if we have > 10 good retries, reset the interval check back to 0;
                if (self.refreshCounter > self.MAX_REFRESH_COUNTER) {
                    self.resetRefreshCounter = 0;
                }
            }
        }


        function setDashboardRefreshInterval() {
            interval = $interval(function () {
                var start = new Date().getTime();
                if (!OpsManagerDashboardService.isFetchingDashboard()) {
                    //only fetch if we are not fetching
                    self.startRefreshTime = new Date().getTime();
                    OpsManagerDashboardService.fetchDashboard().then(function (response) {
                        //checkAndAlignDataWithRefreshInterval();
                    });
                }
            }, self.refreshInterval);
        }

        function init(){
            OpsManagerDashboardService.fetchDashboard();
            setDashboardRefreshInterval();


        }



        init();

    }

    angular.module(moduleName).controller("OverviewController", ["$scope","$mdDialog","$interval","$timeout","AccessControlService","HttpService","OpsManagerDashboardService",OverviewController]);
});

define(["require", "exports", "angular", "./module-name", "../services/OpsManagerDashboardService"], function (require, exports, angular, module_name_1, OpsManagerDashboardService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var OverviewController = /** @class */ (function () {
        function OverviewController($scope, $mdDialog, $interval, $timeout, AccessControlService, HttpService, OpsManagerDashboardService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.AccessControlService = AccessControlService;
            this.HttpService = HttpService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            /**
             * Attempt to align the data time with refresh interval to provide better user realtime refresh
             * Experimental
             */
            this.checkAndAlignDataWithRefreshInterval = function () {
                var dataTime = _this.response.data.time;
                var diff = Math.abs(dataTime - _this.start);
                //if we are off by more than 2 seconds and havent reset our interval for at least 4 times then reset the interval
                //  console.log('time off ', diff)
                if (diff > _this.DIFF_TIME_ALLOWED && _this.resetRefreshCounter < _this.MAX_RESET_REFRESH_INTERVALS) {
                    var nextTime = dataTime;
                    var checkDate = new Date().getTime() + 1000;
                    while (nextTime <= checkDate) {
                        nextTime += 5000;
                    }
                    var waitTime = Math.abs(nextTime - new Date().getTime());
                    _this.resetRefreshCounter++;
                    //reset the refresh interval to be closer to the data time to
                    //  console.log('WAITING ', waitTime, 'to sync the refresh interval to be closer to the data  on ', nextTime, ' diff is ', diff)
                    if (_this.interval != null) {
                        _this.$interval.cancel(_this.interval);
                        _this.$timeout(function () {
                            _this.setDashboardRefreshInterval();
                        }, waitTime);
                    }
                }
                else {
                    _this.refreshCounter++;
                    //if we have > 10 good retries, reset the interval check back to 0;
                    if (_this.refreshCounter > _this.MAX_REFRESH_COUNTER) {
                        _this.resetRefreshCounter = 0;
                    }
                }
            };
            this.setDashboardRefreshInterval = function () {
                var _this = this;
                this.interval = this.$interval(function () {
                    var start = new Date().getTime();
                    if (!_this.OpsManagerDashboardService.isFetchingDashboard()) {
                        //only fetch if we are not fetching
                        _this.startRefreshTime = new Date().getTime();
                        _this.OpsManagerDashboardService.fetchDashboard().then(function (response) {
                            //checkAndAlignDataWithRefreshInterval();
                        });
                    }
                }, this.refreshInterval);
            };
            this.init = function () {
                this.OpsManagerDashboardService.fetchDashboard();
                this.setDashboardRefreshInterval();
            };
            /**
             * Indicates that the user is allowed to access the Operations Manager.
             * @type {boolean}
             */
            this.allowed = false;
            /**
             * Indicates that the page is currently being loaded.
             * @type {boolean}
             */
            this.loading = true;
            /**
             * Refresh interval for the Services, Feed Health, Data Confidence, and Alerts   (Job Activity KPI is not using this value.  it is set to every second)
             * @type {number}
             */
            this.refreshInterval = 5000;
            this.interval = null;
            // Stop polling on destroy
            $scope.$on("$destroy", function () {
                HttpService.cancelPendingHttpRequests();
                if (_this.interval != null) {
                    $interval.cancel(_this.interval);
                    _this.interval = null;
                }
            });
            // Fetch allowed permissions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (AccessControlService.hasAction(AccessControlService.OPERATIONS_MANAGER_ACCESS, actionSet.actions)) {
                    _this.allowed = true;
                }
                else {
                    $mdDialog.show($mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to the Operations Manager.")
                        .ariaLabel("Access denied to operations manager")
                        .ok("OK"));
                }
                _this.loading = false;
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
            this.init();
        } // end of constructor
        return OverviewController;
    }());
    exports.default = OverviewController;
    angular.module(module_name_1.moduleName)
        .service('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', 'BroadcastService', 'OpsManagerFeedService', OpsManagerDashboardService_1.default])
        .controller("OverviewController", ["$scope", "$mdDialog", "$interval", "$timeout",
        "AccessControlService", "HttpService", "OpsManagerDashboardService", OverviewController]);
});
//# sourceMappingURL=OverviewController.js.map
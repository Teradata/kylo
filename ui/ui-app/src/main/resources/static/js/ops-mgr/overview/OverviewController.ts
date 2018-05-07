import * as angular from "angular";
import {moduleName} from "./module-name";
import OpsManagerDashboardService from "../services/OpsManagerDashboardService";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";

export default class OverviewController implements ng.IComponentController{
allowed: boolean;
loading: boolean;
refreshInterval: number;    
/**
     * Refresh interval object for the dashboard
     * @type {null}
     */
interval: any;
DIFF_TIME_ALLOWED: number;
MAX_REFRESH_COUNTER: number;
refreshCounter: number;
resetRefreshCounter: number;
MAX_RESET_REFRESH_INTERVALS: number;
startRefreshTime: any;
response: any;
start: any;
constructor(private $scope: any,
        private $mdDialog: any,
        private $interval: any,
        private $timeout: any,
        private AccessControlService: any,
        private HttpService: any,
        private OpsManagerDashboardService: any){
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
            this.interval= null;

            // Stop polling on destroy
            $scope.$on("$destroy", ()=> {
                HttpService.cancelPendingHttpRequests();
                if(this.interval != null){
                    $interval.cancel(this.interval);
                    this.interval = null;
                }

            });

            // Fetch allowed permissions
            AccessControlService.getUserAllowedActions()
                    .then((actionSet: any)=> {
                        if (AccessControlService.hasAction(AccessControlService.OPERATIONS_MANAGER_ACCESS, actionSet.actions)) {
                            this.allowed = true;
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
                this.loading = false;
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
        }// end of constructor

    /**
     * Attempt to align the data time with refresh interval to provide better user realtime refresh
     * Experimental
     */
    checkAndAlignDataWithRefreshInterval= ()=>{
        var dataTime = this.response.data.time;
        var diff = Math.abs(dataTime - this.start);

        //if we are off by more than 2 seconds and havent reset our interval for at least 4 times then reset the interval
        //  console.log('time off ', diff)
        if (diff > this.DIFF_TIME_ALLOWED && this.resetRefreshCounter < this.MAX_RESET_REFRESH_INTERVALS) {
            var nextTime = dataTime;
            var checkDate = new Date().getTime() + 1000;
            while (nextTime <= checkDate) {
                nextTime += 5000;
            }
            var waitTime = Math.abs(nextTime - new Date().getTime());
            this.resetRefreshCounter++;

            //reset the refresh interval to be closer to the data time to
            //  console.log('WAITING ', waitTime, 'to sync the refresh interval to be closer to the data  on ', nextTime, ' diff is ', diff)
            if (this.interval != null) {
                this.$interval.cancel(this.interval);
                this.$timeout( ()=> {
                    this.setDashboardRefreshInterval();
                }, waitTime)
            }
        }
        else {
            this.refreshCounter++;
            //if we have > 10 good retries, reset the interval check back to 0;
            if (this.refreshCounter > this.MAX_REFRESH_COUNTER) {
                this.resetRefreshCounter = 0;
            }
        }
    }


    setDashboardRefreshInterval=()=> {
        this.interval = this.$interval( ()=> {
            var start = new Date().getTime();
            if (!this.OpsManagerDashboardService.isFetchingDashboard()) {
                //only fetch if we are not fetching
                this.startRefreshTime = new Date().getTime();
                this.OpsManagerDashboardService.fetchDashboard().then((response: any)=> {
                    //checkAndAlignDataWithRefreshInterval();
                });
            }
        }, this.refreshInterval);
    }

     init=()=>{
        this.OpsManagerDashboardService.fetchDashboard();
        this.setDashboardRefreshInterval();


    }


}

angular.module(moduleName)
.controller("OverviewController", 
            ["$scope","$mdDialog","$interval","$timeout",
            "AccessControlService","HttpService","OpsManagerDashboardService",OverviewController]);

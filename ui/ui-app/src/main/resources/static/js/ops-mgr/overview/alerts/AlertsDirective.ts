import * as angular from "angular";
import {moduleName} from "../module-name";
import {StateService} from "../../../services/StateService";
import AlertsServiceV2 from "../../services/AlertsServiceV2";

export default class controller implements ng.IComponentController{
alerts: any[];
feedRefresh: any;
refreshIntervalTime: any;
feedName: any;

static readonly $inject = ["$scope","$element","$interval","AlertsServiceV2","StateService","OpsManagerDashboardService","BroadcastService"];

$onInit() {
    this.ngOnInit();
}

ngOnInit() {
    //  this.alertsService = AlertsServiceV2;
    this.alerts = [];
    /**
    * Handle on the feed alerts refresh interval
    * @type {null}
    */
   this.feedRefresh = null;
   this.refreshIntervalTime = angular.isUndefined(this.refreshIntervalTime) ? 5000 : this.refreshIntervalTime;
   if(this.feedName == undefined || this.feedName == ''){
       this.watchDashboard();
   }
   else {
       this.alerts = [];
       this.stopFeedRefresh();
       this.fetchFeedAlerts();
       this.feedRefresh = this.$interval(this.fetchFeedAlerts,5000);
   }  
}
constructor(private $scope: IScope,
            private $element: JQuery,
            private $interval: angular.IIntervalService,
            private alertsServiceV2: AlertsServiceV2,
            private stateService: StateService,
            private OpsManagerDashboardService: any,
            private BroadcastService: any){
                          
                $scope.$on('$destroy',  ()=> {
                        this.stopFeedRefresh();
                });
         }

        watchDashboard=()=> {
            this.BroadcastService.subscribe(this.$scope,this.OpsManagerDashboardService.DASHBOARD_UPDATED,(dashboard: any)=>{
                var alerts = this.OpsManagerDashboardService.dashboard.alerts;
                this.alertsServiceV2.transformAlerts(alerts);
                this.alerts = alerts;
            });
        }

        fetchFeedAlerts=()=>{
            this.alertsServiceV2.fetchFeedAlerts(this.feedName).then((alerts: any)=> {
                this.alerts =alerts;
            });
        }

        stopFeedRefresh=()=>{
            if(this.feedRefresh != null){
                this.$interval.cancel(this.feedRefresh);
                this.feedRefresh = null;
            }
        }

        navigateToAlerts = (alertsSummary: any)=>{
            //generate Query
            var query = "UNHANDLED,"+ alertsSummary.type;
            if(alertsSummary.groupDisplayName != null && alertsSummary.groupDisplayName != null) {
                query += ","+alertsSummary.groupDisplayName;
            }
            else if(alertsSummary.subtype != null && alertsSummary.subtype != '') {
                query += ","+alertsSummary.subtype;
            }
            this.stateService.OpsManager().Alert().navigateToAlerts(query);
        }
}

 angular.module(moduleName).component('tbaAlerts',{
     controller: controller,
     bindings: {
        panelTitle: "@",
        feedName:'@',
        refreshIntervalTime:'=?'
     },
     controllerAs: "vm",
     templateUrl: "./alerts-template.html"
 });
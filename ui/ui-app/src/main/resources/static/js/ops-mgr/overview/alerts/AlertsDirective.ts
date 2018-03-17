import * as angular from "angular";
import {moduleName} from "../module-name";
import ServicesStatusData from "../../services/ServicesStatusService";
import AlertsServiceV2 from "../../services/AlertsServiceV2";
import OpsManagerDashboardService from "../../services/OpsManagerDashboardService";

export default class controller implements ng.IComponentController{
alertsService: any;
alerts: any[];
feedRefresh: any;
refreshIntervalTime: any;
feedName: any;
constructor(private $scope: any,
            private $element: any,
            private $interval: any,
            private AlertsServiceV2: AlertsServiceV2,
            private StateService: any,
            private OpsManagerDashboardService: any,
            private BroadcastService: any){
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
                this.feedRefresh = $interval(this.fetchFeedAlerts,5000);
                }   
            
                $scope.$on('$destroy',  ()=> {
                        this.stopFeedRefresh();
                });
         }

        watchDashboard=()=> {
            this.BroadcastService.subscribe(this.$scope,this.OpsManagerDashboardService.DASHBOARD_UPDATED,(dashboard: any)=>{
                var alerts = this.OpsManagerDashboardService.dashboard.alerts;
                this.AlertsServiceV2.transformAlerts(alerts);
                this.alerts = alerts;
            });
        }

        fetchFeedAlerts=()=>{
            this.AlertsServiceV2.fetchFeedAlerts(this.feedName).then((alerts: any)=> {
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
            this.StateService.OpsManager().Alert().navigateToAlerts(query);
        }
}

 angular.module(moduleName)
.service('AlertsServiceV2',["$q","$http","$interval","OpsManagerRestUrlService",AlertsServiceV2])
.service('OpsManagerDashboardService',['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService','BroadcastService','OpsManagerFeedService',OpsManagerDashboardService])
.controller('AlertsOverviewController', 
                ["$scope","$element","$interval","AlertsServiceV2","StateService","OpsManagerDashboardService",
                "BroadcastService",controller]);
 angular.module(moduleName)
        .directive('tbaAlerts', [()=> {
                            return {
                                restrict: "E",
                                scope: true,
                                bindToController: {
                                    panelTitle: "@",
                                    feedName:'@',
                                    refreshIntervalTime:'=?'
                                },
                                controllerAs: 'vm',
                                templateUrl: 'js/ops-mgr/overview/alerts/alerts-template.html',
                                controller: "AlertsOverviewController",
                                link: function ($scope, element, attrs) {
                                    $scope.$on('$destroy', function () {

                                    });
                                } //DOM manipulation\}
                            };
        }
             ]     );
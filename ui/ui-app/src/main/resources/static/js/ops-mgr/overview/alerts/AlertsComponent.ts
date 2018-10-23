import StateService from "../../../services/StateService";
import AlertsServiceV2 from "../../services/AlertsServiceV2";
import { Component, Input, Inject } from "@angular/core";
import BroadcastService from "../../../services/broadcast-service";
import OpsManagerDashboardService from "../../services/OpsManagerDashboardService";
import { ObjectUtils } from "../../../common/utils/object-utils";

@Component({
    selector: 'tba-alerts',
    templateUrl: 'js/ops-mgr/overview/alerts/alerts-template.html'
})
export default class AlertsComponent {

    @Input() panelTitle: string;
    @Input() refreshIntervalTime: number;
    @Input() feedName: string;

    alerts: any[] = [];
    feedRefresh: any;
    
    ngOnInit() {
        /**
        * Handle on the feed alerts refresh interval
        * @type {null}
        */
        this.feedRefresh = null;
        this.refreshIntervalTime = ObjectUtils.isUndefined(this.refreshIntervalTime) ? 5000 : this.refreshIntervalTime;
        if(this.feedName == undefined || this.feedName == ''){
            this.watchDashboard();
        }
        else {
            this.alerts = [];
            this.stopFeedRefresh();
            this.fetchFeedAlerts();
            this.feedRefresh = setInterval(this.fetchFeedAlerts,5000);
        }  
    }

    ngOnDestroy() {
        this.stopFeedRefresh();
    }

    constructor(private alertsServiceV2: AlertsServiceV2,
                private stateService: StateService,
                private OpsManagerDashboardService: OpsManagerDashboardService,
                @Inject("BroadcastService") private BroadcastService: BroadcastService) {}

        watchDashboard=()=> {
            this.BroadcastService.subscribe(null,this.OpsManagerDashboardService.DASHBOARD_UPDATED,(dashboard: any)=>{
                var alerts = this.OpsManagerDashboardService.dashboard.alerts;
                this.alertsServiceV2.transformAlertSummaryResponse(alerts);
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
                clearInterval(this.feedRefresh);
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

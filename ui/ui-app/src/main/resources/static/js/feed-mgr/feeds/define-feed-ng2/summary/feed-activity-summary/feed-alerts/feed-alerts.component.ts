import {HttpClient} from "@angular/common/http";
import {OperationsRestUrlConstants} from "../../../../../../services/operations-rest-url-constants";
import {Component, Inject, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService as KyoStateService} from "../../../../../../services/StateService";
import {Feed} from "../../../../../model/feed/feed.model";
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";
import {map} from "rxjs/operators/map";
import * as moment from "moment"


@Component({
    selector: 'feed-alerts',
    templateUrl: './feed-alerts.component.html'
})
export class FeedAlertsComponent implements OnInit, OnDestroy{

    @Input()
    public feed:Feed;

    constructor(private http:HttpClient, @Inject("StateService") private kyloStateService: KyoStateService){ }

    fetchInterval:any;

    kyloIcons:KyloIcons = KyloIcons;

    alerts:any[] = [];

    last = false;

    ngOnInit(){
        this.fetchFeedAlerts();
        this.fetchInterval = setInterval(this.fetchFeedAlerts.bind(this),10000);
    }
    ngOnDestroy() {
        if(this.fetchInterval){
            clearInterval(this.fetchInterval)
        }
    }

    navigateToAlerts(alertsSummary: any){
        //generate Query
        var query = "UNHANDLED,"+ alertsSummary.type;
        if(alertsSummary.groupDisplayName != null && alertsSummary.groupDisplayName != null) {
            query += ","+alertsSummary.groupDisplayName;
        }
        else if(alertsSummary.subtype != null && alertsSummary.subtype != '') {
            query += ","+alertsSummary.subtype;
        }
        this.kyloStateService.OpsManager().Alert().navigateToAlerts(query);
    }

    private transformAlertSummaryResponse(alertSummaries: any[]){
        if(alertSummaries) {
            alertSummaries.forEach((summary: any) => {
                summary.since = moment(summary.lastAlertTimestamp).fromNow();
            });
        }
        return alertSummaries;
    }


    private fetchFeedAlerts() {
        if(this.feed) {
            return this.http.get(OperationsRestUrlConstants.FEED_ALERTS_URL(this.feed.getFullName()))
                .pipe(map((response: any) => this.transformAlertSummaryResponse(response))
                ).subscribe((alerts: any[]) => {
                    this.alerts = alerts
                });
        }
    }

}
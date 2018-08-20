import * as _ from 'underscore';
import * as moment from "moment";
import { HttpClient, HttpParams } from '@angular/common/http';
import { ObjectUtils } from '../../common/utils/object-utils';

export default class AlertsServiceV2 {

    constructor(
        private OpsManagerRestUrlService: any,
        private http: HttpClient
    ) {

    };
    transformAlertSummaryResponse(alertSummaries: any) {
        _.each(alertSummaries, (summary: any) => {
            summary.since = moment(summary.lastAlertTimestamp).fromNow();

        });
    }
    fetchFeedAlerts(feedName: any, feedId?: any) {
        let params = new HttpParams();
        // Begin assigning parameters
        if (ObjectUtils.isDefined(feedId) && feedId != null) {
            params = params.append("feedId", feedId);
        }
        return new Promise((resolve, reject) => {
            this.http.get(this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName),
                { params: params }).toPromise().then((response: any) => {
                    this.transformAlertSummaryResponse(response)
                    resolve(response);
                }, (err: any) => {
                    reject(err)
                });
        })
    }
}
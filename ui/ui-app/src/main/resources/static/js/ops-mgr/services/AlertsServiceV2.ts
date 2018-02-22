import * as angular from "angular";
import {moduleName} from "../module-name";
import module from "../module";
import * as _ from 'underscore';
import * as moment from "moment";
import OpsManagerRestUrlService from "./OpsManagerRestUrlService";

export default class AlertsServiceV2{
    constructor(private $q: any,
            private $http: any,
            private $interval:any,
            private OpsManagerRestUrlService: any
    ){  
            /**
         * Flag to indicate the alerts have been loaded at least once
         * @type {boolean}
         */
        let loadedGlobalAlertsSummary = false;

        let alertSummaryRefreshTimeMillis = 5000;
        /**
         * ref to the refresh interval so we can cancel it
         * @type {null}
         */
        let alertsSummaryIntervalObject = null;
      }
  
alertSummData: any[];  
transformAlertSummaryResponse = (alertSummaries: any)=>{
    _.each(alertSummaries,function(summary: any){
        summary.since =  moment(summary.lastAlertTimestamp).fromNow();

    });

      let data =
        {
            alertsSummary:{
                lastRefreshTime:'',
                data:this.alertSummData
            },
            transformAlerts:this.transformAlertSummaryResponse,
            fetchFeedAlerts:(feedName: any, feedId: any)=> {
                var deferred = this.$q.defer();
                this.$http.get(this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName),
                          {params:{"feedId":feedId}}).then( (response: any)=> {
                                    this.transformAlertSummaryResponse(response.data)
                                    deferred.resolve(response.data);
                            }, (err: any)=>{
                                    deferred.reject(err)
                                });
                return deferred.promise;
            },

        };
        return data;
    }
      
}

angular.module(moduleName)
.service("OpsManagerRestUrlService",[OpsManagerRestUrlService])
.factory('AlertsService',["$q","$http","$interval","OpsManagerRestUrlService",AlertsServiceV2]);
   
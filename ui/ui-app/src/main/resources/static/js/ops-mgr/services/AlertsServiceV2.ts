import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import * as moment from "moment";
import "../module";

export default class AlertsServiceV2{
    data: any;    
    fetchFeedAlerts: any;
    transformAlerts: any;
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
        let transformAlertSummaryResponse = (alertSummaries: any)=>{
            _.each(alertSummaries,(summary: any)=>{
            summary.since =  moment(summary.lastAlertTimestamp).fromNow();

            });
        }
        let alertSummData: any[];  
        this.data =
                {
                 alertsSummary:{
                        lastRefreshTime:'',
                        data:alertSummData
                        },
            transformAlerts: transformAlertSummaryResponse,
            fetchFeedAlerts: (feedName: any, feedId: any)=> {
                var deferred = this.$q.defer();
                this.$http.get(this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName),
                          {params:{"feedId":feedId}}).then( (response: any)=> {
                                    transformAlertSummaryResponse(response.data)
                                    deferred.resolve(response.data);
                            }, (err: any)=>{
                                    deferred.reject(err)
                                });
                return deferred.promise;
            }
        };        
        return this.data;
      }      
}

angular.module(moduleName)
.factory('AlertsServiceV2',["$q","$http","$interval","OpsManagerRestUrlService",AlertsServiceV2]);

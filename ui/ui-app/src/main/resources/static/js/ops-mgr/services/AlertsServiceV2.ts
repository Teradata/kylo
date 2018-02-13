import * as angular from "angular";
import {moduleName} from "../module-name";
import module from "../module";
import * as _ from 'underscore';
import * as moment from "moment";

export default class AlertsServiceV2{
    module: ng.IModule;
    constructor(private $q: any,
            private $http: any,
            private $interval:any,
            private OpsManagerRestUrlService: any
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('AlertsService',["$q","$http","$interval","OpsManagerRestUrlService",this.factoryFn.bind(this)]);
    }
    
      factoryFn() {
    /**
         * Flag to indicate the alerts have been loaded at least once
         * @type {boolean}
         */
        var loadedGlobalAlertsSummary = false;

        var alertSummaryRefreshTimeMillis = 5000;
        /**
         * ref to the refresh interval so we can cancel it
         * @type {null}
         */
        var alertsSummaryIntervalObject = null;

        var transformAlertSummaryResponse = function(alertSummaries: any){
            _.each(alertSummaries,function(summary: any){
                summary.since =  moment(summary.lastAlertTimestamp).fromNow();

            });
        }

        var alertSummData: any[];
        var data =
        {
            alertsSummary:{
                lastRefreshTime:'',
                data:alertSummData
            },
            transformAlerts:transformAlertSummaryResponse,
            fetchFeedAlerts:function(feedName: any, feedId: any) {
                var deferred = this.$q.defer();
                this.$http.get(this.OpsManagerRestUrlService.FEED_ALERTS_URL(feedName),
                          {params:{"feedId":feedId}}).then(function (response: any) {
                                    transformAlertSummaryResponse(response.data)
                                    deferred.resolve(response.data);
                            }, function(err: any){
                                    deferred.reject(err)
                                });
                return deferred.promise;
            },

        };
        return data;
      }
}
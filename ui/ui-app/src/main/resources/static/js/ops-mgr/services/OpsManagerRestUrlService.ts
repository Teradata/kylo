import * as angular from "angular";
import {moduleName} from "../module-name";
import "../module";

export default class OpsManagerRestUrlService{
    constructor(){}
    ROOT: string = "";
    FEED_MGR_BASE = "/proxy/v1/feedmgr";
    FEED_MGR_FEED_BASE_URL = this.FEED_MGR_BASE + "/feeds";
    SLA_BASE_URL = this.FEED_MGR_BASE + "/sla";
    FEEDS_BASE = "/proxy/v1/feeds";
    JOBS_BASE = "/proxy/v1/jobs";
    SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
    DASHBOARD_URL = this.ROOT + '/proxy/v1/dashboard';
    DASHBOARD_PAGEABLE_FEEDS_URL = this.ROOT + '/proxy/v1/dashboard/pageable-feeds';

    FEED_HEALTH_URL = this.FEEDS_BASE + "/health";
    FEED_NAMES_URL = this.FEEDS_BASE + "/names";
    FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL = this.FEED_MGR_FEED_BASE_URL + "/feed-system-name-to-display-name";
    FEED_HEALTH_COUNT_URL = this.FEEDS_BASE + "/health-count";

    /*this.SPECIFIC_FEED_HEALTH_COUNT_URL = function (feedName) {
        return this.FEED_HEALTH_COUNT_URL + '/' + feedName + '/';
    }
    */

    SPECIFIC_FEED_HEALTH_URL =  (feedName: any)=>{
        return '/proxy/v1/dashboard/feeds/feed-name/' + feedName;
    }
    FEED_DAILY_STATUS_COUNT_URL =  (feedName: any)=> {
        return this.FEEDS_BASE + "/" + feedName + "/daily-status-count";
    }

    FEED_NAME_FOR_ID = (feedId: any)=>{
            return this.FEEDS_BASE +"/query/"+feedId
        }
    //JOB urls

    JOBS_QUERY_URL = this.JOBS_BASE;
    JOBS_CHARTS_QUERY_URL = this.JOBS_BASE + '/list';
    JOB_NAMES_URL = this.JOBS_BASE + '/names';

    DAILY_STATUS_COUNT_URL = this.JOBS_BASE + "/daily-status-count/";

        //this.RUNNING_OR_FAILED_COUNTS_URL = this.JOBS_BASE + '/running-failed-counts';

    RUNNING_JOB_COUNTS_URL = '/proxy/v1/dashboard/running-jobs';

       // this.DATA_CONFIDENCE_URL = "/proxy/v1/data-confidence/summary";

     RESTART_JOB_URL =  (executionId: any)=>{
            return this.JOBS_BASE + "/" + executionId + "/restart";
        }
     STOP_JOB_URL =  (executionId: any)=> {
            return this.JOBS_BASE + "/" + executionId + "/stop";
        }

     ABANDON_JOB_URL =  (executionId: any) =>{
            return this.JOBS_BASE + "/" + executionId + "/abandon";
        }


     ABANDON_ALL_JOBS_URL =  (feedId: any) =>{
           return this.JOBS_BASE + "/abandon-all/" + feedId;
        }

     FAIL_JOB_URL =  (executionId: any)=> {
            return this.JOBS_BASE + "/" + executionId + "/fail";
        }

     LOAD_JOB_URL =  (executionId: any) =>{
            return this.JOBS_BASE + "/" + executionId;
        }

     RELATED_JOBS_URL =  (executionId: any) =>{
            return this.JOBS_BASE + "/" + executionId + "/related";
        }

//Service monitoring

     SERVICES_URL = "/proxy/v1/service-monitor/";

        //Provenance Event Stats
     STATS_BASE = "/proxy/v1/provenance-stats";

     STATS_BASE_V2 = "/proxy/v2/provenance-stats";

     PROCESSOR_DURATION_FOR_FEED =  (feedName: any, from: any, to: any)=> {
            return this.STATS_BASE_V2 + "/" + feedName + "/processor-duration?from=" + from + "&to=" + to;
        };

     FEED_STATISTICS_OVER_TIME =  (feedName: any, from: any, to: any, maxDataPoints: any)=> {
            return this.STATS_BASE_V2 + "/" + feedName + "?from=" + from + "&to=" + to + "&dp=" + maxDataPoints;
        };

     FEED_PROCESSOR_ERRORS =  (feedName: any, from: any, to: any)=> {
            return this.STATS_BASE_V2 + "/" + feedName + "/processor-errors?from=" + from + "&to=" + to;
        };

     PROVENANCE_EVENT_TIME_FRAME_OPTIONS = this.STATS_BASE_V2 + "/time-frame-options";

        /**
         * Gets the alert details endpoint for the specified alert.
         * @param {string} alertId the id of the alert
         * @returns {string} the URL of the endpoint
         */
     ALERT_DETAILS_URL =  (alertId: any) =>{
            return "/proxy/v1/alerts/" + alertId;
        };

     ALERTS_URL = "/proxy/v1/alerts";

     ALERTS_SUMMARY_UNHANDLED = "/proxy/v1/dashboard/alerts";

     ALERT_TYPES = "/proxy/v1/alerts/alert-types";

     FEED_ALERTS_URL = (feedName: any)=> {
            return "/proxy/v1/dashboard/alerts/feed-name/"+feedName;
        }

     //assessments
     LIST_SLA_ASSESSMENTS_URL = "/proxy/v1/sla/assessments/"

     GET_SLA_ASSESSMENT_URL = (assessmentId: any)=>{
            return "/proxy/v1/sla/assessments/"+assessmentId;
        };

     GET_SLA_BY_ID_URL =  (slaId: any)=> {
            return this.SLA_BASE_URL + "/"+slaId;
        }

        TRIGGER_SAVEPOINT_RETRY = function(executionId:any, flowfileId:any){
            return this.JOBS_BASE + "/" + executionId + "/savepoint/trigger-retry/"+flowfileId;
        }

        TRIGGER_SAVEPOINT_RELEASE= function(executionId:any, flowfileId:any){
            return this.JOBS_BASE + "/" + executionId + "/savepoint/trigger-release/"+flowfileId;
        }

}

  angular.module(moduleName).service('OpsManagerRestUrlService',[OpsManagerRestUrlService]);

import * as angular from "angular";
import {moduleName} from "../module-name";
import "../module";
import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";

export default class OpsManagerRestUrlService{
    constructor(){}
    ROOT: string = "";
    FEED_MGR_BASE = OperationsRestUrlConstants.FEED_MGR_BASE;
    FEED_MGR_FEED_BASE_URL = OperationsRestUrlConstants.FEED_MGR_FEED_BASE_URL
    SLA_BASE_URL = this.FEED_MGR_BASE + "/sla";
    FEEDS_BASE = OperationsRestUrlConstants.FEEDS_BASE
    JOBS_BASE = OperationsRestUrlConstants.JOBS_BASE
    SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
    DASHBOARD_URL = this.ROOT + '/proxy/v1/dashboard';
    DASHBOARD_PAGEABLE_FEEDS_URL = this.ROOT + '/proxy/v1/dashboard/pageable-feeds';

    FEED_HEALTH_URL = this.FEEDS_BASE + "/health";
    FEED_NAMES_URL = this.FEEDS_BASE + "/names";
    FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL = OperationsRestUrlConstants.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL;
    FEED_HEALTH_COUNT_URL = this.FEEDS_BASE + "/health-count";

    /*this.SPECIFIC_FEED_HEALTH_COUNT_URL = function (feedName) {
        return this.FEED_HEALTH_COUNT_URL + '/' + feedName + '/';
    }
    */

    SPECIFIC_FEED_HEALTH_URL = OperationsRestUrlConstants.SPECIFIC_FEED_HEALTH_URL;

    FEED_DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.FEED_DAILY_STATUS_COUNT_URL;

    FEED_NAME_FOR_ID = OperationsRestUrlConstants.FEED_NAME_FOR_ID;
    //JOB urls

    JOBS_QUERY_URL = OperationsRestUrlConstants.JOBS_BASE;
    JOBS_CHARTS_QUERY_URL = OperationsRestUrlConstants.JOBS_CHARTS_QUERY_URL
    JOB_NAMES_URL = OperationsRestUrlConstants.JOB_NAMES_URL

    DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.DAILY_STATUS_COUNT_URL;

        //this.RUNNING_OR_FAILED_COUNTS_URL = this.JOBS_BASE + '/running-failed-counts';

    RUNNING_JOB_COUNTS_URL = OperationsRestUrlConstants.RUNNING_JOB_COUNTS_URL;

       // this.DATA_CONFIDENCE_URL = "/proxy/v1/data-confidence/summary";

     RESTART_JOB_URL = OperationsRestUrlConstants.RESTART_JOB_URL;

     STOP_JOB_URL = OperationsRestUrlConstants.STOP_JOB_URL;

     ABANDON_JOB_URL =  OperationsRestUrlConstants.ABANDON_JOB_URL;


     ABANDON_ALL_JOBS_URL = OperationsRestUrlConstants.ABANDON_ALL_JOBS_URL;

     FAIL_JOB_URL = OperationsRestUrlConstants.FAIL_JOB_URL;

     LOAD_JOB_URL =  OperationsRestUrlConstants.LOAD_JOB_URL

     RELATED_JOBS_URL = OperationsRestUrlConstants.RELATED_JOBS_URL

//Service monitoring

     SERVICES_URL = "/proxy/v1/service-monitor/";

        //Provenance Event Stats
     STATS_BASE = OperationsRestUrlConstants.STATS_BASE

     STATS_BASE_V2 = OperationsRestUrlConstants.STATS_BASE_V2;

     PROCESSOR_DURATION_FOR_FEED =  OperationsRestUrlConstants.PROCESSOR_DURATION_FOR_FEED;

     FEED_STATISTICS_OVER_TIME =  OperationsRestUrlConstants.FEED_STATISTICS_OVER_TIME

     FEED_PROCESSOR_ERRORS =  OperationsRestUrlConstants.FEED_PROCESSOR_ERRORS

     PROVENANCE_EVENT_TIME_FRAME_OPTIONS = OperationsRestUrlConstants.PROVENANCE_EVENT_TIME_FRAME_OPTIONS

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


export class OperationsRestUrlConstants{

    static FEEDS_BASE = "/proxy/v1/feeds";

    static  JOBS_BASE = "/proxy/v1/jobs";

    static FEED_MGR_BASE = "/proxy/v1/feedmgr";

    static FEED_MGR_FEED_BASE_URL = OperationsRestUrlConstants.FEED_MGR_BASE + "/feeds";

    //Provenance Event Stats
   static STATS_BASE = "/proxy/v1/provenance-stats";

   static STATS_BASE_V2 = "/proxy/v2/provenance-stats";

    static SPECIFIC_FEED_HEALTH_URL =  (feedName: string)=>{
        return '/proxy/v1/dashboard/feeds/feed-name/' + feedName;
    }
    static  FEED_DAILY_STATUS_COUNT_URL =  (feedName: string)=> {
        return OperationsRestUrlConstants.FEEDS_BASE + "/" + feedName + "/daily-status-count";
    }

    static  FEED_NAME_FOR_ID = (feedId: string)=>{
        return OperationsRestUrlConstants.FEEDS_BASE +"/query/"+feedId
    }

    static FEED_HEALTH_URL = OperationsRestUrlConstants.FEEDS_BASE + "/health";

    static FEED_HEALTH_COUNT_URL = OperationsRestUrlConstants.FEEDS_BASE + "/health-count";

    static DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.JOBS_BASE + "/daily-status-count/";

    static FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL = OperationsRestUrlConstants.FEED_MGR_FEED_BASE_URL + "/feed-system-name-to-display-name";

    static FEED_ALERTS_URL = (feedName: any)=> {
        return "/proxy/v1/dashboard/alerts/feed-name/"+feedName;
    }


    static PROVENANCE_EVENT_TIME_FRAME_OPTIONS = OperationsRestUrlConstants.STATS_BASE_V2 + "/time-frame-options";

    static FEED_STATISTICS_OVER_TIME =  (feedName: string, from: number, to: number)=> {
        return OperationsRestUrlConstants.STATS_BASE_V2 + "/" + feedName + "?from=" + from + "&to=" + to;// + "&dp=" + maxDataPoints;
    };

    static  FEED_PROCESSOR_ERRORS =  (feedName: string, from: number, to: number,after?:number)=> {
        return OperationsRestUrlConstants.STATS_BASE_V2 + "/" + feedName + "/processor-errors?from=" + from + "&to=" + to+"&after="+after;
    };


    static PROCESSOR_DURATION_FOR_FEED =  (feedName: string, from: number, to: number)=> {
        return OperationsRestUrlConstants.STATS_BASE_V2 + "/" + feedName + "/processor-duration?from=" + from + "&to=" + to;
    };


    //Jobs

    static JOBS_QUERY_URL = OperationsRestUrlConstants.JOBS_BASE;
    static JOBS_CHARTS_QUERY_URL = OperationsRestUrlConstants.JOBS_BASE + '/list';
    static JOB_NAMES_URL = OperationsRestUrlConstants.JOBS_BASE + '/names';
    static RUNNING_JOB_COUNTS_URL = '/proxy/v1/dashboard/running-jobs';

    static RESTART_JOB_URL =  (executionId: any)=>{
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId + "/restart";
    }
    static STOP_JOB_URL =  (executionId: any)=> {
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId + "/stop";
    }

    static ABANDON_JOB_URL =  (executionId: any) =>{
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId + "/abandon";
    }


    static ABANDON_ALL_JOBS_URL =  (feedId: any) =>{
        return OperationsRestUrlConstants.JOBS_BASE + "/abandon-all/" + feedId;
    }

    static FAIL_JOB_URL =  (executionId: any)=> {
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId + "/fail";
    }

    static LOAD_JOB_URL =  (executionId: any) =>{
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId;
    }

    static RELATED_JOBS_URL =  (executionId: any) =>{
        return OperationsRestUrlConstants.JOBS_BASE + "/" + executionId + "/related";
    }
}
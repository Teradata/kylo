
export class OperationsRestUrlConstants{

    static FEEDS_BASE = "/proxy/v1/feeds";

    static  JOBS_BASE = "/proxy/v1/jobs";

    static SPECIFIC_FEED_HEALTH_URL =  (feedName: any)=>{
        return '/proxy/v1/dashboard/feeds/feed-name/' + feedName;
    }
    static  FEED_DAILY_STATUS_COUNT_URL =  (feedName: any)=> {
        return OperationsRestUrlConstants.FEEDS_BASE + "/" + feedName + "/daily-status-count";
    }

    static  FEED_NAME_FOR_ID = (feedId: any)=>{
        return OperationsRestUrlConstants.FEEDS_BASE +"/query/"+feedId
    }

    static FEED_HEALTH_URL = OperationsRestUrlConstants.FEEDS_BASE + "/health";

    static FEED_HEALTH_COUNT_URL = OperationsRestUrlConstants.FEEDS_BASE + "/health-count";

    static DAILY_STATUS_COUNT_URL = OperationsRestUrlConstants.JOBS_BASE + "/daily-status-count/";
}
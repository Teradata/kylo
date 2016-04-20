angular.module(MODULE_OPERATIONS).service('RestUrlService', function () {

    var self = this;

    this.ROOT = "";

    this.FEEDS_BASE = "/api/v1/feeds";
    this.JOBS_BASE = "/api/v1/jobs";
    this.FEED_HEALTH_URL = this.FEEDS_BASE+"/health";
    this.FEED_NAMES_URL = this.FEEDS_BASE+"/names";
    this.FEED_HEALTH_COUNT_URL = this.FEEDS_BASE+"/health-count";

    this.SPECIFIC_FEED_HEALTH_COUNT_URL = function(feedName) {
        return self.FEED_HEALTH_COUNT_URL+'/'+feedName+'/';
    }

    this.SPECIFIC_FEED_HEALTH_URL = function(feedName) {
        return self.FEED_HEALTH_URL+'/'+feedName+'/';
    }
    this.FEED_DAILY_STATUS_COUNT_URL = function(feedName,datePart,amount){
        return self.FEEDS_BASE+"/"+feedName+"/daily-status-count/"+datePart+"/"+amount;
    }




//JOB urls


    this.JOBS_QUERY_URL = this.JOBS_BASE;
    this.JOBS_CHARTS_QUERY_URL = this.JOBS_BASE+'/list';
    this.JOB_NAMES_URL = this.JOBS_BASE+'/names';
    this.JOB_PARAMETERS_URL = function(jobExecutionId) {
        return self.JOBS_BASE+'/'+jobExecutionId+'/parameters';
    }
    this.DAILY_STATUS_COUNT_URL = function(datePart,amount){
        return self.JOBS_BASE+"/daily-status-count/"+datePart+"/"+amount;
    }

    this.RUNNING_OR_FAILED_COUNTS_URL = this.JOBS_BASE+'/running-failed-counts';


    this.DATA_CONFIDENCE_URL = "/api/v1/data-confidence/summary";


    this.RESTART_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/restart";
    }
    this.STOP_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/stop";
    }

    this.ABANDON_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/abandon";
    }

    this.FAIL_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/fail";
    }


    this.LOAD_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId;
    }
    this.JOB_PROGRESS_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/steps";
    }

    this.RELATED_JOBS_URL = function(executionId) {
        return self.JOBS_BASE+"/"+executionId+"/related";
    }
    
//Service monitoring

this.SERVICES_URL = "/api/v1/service-monitor/";

});
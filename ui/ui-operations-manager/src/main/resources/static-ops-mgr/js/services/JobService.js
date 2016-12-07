/**
 * Service to hold all JobRestController.java urls
 */
var JobService = angular.module(MODULE_OPERATIONS).factory('JobData', ['$q', '$http','$log', 'HttpService','NotificationService','RestUrlService', function ($q, $http,$log, HttpService,NotificationService,RestUrlService) {
    var JobData = {};
    JobData.JOBS_QUERY_URL = RestUrlService.JOBS_QUERY_URL;
    JobData.JOBS_CHARTS_QUERY_URL = RestUrlService.JOBS_CHARTS_QUERY_URL;
    JobData.JOB_NAMES_URL = RestUrlService.JOB_NAMES_URL;
    JobData.DAILY_STATUS_COUNT_URL = RestUrlService.DAILY_STATUS_COUNT_URL;

    JobData.RUNNING_OR_FAILED_COUNTS_URL = RestUrlService.RUNNING_OR_FAILED_COUNTS_URL;

    JobData.DATA_CONFIDENCE_URL = RestUrlService.DATA_CONFIDENCE_URL;


    JobData.RESTART_JOB_URL = RestUrlService.RESTART_JOB_URL;

    JobData.STOP_JOB_URL = RestUrlService.STOP_JOB_URL;

    JobData.ABANDON_JOB_URL = RestUrlService.ABANDON_JOB_URL;

    JobData.FAIL_JOB_URL = RestUrlService.FAIL_JOB_URL;

    JobData.LOAD_JOB_URL = RestUrlService.LOAD_JOB_URL;

    JobData.JOB_PROGRESS_URL = RestUrlService.JOB_PROGRESS_URL;

    JobData.RELATED_JOBS_URL = RestUrlService.RELATED_JOBS_URL;

    JobData.restartJob = function ( executionId,params, callback, errorCallback) {
        return $http.post(JobData.RESTART_JOB_URL(executionId),params).
            success(function (data) {
                callback(data);
            }).error(function (msg) {
                var errorMessage;
                if(msg && msg.message){
                    errorMessage = msg.message;
                }
                if(errorMessage &&  errorMessage.startsWith("A job instance already exists and is complete")){
                    errorMessage = "Unable to restart.  This job is already complete.<br/> If you want to run this job again, change the parameters."
                }

             //   NotificationService.error( errorMessage);
                if(errorCallback) {
                    errorCallback(errorMessage);
                }
            })
    }

    JobData.failJob = function ( executionId, params, callback) {
        return $http.post(JobData.FAIL_JOB_URL(executionId),params).
            success(function (data) {
                callback(data);
            }).error(function (msg) {
                var errorMessasge = msg.error != undefined ? msg.error +': ': '';
                errorMessasge +=msg.message;
            //    NotificationService.error( errorMessasge);
            })
    }
    JobData.abandonJob = function ( executionId, params, callback) {
        $http.post(JobData.ABANDON_JOB_URL( executionId),params).
            success(function (data) {
                callback(data);
            }).error(function (msg) {
                var errorMessasge = msg.error != undefined ? msg.error +': ': '';
                errorMessasge +=msg.message;
            //    NotificationService.error( errorMessasge);
            })
    };

    JobData.stopJob = function ( executionId, params, callback) {
        $http.post(JobData.STOP_JOB_URL( executionId),params).
            success(function (data) {
                callback(data);
            }).error(function (msg) {
                var errorMessasge = msg.error != undefined ? msg.error +': ': '';
                errorMessasge +=msg.message;
              //  NotificationService.error( errorMessasge);
            })
    };


    /**
     *
     * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
     */
    JobData.getJobCountByStatus = function () {
        return new HttpService.get(JobData.JOB_COUNT_BY_STATUS_URL);

    }

    JobData.findAllJobs = function (successFn, errorFn, finallyFn) {
        return new HttpService.newRequestBuilder(JobData.ALL_JOBS_URL).success(successFn).error(errorFn).finally(finallyFn).build();
    };
    JobData.loadJob = function (instanceId) {
        return $http.get(JobData.LOAD_JOB_URL(instanceId));
    };

    /**
     * Get the progress of a Job
     * @param executionId
     * @returns {HttpPromise}
     */
    JobData.getProgress = function (executionId) {
        return $http.get(JobData.JOB_PROGRESS_URL(executionId));
    };

    JobData.lastSelectedTab = 'ALL';


    return JobData;
}]);
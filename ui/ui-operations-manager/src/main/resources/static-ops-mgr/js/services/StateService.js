/**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
angular.module(MODULE_OPERATIONS).service('StateService', function ($state) {

    var self = this;
    this.navigateToJobDetails=function(executionId){
        $state.go('job-details',{executionId:executionId});
    }

    this.navigateToFeedDetails=function(feedName){
        $state.go('feed-details',{feedName:feedName});
    }
    this.navigateToServiceDetails=function(serviceName){
        $state.go('service-details',{serviceName:serviceName});
    }

    this.navigateToServiceComponentDetails=function(serviceName, componentName){
        $state.go('service-component-details',{serviceName:serviceName, componentName:componentName});
    }

    this.navigateToCheckDataJobs=function(){
        $state.go('jobs.checkDataJobs',{resetDataTables:true});
    }
    this.navigateToAllJobsWithJobStatus=function(jobStatus){
        $state.go('jobs.allJobs',{jobStatus:jobStatus,resetDataTables:true});
    }
    this.navigateToAllJobsWithJobType=function(jobType){
        $state.go('jobs.allJobs',{jobType:jobType,resetDataTables:true});
    }
    this.navigateToFeedsWithFeed=function(feed){
        $state.go('feeds',{feed:feed, resetDataTables:true});
    }
    this.navigateToFeedsWithExitCode=function(exitCode){
        $state.go('feeds',{exitCode:exitCode,resetDataTables:true});
    }

    /**
     * Navigates to the details page for the specified alert.
     * @param {string} alertId the id of the alert
     */
    this.navigateToAlertDetails = function(alertId) {
        $state.go("alert-details", {alertId: alertId});
    };

});

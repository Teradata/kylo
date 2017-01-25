/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

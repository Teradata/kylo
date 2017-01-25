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
angular.module(MODULE_OPERATIONS).service('RestUrlService', function () {

    var self = this;

    this.ROOT = "";

    this.FEEDS_BASE = "/proxy/v1/feeds";
    this.JOBS_BASE = "/proxy/v1/jobs";
    this.SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
    this.FEED_HEALTH_URL = this.FEEDS_BASE+"/health";
    this.FEED_NAMES_URL = this.FEEDS_BASE+"/names";
    this.FEED_HEALTH_COUNT_URL = this.FEEDS_BASE+"/health-count";

    this.SPECIFIC_FEED_HEALTH_COUNT_URL = function(feedName) {
        return self.FEED_HEALTH_COUNT_URL+'/'+feedName+'/';
    }

    this.SPECIFIC_FEED_HEALTH_URL = function(feedName) {
        return self.FEED_HEALTH_URL+'/'+feedName+'/';
    }
    this.FEED_DAILY_STATUS_COUNT_URL = function(feedName){
        return self.FEEDS_BASE+"/"+feedName+"/daily-status-count";
    }

    this.ALERTS_URL = this.ROOT + "/proxy/v1/alerts";






//JOB urls


    this.JOBS_QUERY_URL = this.JOBS_BASE;
    this.JOBS_CHARTS_QUERY_URL = this.JOBS_BASE+'/list';
    this.JOB_NAMES_URL = this.JOBS_BASE+'/names';

    this.DAILY_STATUS_COUNT_URL = self.JOBS_BASE+"/daily-status-count/";

    this.RUNNING_OR_FAILED_COUNTS_URL = this.JOBS_BASE+'/running-failed-counts';


    this.DATA_CONFIDENCE_URL = "/proxy/v1/data-confidence/summary";


    this.RESTART_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/restart";
    }
    this.STOP_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/stop";
    }

    this.ABANDON_JOB_URL = function (executionId) {
        return self.JOBS_BASE+"/"+executionId+"/abandon";
    }

    this.ABANDON_ALL_JOBS_URL = function (feedId) {
        return self.JOBS_BASE+"/abandon-all/" + feedId;
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

this.SERVICES_URL = "/proxy/v1/service-monitor/";

    //Provenance Event Stats
    this.STATS_BASE = "/proxy/v1/provenance-stats";

    this.PROCESSOR_DURATION_FOR_FEED = function (feedName, timeInterval) {
        return self.STATS_BASE + "/" + feedName + "/processor-duration/" + timeInterval;
    }

    this.FEED_STATISTICS_OVER_TIME = function (feedName, timeInterval) {
        return self.STATS_BASE + "/" + feedName + "/" + timeInterval;
    }

    this.PROVENANCE_EVENT_TIME_FRAME_OPTIONS = this.STATS_BASE + "/time-frame-options";

    /**
     * Gets the alert details endpoint for the specified alert.
     * @param {string} alertId the id of the alert
     * @returns {string} the URL of the endpoint
     */
    this.ALERT_DETAILS_URL = function(alertId) {
        return "/proxy/v1/alerts/" + alertId;
    };

});

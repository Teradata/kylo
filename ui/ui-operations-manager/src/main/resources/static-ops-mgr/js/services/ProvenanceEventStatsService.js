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
angular.module(MODULE_OPERATIONS).factory('ProvenanceEventStatsService', function ($http, $q, RestUrlService) {

    var data = {

        getTimeFrameOptions: function () {

            var promise = $http.get(RestUrlService.PROVENANCE_EVENT_TIME_FRAME_OPTIONS);
            return promise;
        },

        getFeedProcessorDuration: function (feedName, timeFrame) {
            var self = this;

            var successFn = function (response) {

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.PROCESSOR_DURATION_FOR_FEED(feedName, timeFrame));
            promise.then(successFn, errorFn);
            return promise;
        },
        getFeedStatisticsOverTime: function (feedName, timeFrame) {
            var self = this;

            var successFn = function (response) {

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.FEED_STATISTICS_OVER_TIME(feedName, timeFrame));
            promise.then(successFn, errorFn);
            return promise;
        }

    }
    return data;

});

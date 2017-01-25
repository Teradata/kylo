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
angular.module(MODULE_OPERATIONS).service('ChartJobStatusService', function (IconService, Nvd3ChartService) {

    var self = this;
    this.renderEndUpdated = {};
    /*
    this.toChartData = function(jobStatusCountResponse) {
        var statusMap = {}

        var data = [];
        var dateMap = {};
        var responseData = jobStatusCountResponse;
        if(responseData) {

            angular.forEach(responseData, function (statusCount, i) {
                if (statusMap[statusCount.status] == undefined) {
                    statusMap[statusCount.status] = {};
                }
                dateMap[statusCount.date] = statusCount.date;
                statusMap[statusCount.status][statusCount.date] = statusCount.count;
            });

            var keys = Object.keys(dateMap),
                len = keys.length;
            keys.sort();

            angular.forEach(statusMap, function (dateCounts, status) {
                //fill in any empty dates with 0 values
                var valuesArray = [];
                angular.forEach(dateMap, function (date) {
                    if (dateCounts[date] == undefined) {
                        dateCounts[date] = 0;
                    }
                });

                for (var i = 0; i < len; i++) {
                    var date = keys[i];
                    var count = dateCounts[date];
                    valuesArray.push([parseInt(date), count]);
                }
                data.push({key: status, values: valuesArray, area: true, color: IconService.colorForJobStatus(status)});
            })
        }
        return data;
    }
     */
    this.toChartData = function (jobStatusCountResponse) {
        return Nvd3ChartService.toLineChartData(jobStatusCountResponse, [{label: 'status', value: 'count'}], 'date', IconService.colorForJobStatus);
    }





    this.shouldManualUpdate = function(chart){
            if(self.renderEndUpdated[chart] == undefined){
                self.renderEndUpdated[chart] = chart;
                return true;
            }
        else {
                return false;
            }
    }

});

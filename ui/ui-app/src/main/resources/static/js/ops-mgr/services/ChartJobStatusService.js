define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('ChartJobStatusService', ["IconService", "Nvd3ChartService",function (IconService, Nvd3ChartService) {

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

        this.shouldManualUpdate = function (chart) {
            if (self.renderEndUpdated[chart] == undefined) {
                self.renderEndUpdated[chart] = chart;
                return true;
            }
            else {
                return false;
            }
        }

    }]);
});
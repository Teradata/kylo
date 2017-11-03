define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
angular.module(moduleName).service('Nvd3ChartService', ["$timeout",function ($timeout) {

    var self = this;
    this.renderEndUpdated = {};
    this.timeoutMap = {};

    this.expireRenderEnd = function(chart){
        delete self.renderEndUpdated[chart];
    }

    this.shouldManualUpdate = function(chart){
        if(self.renderEndUpdated[chart] == undefined){
            self.renderEndUpdated[chart] = chart;
            if(self.timeoutMap[chart] != undefined){
                $timeout.cancel(self.timeoutMap[chart]);
            }
            self.timeoutMap[chart] = $timeout(function(){
                self.expireRenderEnd(chart) ;
            },3000);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * toLineChartData(response,[{label:'status',value:'count',valueFn:'optional fn to get the value',color:'optional'}],'date',IconService.colorForJobStatus);
     *
     * @param response
     * @param labelValueMapArr
     * @param xAxisKey
     * @param colorForSeriesFn
     * @param minTime
     * @param maxTime
     * @param maxDataPoints - max data points requested for the graph
     * @returns {Array}
     */
    this.toLineChartData = function (response, labelValueMapArr, xAxisKey, colorForSeriesFn, minTime, maxTime, maxDataPoints) {
        var dataMap = {}

        var data = [];
        var dateMap = {};
        var responseData = response;
        var labelColorMap = {};
        var labelDisabledMap = {};
        var configMap = {};
        if (responseData) {

            angular.forEach(responseData, function (item, i) {
                _.each(labelValueMapArr, function (labelValue) {
                    var label = item[labelValue.label];
                    if (label == undefined) {
                        label = labelValue.label; //label = Completed,Started
                    }
                    if (dataMap[label] == undefined) {
                        dataMap[label] = {};
                    }
                    dateMap[item[xAxisKey]] = item[xAxisKey]; //dateMap[item[maxEventTime]] = maxEventTime
                    var value;
                    // console.log("labelValeu.valueFn = ", labelValue.valueFn);
                    if (labelValue.valueFn != undefined) {
                        value = labelValue.valueFn(item);
                    }
                    else {
                        value = item[labelValue.value]; //item[jobsStartedPerSecond]
                    }
                    dataMap[label][item[xAxisKey]] = value; //dataMap[Started][maxEventTime] = jobsStartedPerSecond
                    if (labelValue['color'] != undefined) {
                        labelColorMap[label] = labelValue.color;
                    }
                    if (labelValue['disabled'] != undefined) {
                        labelDisabledMap[label] = labelValue.disabled;
                    }
                    configMap[label] = labelValue;
                });

            });

            var keys = Object.keys(dateMap),
                len = keys.length;
            keys.sort(); //sort all the dates

            if(angular.isDefined(minTime) && angular.isDefined(maxTime) && angular.isDefined(maxDataPoints)) {
                //add missing data points
                var timeInterval = (maxTime - minTime) / maxDataPoints;
                var doubleTimeInterval = timeInterval * 2;
                var currentTime = minTime;
                var closestTime;
                var newDateMap = [];
                for (var i = 0; i < maxDataPoints && currentTime < maxTime; i++) {
                    closestTime = getClosestTime(keys, currentTime);
                    var diff = Math.abs(currentTime - closestTime);
                    if (diff > doubleTimeInterval) {
                        newDateMap[currentTime] = currentTime;
                    } else {
                        newDateMap[closestTime] = closestTime;
                    }
                    currentTime += timeInterval;
                }

                function getClosestTime(times, goal) {
                    if (_.isUndefined(times) || _.isEmpty(times)) {
                        return 0;
                    }
                    return times.reduce(function (prev, curr) {
                        return (Math.abs(curr - goal) < Math.abs(prev - goal) ? curr : prev);
                    });
                }

                dateMap = newDateMap;
                keys = Object.keys(dateMap);
                len = keys.length;
                keys.sort(); //sort again after adding missing time points
            }

            angular.forEach(dataMap, function (labelCounts, label) {
                // //fill in any empty dates with 0 values
                var valuesArray = [];
                for (var i = 0; i < len; i++) {
                    var date = keys[i];
                    var count = labelCounts[date];
                    var dateAsNum = parseInt(date);
                    valuesArray.push([dateAsNum, _.isUndefined(count) ? 0 : count]);
                }
                var color = colorForSeriesFn != undefined ? colorForSeriesFn(label) : labelColorMap[label];
                var disabled = labelDisabledMap[label] != undefined ? labelDisabledMap[label] : false;
                var area = configMap[label]['area'] != undefined ? configMap[label]['area'] : true;
                data.push({key: label, values: valuesArray, area: area, color: color, disabled: disabled});
            })
        }
        return data;
    }


}]);
});

/*
 * Copyright (c) 2015.
 */

/**
 *
 */
angular.module(MODULE_OPERATIONS).service('Nvd3ChartService', function ($timeout) {

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
     * @returns {Array}
     */
    this.toLineChartData = function (response, labelValueMapArr, xAxisKey, colorForSeriesFn) {
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
                        label = labelValue.label;
                    }
                    if (dataMap[label] == undefined) {
                        dataMap[label] = {};
                    }
                    dateMap[item[xAxisKey]] = item[xAxisKey];
                    var value;
                    if (labelValue.valueFn != undefined) {
                        value = labelValue.valueFn(item);
                    }
                    else {
                        value = item[labelValue.value];
                    }
                    dataMap[label][item[xAxisKey]] = value;
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
            keys.sort();

            angular.forEach(dataMap, function (labelCounts, label) {
                //fill in any empty dates with 0 values
                var valuesArray = [];
                angular.forEach(dateMap, function (date) {
                    if (labelCounts[date] == undefined) {
                        labelCounts[date] = 0;
                    }
                });

                for (var i = 0; i < len; i++) {
                    var date = keys[i];
                    var count = labelCounts[date];
                    valuesArray.push([parseInt(date), count]);
                }
                var color = colorForSeriesFn != undefined ? colorForSeriesFn(label) : labelColorMap[label];
                var disabled = labelDisabledMap[label] != undefined ? labelDisabledMap[label] : false;
                var area = configMap[label]['area'] != undefined ? configMap[label]['area'] : true;
                data.push({key: label, values: valuesArray, area: area, color: color, disabled: disabled});
            })
        }
        return data;
    }


});
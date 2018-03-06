define(["require", "exports", "angular", "../module-name", "underscore", "pascalprecht.translate"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Nvd3ChartService = /** @class */ (function () {
        function Nvd3ChartService($timeout, $filter) {
            var _this = this;
            this.$timeout = $timeout;
            this.$filter = $filter;
            this.renderEndUpdated = {};
            this.timeoutMap = {};
            this.addToDataMap = function (dataMap, labelValueMapArr, x, value, label) {
                if (angular.isUndefined(label)) {
                    _.each(labelValueMapArr, function (lv) {
                        if (dataMap[lv.label] == undefined) {
                            dataMap[lv.label] = {};
                        }
                        dataMap[lv.label][x] = value;
                    });
                }
                else {
                    if (dataMap[label] == undefined) {
                        dataMap[label] = {};
                    }
                    dataMap[label][x] = value;
                }
            };
            this.expireRenderEnd = function (chart) {
                delete _this.renderEndUpdated[chart];
            };
            this.shouldManualUpdate = function (chart) {
                if (_this.renderEndUpdated[chart] == undefined) {
                    _this.renderEndUpdated[chart] = chart;
                    if (_this.timeoutMap[chart] != undefined) {
                        _this.$timeout.cancel(_this.timeoutMap[chart]);
                    }
                    _this.timeoutMap[chart] = _this.$timeout(function () {
                        _this.expireRenderEnd(chart);
                    }, 3000);
                    return true;
                }
                else {
                    return false;
                }
            };
            this.FILL_STRATEGY_TYPE = {
                MAX_DATA_POINTS: "MAX_DATA_POINTS",
                INTERVAL: "INTERVAL"
            };
            /**
             *
             * @param type FILL_STRATEGY_TYPE
             * @param value number
             * @return {{type: *, value: *}}
             */
            this.fillStrategy = function (type, value) {
                return { type: type, value: value };
            };
            this.fillAllStrategy = function (minValue, maxValue, dataMap, labelValueMapArr, incrementInterval, fillMiddle) {
                var incrementIntervalVal = incrementInterval;
                var diff = maxValue - minValue;
                //less than 5 min space out every 5 sec
                if (diff <= 300000) {
                    incrementIntervalVal = 5000;
                }
                else if (diff <= 3600000) {
                    // 1 hr diff, increment every 60 sec
                    incrementIntervalVal = 60000;
                }
                else if (diff <= 43200000) {
                    // 12 hr diff  every 5 min
                    incrementIntervalVal = 60000 * 5;
                }
                else {
                    // every 20 minutes
                    incrementIntervalVal = 60000 * 20;
                }
                /**
                 * The new map to be merged back into the dataMap
                 * @type {{}}
                 */
                var newDataMap = {};
                /**
                 * Min value in the dataMap
                 */
                var minDataPoint = minValue;
                /**
                 * Max Value in the dataMap
                 */
                var maxDataPoint = maxValue;
                if (_.isEmpty(dataMap)) {
                    var tmpVal = minValue;
                    while (tmpVal <= maxValue) {
                        _this.addToDataMap(newDataMap, labelValueMapArr, tmpVal, 0);
                        tmpVal += incrementIntervalVal;
                    }
                }
                else {
                    angular.forEach(labelValueMapArr, function (lv) {
                        var label = lv.label;
                        var labelCounts = dataMap[label] || {};
                        minDataPoint = minValue;
                        maxDataPoint = maxValue;
                        _this.keys = Object.keys(labelCounts).map(Number);
                        //Find the min/Max values if they exist
                        if (!_.isEmpty(labelCounts)) {
                            minDataPoint = _.min(_this.keys);
                            maxDataPoint = _.max(_this.keys);
                        }
                        //Start processing with the minValue on the graph
                        var tmpVal = minValue;
                        //iterate and add data points before the minDataPoint value
                        while (tmpVal < minDataPoint) {
                            _this.addToDataMap(newDataMap, labelValueMapArr, tmpVal, 0, label);
                            tmpVal += incrementIntervalVal;
                        }
                        //Reassign the tmpVal to be the starting dataPoint value
                        tmpVal = minDataPoint;
                        if (!_.isEmpty(labelCounts)) {
                            //fill in the body
                            //if its empty fill with 0's
                            if (fillMiddle) {
                                //attempt to fill in increments with 0
                                //start with the
                                var startingTmpVal = tmpVal;
                                tmpVal = startingTmpVal;
                                //sort by key
                                var orderedMap = {};
                                _this.keys.sort().forEach(function (key) {
                                    orderedMap[key] = labelCounts[key];
                                });
                                _.each(orderedMap, function (val, key) {
                                    var numericKey = parseInt(key);
                                    var diff = numericKey - tmpVal;
                                    if (diff > incrementIntervalVal) {
                                        tmpVal = numericKey;
                                        var len = Math.floor(diff / incrementIntervalVal);
                                        for (var i = 0; i < len; i++) {
                                            tmpVal += incrementIntervalVal;
                                            _this.addToDataMap(newDataMap, labelValueMapArr, tmpVal, 0, label);
                                        }
                                    }
                                });
                            }
                            //now start with the max value in the data set
                            tmpVal = maxDataPoint;
                            tmpVal += incrementIntervalVal;
                        }
                        // add in ending datapoints
                        while (tmpVal < maxValue) {
                            _this.addToDataMap(newDataMap, labelValueMapArr, tmpVal, 0, label);
                            tmpVal += incrementIntervalVal;
                        }
                    });
                }
                //merge into the dataMap
                angular.forEach(newDataMap, function (labelCounts, label) {
                    if (dataMap[label] == undefined) {
                        dataMap[label] = labelCounts;
                    }
                    else {
                        _.extend(dataMap[label], labelCounts);
                    }
                });
            };
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
            this.toLineChartData = function (response, labelValueMapArr, xAxisKey, colorForSeriesFn, minValue, maxValue) {
                //  var this = this;
                var dataMap = {};
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
                            var prevVal = dataMap[label][item[xAxisKey]];
                            if (angular.isDefined(prevVal)) {
                                value += prevVal;
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
                    if (angular.isDefined(minValue) && angular.isDefined(maxValue)) {
                        //Fill in gaps, before, after, and optionally in the middle of the data
                        _this.fillAllStrategy(minValue, maxValue, dataMap, labelValueMapArr, 5000, false);
                    }
                    angular.forEach(dataMap, function (labelCounts, label) {
                        var valuesArray = [];
                        var orderedMap = {};
                        Object.keys(labelCounts).sort().forEach(function (key) {
                            orderedMap[key] = labelCounts[key];
                            valuesArray.push([parseInt(key), labelCounts[key]]);
                        });
                        var color = colorForSeriesFn != undefined ? colorForSeriesFn(label) : labelColorMap[label];
                        var disabled = labelDisabledMap[label] != undefined ? labelDisabledMap[label] : false;
                        var area = (configMap[label] != undefined && configMap[label]['area'] != undefined) ? configMap[label]['area'] : true;
                        var displayLabel = _this.$filter('translate')(label);
                        data.push({ key: displayLabel, values: valuesArray, area: area, color: color, disabled: disabled });
                    });
                }
                return data;
            };
            this.determineMaxY = function (nvd3Dataset) {
                var max = 0;
                var max2 = 0;
                if (nvd3Dataset && nvd3Dataset[0]) {
                    max = d3.max(nvd3Dataset[0].values, function (d) {
                        return d[1];
                    });
                }
                if (nvd3Dataset && nvd3Dataset[1]) {
                    max2 = d3.max(nvd3Dataset[1].values, function (d) {
                        return d[1];
                    });
                }
                max = max2 > max ? max2 : max;
                if (max == undefined || max == 0) {
                    max = 5;
                }
                else {
                    max *= 1.2;
                }
                max = Math.round(max);
                return max;
            };
        }
        return Nvd3ChartService;
    }());
    exports.default = Nvd3ChartService;
    angular.module(module_name_1.moduleName).service('Nvd3ChartService', ["$timeout", "$filter", Nvd3ChartService]);
});
//# sourceMappingURL=Nvd3ChartService.js.map
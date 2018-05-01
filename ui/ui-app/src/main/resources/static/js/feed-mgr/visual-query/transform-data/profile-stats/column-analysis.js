define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var d3 = require('d3');
    var moduleName = require('feed-mgr/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                profile: '=',
                field: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/visual-query/transform-data/profile-stats/column-analysis.html',
            controller: "ColumnAnalysisController"
        };
    };
    var ColumnAnalysisController = /** @class */ (function () {
        function ColumnAnalysisController($scope, $timeout) {
            this.$scope = $scope;
            this.$timeout = $timeout;
            this.data = [];
            this.showValid = false;
            this.show();
        }
        ColumnAnalysisController.prototype.show = function () {
            var self = this;
            self.initializeStats();
            // populate metrics
            if (self.data && self.data.length > 0) {
                self.data.sort(self.compare);
                // rescale bar
                var total_1 = parseInt(self.totalCount);
                var scaleFactor_1 = (1 / (self.data[0].count / total_1));
                var cummCount_1 = 0;
                angular.forEach(self.data, function (item) {
                    var frequency = (item.count / total_1);
                    item.frequency = frequency * 100;
                    cummCount_1 += item.frequency;
                    item.cumm = cummCount_1;
                    item.width = item.frequency * scaleFactor_1;
                });
            }
        };
        ColumnAnalysisController.prototype.initializeStats = function () {
            var self = this;
            angular.forEach(self.profile, function (value) {
                if (value.columnName == self.field) {
                    switch (value.metricType) {
                        case 'TOP_N_VALUES':
                            var values = value.metricValue.split("^B");
                            angular.forEach(values, function (item) {
                                if (item != '') {
                                    var e = item.split("^A");
                                    self.data.push({ domain: e[1], count: parseInt(e[2]) });
                                }
                            });
                            break;
                        case 'TOTAL_COUNT':
                            self.totalCount = value.metricValue;
                            break;
                        case 'UNIQUE_COUNT':
                            self.unique = value.metricValue;
                            break;
                        case 'EMPTY_COUNT':
                            self.emptyCount = value.metricValue;
                            break;
                        case 'NULL_COUNT':
                            self.nullCount = value.metricValue;
                            break;
                        case 'COLUMN_DATATYPE':
                            self.columnDataType = value.metricValue;
                            break;
                        case 'MAX_LENGTH':
                            self.maxLen = value.metricValue;
                            break;
                        case 'MIN_LENGTH':
                            self.minLen = value.metricValue;
                            break;
                        case 'MAX':
                            self.max = value.metricValue;
                            break;
                        case 'MIN':
                            self.min = value.metricValue;
                            break;
                        case 'SUM':
                            self.sum = value.metricValue;
                            break;
                        case 'MEAN':
                            self.mean = value.metricValue;
                            break;
                        case 'STDDEV':
                            self.stddev = value.metricValue;
                            break;
                        case 'VARIANCE':
                            self.variance = value.metricValue;
                            break;
                        case 'HISTO':
                            self.histo = angular.fromJson(value.metricValue);
                            break;
                        case 'VALID_COUNT':
                            self.validCount = value.metricValue;
                            self.showValid = true;
                            break;
                        case 'INVALID_COUNT':
                            self.invalidCount = value.metricValue;
                            self.showValid = true;
                            break;
                    }
                }
            });
            if (this.unique != null) {
                this.percUnique = (parseInt(this.unique) / parseInt(this.totalCount));
            }
            if (this.emptyCount != null) {
                this.percEmpty = (parseInt(this.emptyCount) / parseInt(this.totalCount));
            }
            if (this.nullCount != null) {
                this.percNull = (parseInt(this.nullCount) / parseInt(this.totalCount));
            }
            if (this.showValid) {
                this.validCount = (parseInt(this.totalCount) - parseInt(this.invalidCount));
            }
        };
        /**
         * Comparator function for model reverse sort
         */
        ColumnAnalysisController.prototype.compare = function (a, b) {
            if (a.count < b.count)
                return 1;
            if (a.count > b.count)
                return -1;
            return 0;
        };
        return ColumnAnalysisController;
    }());
    exports.ColumnAnalysisController = ColumnAnalysisController;
    var columnHistogram = function () {
        return {
            restrict: "EA",
            bindToController: {
                chartData: '='
            },
            controllerAs: 'vm',
            scope: {},
            template: '<nvd3 options="vm.chartOptions" data="vm.chartData" api="vm.chartApi"></nvd3>',
            controller: "HistogramController"
        };
    };
    var HistogramController = /** @class */ (function () {
        function HistogramController($scope) {
            this.$scope = $scope;
            this.chartOptions = {
                "chart": {
                    "type": "multiBarChart",
                    "height": 315,
                    "width": 700,
                    "margin": {
                        "top": 20,
                        "right": 20,
                        "bottom": 45,
                        "left": 45
                    },
                    "clipEdge": true,
                    "duration": 250,
                    "stacked": false,
                    "xAxis": {
                        "axisLabel": "Value",
                        "showMaxMin": true,
                        "tickFormat": function (d) { return d3.format('0f')(d); }
                    },
                    "yAxis": {
                        "axisLabel": "Count",
                        "axisLabelDistance": -20,
                        "tickFormat": function (d) { return d3.format('0f')(d); }
                    },
                    "showControls": false,
                    "showLegend": false,
                    tooltip: {
                        contentGenerator: function (e) {
                            var data = e.data;
                            var index = e.index;
                            if (data === null)
                                return;
                            return "<table><tbody>" +
                                ("<tr><td class=\"key\">Min</td><td>" + data.min + "</td></tr>") +
                                ("<tr><td class=\"key\">Max</td><td>" + data.max + "</td></tr>") +
                                ("<tr><td class=\"key\">Count</td><td>" + data.y + "</td></tr>") +
                                "</tbody></table>";
                        }
                    }
                }
            };
            this.formatData = function () {
                var xAxis = this.chartData._1;
                var yAxis = this.chartData._2;
                var data = [];
                for (var i = 0; i < xAxis.length - 1; i++) {
                    data.push({ min: xAxis[i], max: xAxis[i + 1], x: (xAxis[i] + xAxis[i + 1]) / 2, y: yAxis[i] });
                }
                this.tooltipData = data;
                return [{
                        key: 'Count',
                        color: '#bcbd22',
                        values: data
                    }];
            };
        }
        HistogramController.prototype.$onInit = function () {
            console.log('chartData', this.chartData);
            this.chartData = this.formatData();
            /*
            this.$scope.$watch(this.chartData, (newValue:object)=> {
                this.chartApi.update();
            })
            */
        };
        return HistogramController;
    }());
    exports.HistogramController = HistogramController;
    angular.module(moduleName).controller("ColumnAnalysisController", ["$scope", "$timeout", ColumnAnalysisController]);
    angular.module(moduleName).controller("HistogramController", ["$scope", HistogramController]);
    angular.module(moduleName).directive("columnAnalysis", directive);
    angular.module(moduleName).directive("columnHistogram", columnHistogram);
});
//# sourceMappingURL=column-analysis.js.map
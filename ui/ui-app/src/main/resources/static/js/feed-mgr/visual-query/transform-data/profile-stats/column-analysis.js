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
                            self.histo = value.metricValue;
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
    angular.module(moduleName).controller("ColumnAnalysisController", ["$scope", "$timeout", ColumnAnalysisController]);
    angular.module(moduleName).directive("columnAnalysis", directive);
});
//# sourceMappingURL=column-analysis.js.map
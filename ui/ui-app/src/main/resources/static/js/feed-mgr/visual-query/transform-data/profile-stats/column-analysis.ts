import * as angular from "angular";

const d3 = require('d3');

const moduleName = require('feed-mgr/module-name');

const directive = function () {
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

export class ColumnAnalysisController implements ng.IComponentController {

    data: Array<any> = [];
    profile: any;
    field: string;
    totalCount: string;
    unique: string;
    maxLen: string;
    minLen: string;
    percUnique: string;
    percEmpty: string;
    emptyCount: string;
    columnDataType: string;
    nullCount: string;
    percNull: string;
    max : string;
    min : string;
    sum : string;
    mean : string;
    stddev : string;
    variance : string;

    constructor(private $scope: any, private $timeout: any) {
        this.show();
    }

    show(): void {
        var self = this;

        angular.forEach(self.profile, function (value: any) {
            if (value.columnName == self.field) {

                switch (value.metricType) {
                    case 'TOP_N_VALUES':
                        let values = value.metricValue.split("^B");
                        angular.forEach(values, function (item: string) {
                            if (item != '') {
                                let e = item.split("^A");
                                self.data.push({domain: e[1], count: parseInt(e[2])});
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
                    case 'PERC_EMPTY_VALUES':
                        self.percEmpty = value.metricValue;
                        break;
                    case 'NULL_COUNT':
                        self.nullCount = value.metricValue;
                        break;
                    case 'PERC_NULL_VALUES':
                        self.percNull = value.metricValue;
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
                }

            }
        });

        if (self.data && self.data.length > 0) {
            self.data.sort(self.compare);

            // rescale bar
            let total: number = parseInt(self.totalCount);
            let scaleFactor: number = (1 / (self.data[0].count / total));
            let cummCount: number = 0;
            angular.forEach(self.data, function (item: any) {
                let frequency = (item.count / total);
                item.frequency = frequency * 100;
                cummCount += item.frequency
                item.cumm = cummCount;
                item.width = item.frequency * scaleFactor;
            });

        }
    }

    /**
     * Comparator function for model reverse sort
     */
    compare(a: any, b: any): number {
        if (a.count < b.count)
            return 1;
        if (a.count > b.count)
            return -1;
        return 0;
    }


}

angular.module(moduleName).controller("ColumnAnalysisController", ["$scope", "$timeout", ColumnAnalysisController]);
angular.module(moduleName).directive("columnAnalysis", directive);

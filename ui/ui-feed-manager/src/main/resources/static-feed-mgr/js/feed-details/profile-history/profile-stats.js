(function () {
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/profile-history/profile-stats.html',
            controller: "FeedProfileStatsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    };

    var controller =  function($scope, $http, $sce,$stateParams, PaginationDataService, FeedService, RestUrlService, HiveService, Utils, BroadcastService) {

        var self = this;

        var chartColor = function (d, i) {
            return '#1f77b4'; //couldn't do it via css
        };
        var chartDuration = 500;

        self.data = [];
        self.loading = true;
        self.processingDate = new Date(HiveService.getUTCTime(self.processingdttm));
        self.model = FeedService.editFeedModel;
        self.selectedRow = [];
        self.filtered = [];
        self.summaryApi = {};
        self.lengthApi = {};

        self.topvalues = [];

        self.selectRow = function(event, row) {
            //console.log('selected row');
            //console.log(row);

            self.selectedRow = row;
            self.filtered = _.filter(self.data.rows, function(row){ return row.columnname == self.selectedRow.columnname; });
            self.topvalues = selectTopValues(self.filtered);
            self.summaryApi.update();
            self.lengthApi.update();
        };

        self.summaryOptions = {
            chart: {
                type: 'discreteBarChart',
                color: chartColor,
                margin : {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 0,
                    bottom: 20, //otherwise bottom labels are not visible
                    left: 0
                },
                duration: chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value + (1e-10);},
                showXAxis: true,
                showYAxis: false,
                showValues: true,
                valueFormat: function(d){
                    return d3.format(',.0f')(d);
                }
            }
        };

        self.summaryData = function() {
            var total = findNumericStat(self.filtered, 'TOTAL_COUNT');
            var nulls = findNumericStat(self.filtered, 'NULL_COUNT');
            var empty = findNumericStat(self.filtered, 'EMPTY_COUNT');
            var unique = findNumericStat(self.filtered, 'UNIQUE_COUNT');
            var invalid = findNumericStat(self.filtered, 'INVALID_COUNT');

            return [{
                key: "Summary",
                values: [
                    {
                        "label": "Total",
                        "value": total
                    },
                    {
                        "label": "Valid",
                        "value": total - invalid
                    },
                    {
                        "label": "Invalid",
                        "value": invalid
                    },
                    {
                        "label": "Unique",
                        "value": unique
                    },
                    {
                        "label": "Missing",
                        "value": nulls + empty
                    }
                ]
            }];
        };

        self.lengthOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: chartColor,
                margin : {
                    top: 0,
                    right: 0,
                    bottom: 0,
                    left: 70 //otherwise y axis labels are not visible
                },
                duration: chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showXAxis: true,
                showYAxis: false,
                showControls: false,
                showValues: true,
                showLegend: false
            }
        };

        self.lengthData = function() {
            var min = findNumericStat(self.filtered, 'MIN_LENGTH');
            var max = findNumericStat(self.filtered, 'MAX_LENGTH');

            return [{
                key: "Length",
                values: [
                    {
                        "label": "Minimum",
                        "value": min
                    },
                    {
                        "label": "Maximum",
                        "value": max
                    }
                ]
            }];
        };

        function findStat(rows, metrictype) {
            var row = _.find(rows, function(row){ return row.metrictype == metrictype; });
            return _.isUndefined(row) || _.isUndefined(row.metricvalue) ? "" : row.metricvalue;
        }

        function findNumericStat(rows, metrictype) {
            var stat = findStat(rows, metrictype);
            return stat == ""  ? 0 : Number(stat);
        }

        function selectTopValues(rows) {
            var topN = findStat(rows, 'TOP_N_VALUES');
            if (_.isUndefined(topN)) {
                return [];
            } else {
                var lines = topN.split("\n");
                function transformTopValues(line) {
                    var value = line.substring(line.indexOf(".") + 1, line.indexOf("("));
                    var count = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                    return {value: value, count: count};
                }
                return _.map(lines, transformTopValues);
            }
        }

        function getProfileStats(){
            self.loading = true;
            var successFn = function (response) {
                var transformFn = function(row,columns,displayColumns){
                    var _index = _.indexOf(displayColumns,'metrictype');
                    var metricType = row[columns[_index]];
                    if(metricType == 'TOP_N_VALUES') {
                        _index = _.indexOf(displayColumns,'metricvalue');
                        var val = row[columns[_index]];
                        if(val) {
                            var newVal = '';
                            angular.forEach(val.split('^B'),function(row) {
                                var itemArr = row.split('^A');
                                if(itemArr != undefined && itemArr.length ==3) {
                                    newVal += itemArr[0] + "." + itemArr[1] + " (" + itemArr[2] + ") \n";
                                }
                            });
                            row[columns[_index]] = newVal;
                        }
                    }

                };
                self.data = HiveService.transformResults2(response, ['processing_dttm'], transformFn);
                if (self.data && self.data.rows && self.data.rows.length > 0) {
                    self.selectedRow = self.data.rows[0];
                }
                console.log(self.data);
                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','profile-stats');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            var promise = $http.get(RestUrlService.FEED_PROFILE_STATS_URL(self.model.id),{params:{'processingdttm':self.processingdttm}});
            promise.then(successFn, errorFn);
            return promise;
        }

        getProfileStats();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileStatsController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileStats', directive);


})();

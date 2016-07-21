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
        self.selectedRow = {};
        self.filtered = [];
        self.summaryApi = {};
        self.statApi = {};

        self.topvalues = [];

        self.selectRowAndUpdateCharts = function(event, row) {
            selectRow(row);
            updateCharts();
        };

        function selectRow(row) {
            selectColumn(row);

            selectColumnData();
            selectType();
            selectTopValues();
        }

        function selectColumn(row) {
            self.selectedRow.prevColumn = self.selectedRow.columnname;
            self.selectedRow.columnname = row.columnname;
        }

        function selectColumnData() {
            self.filtered = _.filter(self.data.rows, function (row) {
                return row.columnname == self.selectedRow.columnname;
            });
        }

        function selectType() {
            var type = findStat(self.filtered, 'COLUMN_DATATYPE');
            if (_.isUndefined(type)) {
                type = "UnknownType";
            }
            type = type.substring(0, type.indexOf("Type"));
            self.selectedRow.type = type;
            if (type == "String") {
                self.selectedRow.profile = "String";
            } else if (type == "Long" || type == "Double") {
                self.selectedRow.profile = "Numeric";
            } else if (type == "Timestamp") {
                self.selectedRow.profile = "Time";
            } else {
                self.selectedRow.profile = "Unknown";
            }
        }

        function updateCharts() {
            self.summaryApi.update();
            if (self.selectedRow.prevColumn != "(ALL)") {
                //otherwise will update the table twice,
                //once when its shown after being hidden for '(ALL)' column and
                //once with explicit call to update here
                self.statApi.update();
            }
        }

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
            var valid = total - invalid;

            //display negative values in red
            var color = chartColor();
            if (valid < 0) {
                color = "red";
            }

            var values = [];
            values.push({"label": "Total", "value": total});
            values.push({"label": "Valid", "value": valid, "color": color});
            values.push({"label": "Invalid", "value": invalid});

            if (self.selectedRow.columnname != '(ALL)') {
                values.push({"label": "Unique", "value": unique});
                values.push({"label": "Missing", "value": nulls + empty});
            }

            return [{key: "Summary", values: values}];
        };

        self.statOptions = {
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

        self.statData = function() {
            console.log("calculating stat data");

            var min = findNumericStat(self.filtered, 'MIN_LENGTH');
            var max = findNumericStat(self.filtered, 'MAX_LENGTH');

            return [{
                key: "Stat",
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

        function selectTopValues() {
            var topN = findStat(self.filtered, 'TOP_N_VALUES');
            var topVals = [];
            if (_.isUndefined(topN)) {
                topVals = [];
            } else {
                var lines = topN.split("\n");
                function transformTopValues(line) {
                    var value = line.substring(line.indexOf(".") + 1, line.indexOf("("));
                    var count = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                    return {value: value, count: count};
                }
                topVals = _.map(lines, transformTopValues);
            }
            self.topvalues = topVals;
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
                    selectRow(self.data.rows[0]);
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

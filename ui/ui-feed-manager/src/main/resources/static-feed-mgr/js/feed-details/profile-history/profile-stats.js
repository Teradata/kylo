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

        self.data = [];
        self.loading = true;
        self.processingDate = new Date(HiveService.getUTCTime(self.processingdttm));
        self.model = FeedService.editFeedModel;
        self.selectedRow = {};
        self.filtered = [];
        self.summaryApi = {};
        self.stringApi = {};
        self.numericApi = {};
        self.percApi = {};
        self.topvalues = [];

        self.selectRowAndUpdateCharts = function(event, row) {
            //called when user selects the column
            self.selectRow(row);
            self.updateCharts();
        };

        self.chartColor = function (d, i) {
            return '#f08c38'; //#1f77b4'; //couldn't do it via css
        };
        self.chartDuration = 500;
        self.multiBarHorizontalChartMarginLeft = 80;
        self.multiBarHorizontalChartMarginRight = 50;

        self.selectRow = function(row) {
            //console.log("Selecting row " + row);

            self.selectColumn(row);

            self.selectColumnData();
            self.selectType();
            self.selectTopValues();
            self.selectTimeValues();
            self.selectStringValues();
            self.selectNumericValues();
        }

        self.selectColumn = function(row) {
            self.selectedRow.prevProfile = self.selectedRow.profile;
            self.selectedRow.columnname = row.columnname;
        }

        self.selectColumnData = function() {
            self.filtered = _.filter(self.data.rows, function (row) {
                return row.columnname == self.selectedRow.columnname;
            });
        }

        self.selectType = function() {
            var type = self.findStat(self.filtered, 'COLUMN_DATATYPE');
            if (_.isUndefined(type)) {
                type = "UnknownType";
            }
            type = type.substring(0, type.indexOf("Type"));
            self.selectedRow.type = type;
            if (type == "String") {
                self.selectedRow.profile = "String";
            } else if (type == "Long" || type == "Double"|| type == "Float" || type == "Byte" || type == "Integer") {
                self.selectedRow.profile = "Numeric";
            } else if (type == "Timestamp" || type == "Date") {
                self.selectedRow.profile = "Time";
            } else {
                self.selectedRow.profile = "Unknown";
            }
        }


        self.updateCharts = function() {

            self.summaryApi.update();
            if (self.selectedRow.prevProfile != "Unknown") {
                //chart with percents is not shown only for Unknown, i.e.
                // in all other cases it needs to be updated
                self.percApi.update();
            }
        }

        self.summaryOptions = {
            chart: {
                type: 'discreteBarChart',
                color: self.chartColor,
                height: 270,
                margin : {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 0,
                    bottom: 25, //otherwise bottom labels are not visible
                    left: 0
                },
                duration: self.chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value + (1e-10);},
                showXAxis: true,
                showYAxis: false,
                showValues: true,
                xAxis: {
                    tickPadding: 10
                },
                valueFormat: function (d) {
                    return d3.format(',.0f')(d);
                }
            }
        };

        /* Returns the class indicating an active column selection */
        self.getClass = function(item) {
            if (item.columnname == self.selectedRow.columnname) {
                return "md-raised"
            } else {
                return "";
            }
        }

        self.summaryData = function() {
            var total = self.findNumericStat(self.filtered, 'TOTAL_COUNT');
            var nulls = self.findNumericStat(self.filtered, 'NULL_COUNT');
            var empty = self.findNumericStat(self.filtered, 'EMPTY_COUNT');
            var unique = self.findNumericStat(self.filtered, 'UNIQUE_COUNT');
            var invalid = self.findNumericStat(self.filtered, 'INVALID_COUNT');
            var valid = total - invalid;

            //display negative values in red
            var color = self.chartColor();
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

        self.stringOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: self.chartColor,
                height: 125,
                margin : {
                    top: 0,
                    right: 0,
                    bottom: 0,
                    left: self.multiBarHorizontalChartMarginLeft //otherwise y axis labels are not visible
                },
                duration: self.chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showXAxis: true,
                showYAxis: false,
                showControls: false,
                showValues: true,
                showLegend: false,
                valueFormat: function(d){
                    return d3.format(',.0f')(d);
                }
            }
        };

        self.stringData = function() {
            //console.log("calculating string data");
            var values = [];

            values.push({"label": "Minimum", "value": findNumericStat(self.filtered, 'MIN_LENGTH')});
            values.push({"label": "Maximum", "value": findNumericStat(self.filtered, 'MAX_LENGTH')});

            return [{key: "Stats", values: values}];
        };

        self.numericOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: self.chartColor,
                height: 250,
                margin : {
                    top: 0,
                    right: self.multiBarHorizontalChartMarginRight, //otherwise large numbers are cut off
                    bottom: 0,
                    left: self.multiBarHorizontalChartMarginLeft //otherwise y axis labels are not visible
                },
                duration: self.chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showXAxis: true,
                showYAxis: false,
                showControls: false,
                showValues: true,
                showLegend: false
            }
        };

        self.percOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: self.chartColor,
                height: 200,
                margin : {
                    top: 0,
                    right: self.multiBarHorizontalChartMarginRight, //otherwise large numbers are cut off
                    bottom: 0,
                    left: self.multiBarHorizontalChartMarginLeft //otherwise y axis labels are not visible
                },
                duration: self.chartDuration,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showXAxis: true,
                showYAxis: false,
                showControls: false,
                showValues: true,
                showLegend: false,
                valueFormat: function (n) {
                    return d3.format(',.1f')(n) + " %";
                }
            }
        };

        self.percData = function() {
            //console.log("calculating percentage data");
            var values = [];

            values.push({label: "Nulls", value: self.findNumericStat(self.filtered, 'PERC_NULL_VALUES')});
            values.push({label: "Unique", value: self.findNumericStat(self.filtered, 'PERC_UNIQUE_VALUES')});
            values.push({label: "Duplicates", value: self.findNumericStat(self.filtered, 'PERC_DUPLICATE_VALUES')});

            return [{key: "Stats", values: values}];
        };

        self.findStat = function(rows, metrictype) {
            var row = _.find(rows, function(row){ return row.metrictype == metrictype; });
            return _.isUndefined(row) || _.isUndefined(row.metricvalue) ? "" : row.metricvalue;
        }

        self.findNumericStat = function(rows, metrictype) {
            var stat = self.findStat(rows, metrictype);
            return stat == ""  ? 0 : Number(stat);
        }

        self.selectTopValues = function() {
            var topN = self.findStat(self.filtered, 'TOP_N_VALUES');
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

        self.selectTimeValues = function() {
            var timeVals = [];
            self.timevalues = timeVals;
            if (self.selectedRow.profile == "Time") {
                //console.log("calculating time data");

                timeVals.push({name: "Maximum", value: self.findStat(self.filtered, 'MAX_TIMESTAMP')});
                timeVals.push({name: "Minimum", value: self.findStat(self.filtered, 'MIN_TIMESTAMP')});
            }
        }

        self.selectNumericValues = function() {
            //console.log("calculating numeric data");
            var values = [];
            self.numericvalues = values;

            if (self.selectedRow.profile == "Numeric") {
                values.push({"name": "Minimum", "value": self.findNumericStat(self.filtered, 'MIN')});
                values.push({"name": "Maximum", "value": self.findNumericStat(self.filtered, 'MAX')});
                values.push({"name": "Mean", "value": self.findNumericStat(self.filtered, 'MEAN')});
                values.push({"name": "Std Dev", "value": self.findNumericStat(self.filtered, 'STDDEV')});
                values.push({"name": "Variance", "value": self.findNumericStat(self.filtered, 'VARIANCE')});
                values.push({"name": "Sum", "value": self.findNumericStat(self.filtered, 'SUM')});
            }

        }

        self.selectStringValues = function() {
            var vals = [];
            self.stringvalues = vals;
            if (self.selectedRow.profile == "String") {
                //console.log("calculating time data");

                vals.push({name: "Longest", value: self.findStat(self.filtered, 'LONGEST_STRING')});
                vals.push({name: "Shortest", value: self.findStat(self.filtered, 'SHORTEST_STRING')});
                vals.push({name: "Min (Case Sensitive)", value: self.findStat(self.filtered, 'MIN_STRING_CASE')});
                vals.push({name: "Max (Case Sensitive)", value: self.findStat(self.filtered, 'MAX_STRING_CASE')});
                vals.push({name: "Min (Case Insensitive)", value: self.findStat(self.filtered, 'MIN_STRING_ICASE')});
                vals.push({name: "Max (Case Insensitive)", value: self.findStat(self.filtered, 'MAX_STRING_ICASE')});
            }
        }

        self.getProfileStats = function(){
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
                    var unique = _.uniq(self.data.rows, _.property('columnname'));
                    self.sorted = _.sortBy(unique, _.property('columnname'));
                    if (self.sorted && self.sorted.length > 1) {
                        //default to selecting other than (ALL) column - (ALL) column will be first, so we select second
                        self.selectRow(self.sorted[1]);
                    } else if (self.sorted && self.sorted.length > 0) {
                        //fall back to selecting first column if no other exist
                        self.selectRow(self.sorted[1]);
                    }
                }

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

        this.getProfileStats();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileStatsController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileStats', directive);


})();

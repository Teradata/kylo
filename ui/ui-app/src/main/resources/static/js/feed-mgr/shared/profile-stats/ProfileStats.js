define(["angular", "feed-mgr/module-name", "angular-nvd3"], function (angular, moduleName) {
    /**
     * Name of the column that represents all columns.
     * @type {string}
     */
    var ALL_COLUMN_NAME = "(ALL)";

    /**
     * Color to use for charts.
     * @type {string}
     */
    var CHART_COLOR = "#f08c38";

    var ProfileStatsDirective = function () {
        return {
            controller: "ProfileStatsController",
            controllerAs: "vm",
            restrict: "E",
            scope: {
                hideColumns: "=",
                model: "="
            },
            templateUrl: "js/feed-mgr/shared/profile-stats/profile-stats.html"
        };
    };

    var ProfileStatsController = function ($scope, $timeout, HiveService) {
        var self = this;

        /**
         * Duration of the chart animations.
         * @type {number}
         */
        self.chartDuration = 500;

        /**
         * Names of columns in data.
         * @type {{columnName: string, metricType: string, metricValue: string}}
         */
        self.columns = {
            columnName: "columnName",
            metricType: "metricType",
            metricValue: "metricValue"
        };

        /**
         * Column statistics.
         * @type {Object}
         */
        self.data = {};

        /**
         * Chart left margin.
         * @type {number}
         */
        self.multiBarHorizontalChartMarginLeft = 80;

        /**
         * Chart right margin.
         * @type {number}
         */
        self.multiBarHorizontalChartMarginRight = 50;

        /**
         * Promise for the next chart update.
         * @type {Promise}
         */
        self.nextChartUpdate = null;

        /**
         * API for the Relative Statistics chart.
         * @type {Object}
         */
        self.percApi = {};

        /**
         * Options for the Relative Statistics chart.
         * @type {Object}
         */
        self.percOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: function () {
                    return CHART_COLOR;
                },
                height: 200,
                margin: {
                    top: 0,
                    right: self.multiBarHorizontalChartMarginRight, //otherwise large numbers are cut off
                    bottom: 0,
                    left: self.multiBarHorizontalChartMarginLeft //otherwise y axis labels are not visible
                },
                duration: self.chartDuration,
                x: function (d) {
                    return d.label;
                },
                y: function (d) {
                    return d.value;
                },
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

        /**
         * Statistics for the select column.
         * @type {Object}
         */
        self.selectedRow = {};

        /**
         * Indicates that only columns with column statistics should be displayed.
         * @type {boolean}
         */
        self.showOnlyProfiled = false;

        /**
         * Column statistics sorted by column name.
         * @type {Array.<Object>}
         */
        self.sorted = [];

        /**
         * API for the Summary chart.
         * @type {Object}
         */
        self.summaryApi = {};

        /**
         * Options for the Summary chart.
         * @type {Object}
         */
        self.summaryOptions = {
            chart: {
                type: 'discreteBarChart',
                color: function () {
                    return CHART_COLOR;
                },
                height: 270,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 0,
                    bottom: 25, //otherwise bottom labels are not visible
                    left: 0
                },
                duration: self.chartDuration,
                x: function (d) {
                    return d.label;
                },
                y: function (d) {
                    return d.value + (1e-10);
                },
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

        // Watch for changes to the model
        $scope.$watch(
            function () {
                return $scope.model;
            },
            function () {
                self.onModelChange();
            }
        );

        /**
         * Finds the metric value for the specified metric type.
         *
         * @param {Array.<Object>} rows the column statistics
         * @param {string} metricType the metric type
         * @returns {string} the metric value
         */
        self.findStat = function (rows, metricType) {
            var row = _.find(rows, function (row) {
                return row[self.columns.metricType] === metricType;
            });
            return (angular.isDefined(row) && angular.isDefined(row[self.columns.metricValue])) ? row[self.columns.metricValue] : "";
        };

        /**
         * Finds the numeric metric value for the specified metric type.
         *
         * @param {Array.<Object>} rows the column statistics
         * @param {string} metricType the metric type
         * @returns {number} the metric value
         */
        self.findNumericStat = function (rows, metricType) {
            var stat = self.findStat(rows, metricType);
            return stat === "" ? 0 : Number(stat);
        };

        /**
         * Formats the specified model row.
         *
         * @param {Array} row the model row
         * @param {Array.<string>} columns the list of column system names
         * @param {Array.<displayColumns>} displayColumns the list of column display names
         */
        self.formatRow = function (row, columns, displayColumns) {
            // Determine metric type
            var index = _.indexOf(displayColumns, self.columns.metricType);
            var metricType = row[columns[index]];

            // Modify value of 'Top N Values' metric
            if (metricType === "TOP_N_VALUES") {
                index = _.indexOf(displayColumns, self.columns.metricValue);
                var val = row[columns[index]];
                if (val) {
                    var newVal = "";
                    angular.forEach(val.split("^B"), function (row) {
                        var itemArr = row.split("^A");
                        if (angular.isArray(itemArr) && itemArr.length === 3) {
                            newVal += itemArr[0] + "." + itemArr[1] + " (" + itemArr[2] + ") \n";
                        }
                    });
                    row[columns[index]] = newVal;
                }
            }
        };

        /**
         * Returns the class indicating an active column selection.
         */
        self.getClass = function (item) {
            return (item[self.columns.columnName] === self.selectedRow.columnName) ? "md-raised" : "";
        };

        /**
         * Indicates if the specified column should be displayed.
         *
         * @param {Object} column the column
         * @returns {boolean} true if the column should be displayed, or false otherwise
         */
        self.hasProfile = function (column) {
            return (!self.showOnlyProfiled || column.isProfiled);
        };

        /**
         * Indicates if the specified column has profile statistics.
         *
         * @param {Object} item the column
         * @returns {boolean} true if the column has profile statistics, or false otherwise
         */
        self.isProfiled = function (item) {
            if (_.isUndefined(item.isProfiled)) {
                var filtered = _.filter(self.data.rows, function (row) {
                    return row[self.columns.columnName] === item[self.columns.columnName];
                });

                // anything profiled will have "COLUMN_DATATYPE"
                var type = self.findStat(filtered, 'COLUMN_DATATYPE');
                item.isProfiled = (type !== "");
            }
            return item.isProfiled;
        };

        /**
         * Updates the profile data with changes to the model.
         */
        self.onModelChange = function () {
            // Determine column names
            if (angular.isArray($scope.model) && $scope.model.length > 0) {
                if (angular.isDefined($scope.model[0].columnName)) {
                    self.columns.columnName = "columnName";
                    self.columns.metricType = "metricType";
                    self.columns.metricValue = "metricValue";
                } else {
                    self.columns.columnName = "columnname";
                    self.columns.metricType = "metrictype";
                    self.columns.metricValue = "metricvalue";
                }
            }

            // Process the model
            var hideColumns = angular.isArray($scope.hideColumns) ? $scope.hideColumns : [];
            self.data = HiveService.transformResults2({data: $scope.model}, hideColumns, self.formatRow);

            // Sort by column name
            if (angular.isArray(self.data.rows) && self.data.rows.length > 0) {
                var unique = _.uniq(self.data.rows, _.property(self.columns.columnName));
                self.sorted = _.sortBy(unique, _.property(self.columns.columnName));
                if (self.sorted && self.sorted.length > 1) {
                    //default to selecting other than (ALL) column - (ALL) column will be first, so we select second
                    self.selectRow(self.sorted[1]);
                } else if (self.sorted && self.sorted.length > 0) {
                    //fall back to selecting first column if no other exist
                    self.selectRow(self.sorted[1]);
                }
            }

            // Determine total number of rows
            var allColumnData = _.filter(self.data.rows, function (row) {
                return row[self.columns.columnName] === ALL_COLUMN_NAME;
            });
            self.totalRows = self.findNumericStat(allColumnData, 'TOTAL_COUNT');

            // Update selected row
            if (angular.isString(self.selectedRow.columnName)) {
                var newSelectedRow = _.find(self.sorted, function (row) {
                    return row[self.columns.columnName] === self.selectedRow.columnName;
                });
                self.selectRowAndUpdateCharts(null, newSelectedRow);
            }

            // Ensure charts are the correct size
            if (self.nextChartUpdate !== null) {
                $timeout.cancel(self.nextChartUpdate);
            }

            self.nextChartUpdate = $timeout(function () {
                self.updateCharts();
                self.nextChartUpdate = null;
            }, self.chartDuration);
        };

        /**
         * Gets the data for the Relative Statistics graph.
         *
         * @returns {Array.<Object>} the graph data
         */
        self.percData = function () {
            var values = [];

            values.push({label: "Nulls", value: self.findNumericStat(self.filtered, 'PERC_NULL_VALUES')});
            values.push({label: "Unique", value: self.findNumericStat(self.filtered, 'PERC_UNIQUE_VALUES')});
            values.push({label: "Duplicates", value: self.findNumericStat(self.filtered, 'PERC_DUPLICATE_VALUES')});

            return [{key: "Stats", values: values}];
        };

        /**
         * Sets the selected row.
         *
         * @param {Object} row the selected row
         */
        self.selectColumn = function (row) {
            self.selectedRow.prevProfile = self.selectedRow.profile;
            self.selectedRow.columnName = row[self.columns.columnName];
        };

        /**
         * Sets the selected column data based on the selected row.
         */
        self.selectColumnData = function () {
            self.filtered = _.filter(self.data.rows, function (row) {
                return row[self.columns.columnName] === self.selectedRow.columnName;
            });
        };

        /**
         * Sets the values for the Numeric Stats table.
         */
        self.selectNumericValues = function () {
            var values = [];
            self.numericvalues = values;

            if (self.selectedRow.profile === "Numeric") {
                values.push({"name": "Minimum", "value": self.findNumericStat(self.filtered, 'MIN')});
                values.push({"name": "Maximum", "value": self.findNumericStat(self.filtered, 'MAX')});
                values.push({"name": "Mean", "value": self.findNumericStat(self.filtered, 'MEAN')});
                values.push({"name": "Std Dev", "value": self.findNumericStat(self.filtered, 'STDDEV')});
                values.push({"name": "Variance", "value": self.findNumericStat(self.filtered, 'VARIANCE')});
                values.push({"name": "Sum", "value": self.findNumericStat(self.filtered, 'SUM')});
            }

        };

        /**
         * Selects the specified row.
         *
         * @param {Object} row the row to select
         */
        self.selectRow = function (row) {
            self.selectColumn(row);
            self.selectColumnData();
            self.selectType();
            self.selectTopValues();
            self.selectTimeValues();
            self.selectStringValues();
            self.selectNumericValues();
        };

        /**
         * Selects the specified row and updates charts.
         *
         * @param event
         * @param {Object} row the row to be selected
         */
        self.selectRowAndUpdateCharts = function (event, row) {
            //called when user selects the column
            self.selectRow(row);
            self.updateCharts();
        };

        /**
         * Sets the values for the String Stats table.
         */
        self.selectStringValues = function () {
            var vals = [];
            self.stringvalues = vals;
            if (self.selectedRow.profile === "String") {
                vals.push({name: "Longest", value: self.findStat(self.filtered, 'LONGEST_STRING')});
                vals.push({name: "Shortest", value: self.findStat(self.filtered, 'SHORTEST_STRING')});
                vals.push({name: "Min (Case Sensitive)", value: self.findStat(self.filtered, 'MIN_STRING_CASE')});
                vals.push({name: "Max (Case Sensitive)", value: self.findStat(self.filtered, 'MAX_STRING_CASE')});
                vals.push({name: "Min (Case Insensitive)", value: self.findStat(self.filtered, 'MIN_STRING_ICASE')});
                vals.push({name: "Max (Case Insensitive)", value: self.findStat(self.filtered, 'MAX_STRING_ICASE')});
            }
        };

        /**
         * Sets the values for the Time Stats table.
         */
        self.selectTimeValues = function () {
            var timeVals = [];
            self.timevalues = timeVals;
            if (self.selectedRow.profile === "Time") {
                timeVals.push({name: "Maximum", value: self.findStat(self.filtered, 'MAX_TIMESTAMP')});
                timeVals.push({name: "Minimum", value: self.findStat(self.filtered, 'MIN_TIMESTAMP')});
            }
        };

        /**
         * Sets the values for the Top Values table.
         */
        self.selectTopValues = function () {
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
        };

        /**
         * Determines the type of the selected column.
         */
        self.selectType = function () {
            var type = self.findStat(self.filtered, 'COLUMN_DATATYPE');
            if (_.isUndefined(type)) {
                type = "UnknownType";
            }
            type = type.substring(0, type.indexOf("Type"));
            self.selectedRow.type = type;
            if (type === "String") {
                self.selectedRow.profile = "String";
            } else if (type === "Long" || type === "Double" || type === "Float" || type === "Byte" || type === "Integer" || type === "Decimal") {
                self.selectedRow.profile = "Numeric";
            } else if (type === "Timestamp" || type === "Date") {
                self.selectedRow.profile = "Time";
            } else {
                self.selectedRow.profile = "Unknown";
            }
        };

        /**
         * Gets the data for the Summary graph.
         *
         * @returns {Array.<Object>} the graph data
         */
        self.summaryData = function () {
            var nulls = self.findNumericStat(self.filtered, 'NULL_COUNT');
            var empty = self.findNumericStat(self.filtered, 'EMPTY_COUNT');
            var unique = self.findNumericStat(self.filtered, 'UNIQUE_COUNT');
            var invalid = self.findNumericStat(self.filtered, 'INVALID_COUNT');
            var valid = self.totalRows - invalid;

            //display negative values in red
            var color = CHART_COLOR;
            if (valid < 0) {
                color = "red";
            }

            var values = [];
            values.push({"label": "Total", "value": self.totalRows});
            values.push({"label": "Valid", "value": valid, "color": color});
            values.push({"label": "Invalid", "value": invalid});

            if (self.selectedRow.columnName !== '(ALL)') {
                values.push({"label": "Unique", "value": unique});
                values.push({"label": "Missing", "value": nulls + empty});
            }

            return [{key: "Summary", values: values}];
        };

        /**
         * Updates the Summary and Relative Statistics charts.
         */
        self.updateCharts = function () {
            if (angular.isDefined(self.summaryApi.update)) {
                self.summaryApi.update();
            }
            if (angular.isDefined(self.percApi.update)) {
                self.percApi.update();
            }
        };
    };

    angular.module(moduleName).controller("ProfileStatsController", ["$scope", "$timeout", "HiveService", ProfileStatsController]);
    angular.module(moduleName).directive("profileStats", ProfileStatsDirective);
});

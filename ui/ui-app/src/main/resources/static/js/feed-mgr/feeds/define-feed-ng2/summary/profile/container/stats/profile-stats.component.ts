import {Component, Injector, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import * as angular from 'angular';
import * as _ from 'underscore';
import 'd3';
import 'nvd3';
import {TranslateService} from '@ngx-translate/core';
import {HttpClient} from '@angular/common/http';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {ProfileValidComponent} from '../valid/profile-valid.component';
import {ProfileInvalidComponent} from '../invalid/profile-invalid.component';

declare let d3: any;


@Component({
    selector: "profile-stats2",
    styleUrls: ["./profile-stats.component.scss"],
    templateUrl: "./profile-stats.component.html"
})
export class ProfileStatsComponent implements OnInit, OnChanges  {

    private static topOfPageLoader: string = "ProfileStatsComponent.topOfPageLoader";

    /**
     * Name of the column that represents all columns.
     * @type {string}
     */
    ALL_COLUMN_NAME = "(ALL)";

    /**
     * Color to use for charts.
     * @type {string}
     */
    CHART_COLOR = "#00b2b1";

    /**
     * Duration of the chart animations.
     * @type {number}
     */
    chartDuration: number = 500;
    /**
     * Names of columns in data.
     * @type {{columnName: string, metricType: string, metricValue: string}}
     */
    columns: {
        columnName: string,
        metricType: string,
        metricValue: string
    };
    /**
     * Column statistics.
     * @type {Object}
     */
    data: any = {};
    /**
     * Chart left margin.
     * @type {number}
     */
    multiBarHorizontalChartMarginLeft: number = 80;
    /**
     * Chart right margin.
     * @type {number}
     */
    multiBarHorizontalChartMarginRight: number = 50;
    /**
     * Promise for the next chart update.
     * @type {Promise}
     */
    nextChartUpdate: any = null;
    /**
     * API for the Relative Statistics chart.
     * @type {Object}
     */
    percApi: any = {};
    percOptions: any;
    /**
     * Statistics for the select column.
     * @type {Object}
     */
    selectedRow: any = {};
    /**
     * Indicates that only columns with column statistics should be displayed.
     * @type {boolean}
     */
    showOnlyProfiled: boolean = false;
    /**
     * Column statistics sorted by column name.
     * @type {Array.<Object>}
     */
    sorted: any[] = [];
    /**
     * API for the Summary chart.
     * @type {Object}
     */
    summaryApi: any = {};
    summaryOptions: any;
    numericvalues: any;
    timevalues: any;
    selectTopdValues: any;
    totalRows: any;
    filtered: any;
    stringvalues: any;
    topvalues: any;

    hideColumns: any;

    $timeout: angular.ITimeoutService;
    private hiveService: any;
    private restUrlService: any;

    @Input()
    private feedId: string;

    @Input()
    private processingdttm: string;

    @Input()
    private active: boolean;
    private activated: boolean = false;

    private model: any;

    private summaryData: { key: string; values: any[] }[];
    private percData: { key: string; values: any[] }[];
    private labelNulls: string;
    private labelUnique: string;
    private labelDuplicates: string;
    private labelMinimum: string;
    private labelMaximum: string;
    private labelMean: string;
    private labelStdDev: string;
    private labelVariance: string;
    private labelSum: string;
    private labelLongest: string;
    private labelShortest: string;
    private labelMinCaseSensitive: string;
    private labelMaxCaseSensitive: string;
    private labelMinCaseInsensitive: string;
    private labelMaxCaseInsensitive: string;
    private labelTotal: string;
    private labelValid: string;
    private labelInvalid: string;
    private labelMissing: string;
    private loading = false;

    constructor(private $$angularInjector: Injector, private translateService: TranslateService, private http: HttpClient, private loadingService: TdLoadingService) {
        this.$timeout = $$angularInjector.get("$timeout");
        this.hiveService = $$angularInjector.get("HiveService");
        this.restUrlService = $$angularInjector.get("RestUrlService");

        this.labelNulls = this.translateService.instant("Profile.Stats.Nulls");
        this.labelUnique = this.translateService.instant("Profile.Stats.Unique");
        this.labelDuplicates = this.translateService.instant("Profile.Stats.Duplicates");
        this.labelMinimum = this.translateService.instant("Profile.Stats.Minimum");
        this.labelMaximum = this.translateService.instant("Profile.Stats.Maximum");
        this.labelMean = this.translateService.instant("Profile.Stats.Mean");
        this.labelStdDev = this.translateService.instant("Profile.Stats.StdDev");
        this.labelVariance = this.translateService.instant("Profile.Stats.Variance");
        this.labelSum = this.translateService.instant("Profile.Stats.Sum");
        this.labelLongest = this.translateService.instant("Profile.Stats.Longest");
        this.labelShortest = this.translateService.instant("Profile.Stats.Shortest");
        this.labelMinCaseSensitive = this.translateService.instant("Profile.Stats.MinCaseSensitive");
        this.labelMaxCaseSensitive = this.translateService.instant("Profile.Stats.MaxCaseSensitive");
        this.labelMinCaseInsensitive = this.translateService.instant("Profile.Stats.MinCaseInsensitive");
        this.labelMaxCaseInsensitive = this.translateService.instant("Profile.Stats.MaxCaseInsensitive");
        this.labelTotal = this.translateService.instant("Profile.Stats.Total");
        this.labelValid = this.translateService.instant("Profile.Stats.Valid") ;
        this.labelInvalid = this.translateService.instant("Profile.Stats.Invalid");
        this.labelMissing = this.translateService.instant("Profile.Stats.Missing");

        this.loadingService.create({
            name: ProfileStatsComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });

        /**
         * Options for the Relative Statistics chart.
         * @type {Object}
         */
        this.percOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                color: () => {
                    return this.CHART_COLOR;
                },
                height: 200,
                margin: {
                    top: 0,
                    right: this.multiBarHorizontalChartMarginRight, //otherwise large numbers are cut off
                    bottom: 0,
                    left: this.multiBarHorizontalChartMarginLeft //otherwise y axis labels are not visible
                },
                duration: this.chartDuration,
                x: (d: any) => {
                    return d.label;
                },
                y: (d: any) => {
                    return d.value;
                },
                showXAxis: true,
                showYAxis: false,
                showControls: false,
                showValues: true,
                showLegend: false,
                valueFormat: (n: any) => {
                    return d3.format(',.1f')(n) + " %";
                }
            }
        };
        this.columns = {
            columnName: "columnName",
            metricType: "metricType",
            metricValue: "metricValue"
        };
        /**
         * Options for the Summary chart.
         * @type {Object}
         */
        this.summaryOptions = {
            chart: {
                type: 'discreteBarChart',
                color: () => {
                    return this.CHART_COLOR;
                },
                height: 270,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 0,
                    bottom: 25, //otherwise bottom labels are not visible
                    left: 0
                },
                duration: this.chartDuration,
                x: (d: any) => {
                    return d.label;
                },
                y: (d: any) => {
                    return d.value + (1e-10);
                },
                showXAxis: true,
                showYAxis: false,
                showValues: true,
                xAxis: {
                    tickPadding: 10
                },
                valueFormat: (d: any) => {
                    return d3.format(',.0f')(d);
                }
            }
        };
    }

    ngOnInit(): void {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.active.currentValue && !this.activated) {
            //lazy loading, i.e. loading only when tab is opened for the first time
            this.activated = true;
            this.getProfileStats().then(this.onModelChange);
        }
    }

    private getProfileStats() {
        this.loading = true;
        this.loadingService.register(ProfileStatsComponent.topOfPageLoader);
        const successFn = (response: any) => {
            this.loadingService.resolve(ProfileStatsComponent.topOfPageLoader);
            this.loading = false;
            this.model = response;
        };
        const errorFn = (err: any) => {
            this.loadingService.resolve(ProfileStatsComponent.topOfPageLoader);
            this.loading = false;
        };
        const promise = this.http.get(this.restUrlService.FEED_PROFILE_STATS_URL(this.feedId), {params: {'processingdttm': this.processingdttm}}).toPromise();
        return promise.then(successFn, errorFn);
    };


    /**
     * Finds the metric value for the specified metric type.
     *
     * @param {Array.<Object>} rows the column statistics
     * @param {string} metricType the metric type
     * @returns {string} the metric value
     */
    findStat = (rows: any, metricType: any) => {
        var row = _.find(rows, (row) => {
            return row[this.columns.metricType] === metricType;
        });
        return (angular.isDefined(row) && angular.isDefined(row[this.columns.metricValue])) ? row[this.columns.metricValue] : "";
    };

    /**
     * Finds the numeric metric value for the specified metric type.
     *
     * @param {Array.<Object>} rows the column statistics
     * @param {string} metricType the metric type
     * @returns {number} the metric value
     */
    findNumericStat = (rows: any, metricType: any) => {
        var stat = this.findStat(rows, metricType);
        return stat === "" ? 0 : Number(stat);
    };

    /**
     * Formats the specified model row.
     *
     * @param {Array} row the model row
     * @param {Array.<string>} columns the list of column system names
     * @param {Array.<displayColumns>} displayColumns the list of column display names
     */
    formatRow = (row: any, columns: any, displayColumns: any) => {
        // Determine metric type
        var index = _.indexOf(displayColumns, this.columns.metricType);
        var metricType = row[columns[index]];

        // Modify value of 'Top N Values' metric
        if (metricType === "TOP_N_VALUES") {
            index = _.indexOf(displayColumns, this.columns.metricValue);
            var val = row[columns[index]];
            if (val) {
                var newVal = "";
                angular.forEach(val.split("^B"), (row: any) => {
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
    getClass = (item: any) => {
        return (item[this.columns.columnName] === this.selectedRow.columnName) ? "md-raised" : "";
    };

    /**
     * Indicates if the specified column has profile statistics.
     *
     * @param {Object} item the column
     * @returns {boolean} true if the column has profile statistics, or false otherwise
     */
    isProfiled = (item: any) => {
        if (_.isUndefined(item.isProfiled)) {
            var filtered = _.filter(this.data.rows, (row) => {
                return row[this.columns.columnName] === item[this.columns.columnName];
            });

            // anything profiled will have "COLUMN_DATATYPE"
            var type = this.findStat(filtered, 'COLUMN_DATATYPE');
            item.isProfiled = (type !== "");
        }
        return item.isProfiled;
    };

    /**
     * Updates the profile data with changes to the model.
     */
    onModelChange = () => {
        // Determine column names
        if (angular.isArray(this.model) && this.model.length > 0) {
            if (angular.isDefined(this.model[0].columnName)) {
                this.columns.columnName = "columnName";
                this.columns.metricType = "metricType";
                this.columns.metricValue = "metricValue";
            } else {
                this.columns.columnName = "columnname";
                this.columns.metricType = "metrictype";
                this.columns.metricValue = "metricvalue";
            }
        }

        // Process the model
        var hideColumnsVar = angular.isArray(this.hideColumns) ? this.hideColumns : [];
        const clone = JSON.parse(JSON.stringify(this.model));
        this.data = this.hiveService.transformResults2({ data: clone }, hideColumnsVar, this.formatRow);

        // Sort by column name
        if (angular.isArray(this.data.rows) && this.data.rows.length > 0) {
            var unique = _.uniq(this.data.rows, _.property(this.columns.columnName));
            this.sorted = _.sortBy(unique, _.property(this.columns.columnName));
            const self = this;
            this.sorted = _.filter(this.sorted, function(column) {
                return (!self.showOnlyProfiled || self.isProfiled(column))
            });
            if (this.sorted && this.sorted.length > 1) {
                //default to selecting other than (ALL) column - (ALL) column will be first, so we select second
                if (this.sorted[0][this.columns.columnName] == "(ALL)") {
                    this.selectRow(this.sorted[1]);
                } else {
                    this.selectRow(this.sorted[0]);
                }
            } else if (this.sorted && this.sorted.length > 0) {
                //fall back to selecting first column if no other exist
                this.selectRow(this.sorted[1]);
            }
        }

        // Determine total number of rows
        var allColumnData = _.filter(this.data.rows, (row) => {
            return row[this.columns.columnName] === this.ALL_COLUMN_NAME;
        });
        this.totalRows = this.findNumericStat(allColumnData, 'TOTAL_COUNT');

        // Update selected row
        if (angular.isString(this.selectedRow.columnName)) {
            var newSelectedRow = _.find(this.sorted, (row) => {
                return row[this.columns.columnName] === this.selectedRow.columnName;
            });
            this.selectRowAndUpdateCharts(null, newSelectedRow);
        }

        // // Ensure charts are the correct size
        if (this.nextChartUpdate !== null) {
            this.$timeout.cancel(this.nextChartUpdate);
        }

        this.nextChartUpdate = this.$timeout(() => {
            this.updateCharts();
            this.nextChartUpdate = null;
        }, this.chartDuration);
    };

    /**
     * Gets the data for the Relative Statistics graph.
     *
     * @returns {Array.<Object>} the graph data
     */
    getPercData = () => {
        var values = [];

        values.push({ label: this.labelNulls, value: this.findNumericStat(this.filtered, 'PERC_NULL_VALUES') });
        values.push({ label: this.labelUnique, value: this.findNumericStat(this.filtered, 'PERC_UNIQUE_VALUES') });
        values.push({ label: this.labelDuplicates, value: this.findNumericStat(this.filtered, 'PERC_DUPLICATE_VALUES') });

        return [{ key: "Stats", values: values }];
    };

    /**
     * Sets the selected row.
     *
     * @param {Object} row the selected row
     */
    selectColumn = (row: any) => {
        this.selectedRow.prevProfile = this.selectedRow.profile;
        this.selectedRow.columnName = row[this.columns.columnName];
    };

    /**
     * Sets the selected column data based on the selected row.
     */
    selectColumnData = () => {
        this.filtered = _.filter(this.data.rows, (row) => {
            return row[this.columns.columnName] === this.selectedRow.columnName;
        });
    };

    /**
     * Sets the values for the Numeric Stats table.
     */
    selectNumericValues = () => {
        var values: any = [];
        this.numericvalues = values;

        if (this.selectedRow.profile === "Numeric") {
            values.push({ "name": this.labelMinimum, "value": this.findNumericStat(this.filtered, 'MIN') });
            values.push({ "name": this.labelMaximum, "value": this.findNumericStat(this.filtered, 'MAX') });
            values.push({ "name": this.labelMean, "value": this.findNumericStat(this.filtered, 'MEAN') });
            values.push({ "name": this.labelStdDev, "value": this.findNumericStat(this.filtered, 'STDDEV') });
            values.push({ "name": this.labelVariance, "value": this.findNumericStat(this.filtered, 'VARIANCE') });
            values.push({ "name": this.labelSum, "value": this.findNumericStat(this.filtered, 'SUM') });
        }

    };

    isSelected(row: any): boolean {
        return this.selectedRow.columnName === row[this.columns.columnName];
    }

    /**
     * Selects the specified row.
     *
     * @param {Object} row the row to select
     */
    selectRow = (row: any) => {
        this.selectColumn(row);
        this.selectColumnData();
        this.selectType();
        this.selectTopValues();
        this.selectTimeValues();
        this.selectStringValues();
        this.selectNumericValues();
    };

    /**
     * Selects the specified row and updates charts.
     *
     * @param event
     * @param {Object} row the row to be selected
     */
    selectRowAndUpdateCharts = (event: any, row: any) => {
        //called when user selects the column
        this.selectRow(row);
        this.updateCharts();
    };

    /**
     * Sets the values for the String Stats table.
     */
    selectStringValues = () => {
        var vals: any = [];
        this.stringvalues = vals;
        if (this.selectedRow.profile === "String") {
            vals.push({ name: this.labelLongest, value: this.findStat(this.filtered, 'LONGEST_STRING') });
            vals.push({ name: this.labelShortest, value: this.findStat(this.filtered, 'SHORTEST_STRING') });
            vals.push({ name: this.labelMinCaseSensitive, value: this.findStat(this.filtered, 'MIN_STRING_CASE') });
            vals.push({ name: this.labelMaxCaseSensitive, value: this.findStat(this.filtered, 'MAX_STRING_CASE') });
            vals.push({ name: this.labelMinCaseInsensitive, value: this.findStat(this.filtered, 'MIN_STRING_ICASE') });
            vals.push({ name: this.labelMaxCaseInsensitive, value: this.findStat(this.filtered, 'MAX_STRING_ICASE') });
        }
    };

    /**
     * Sets the values for the Time Stats table.
     */
    selectTimeValues = () => {
        var timeVals: any = [];
        this.timevalues = timeVals;
        if (this.selectedRow.profile === "Time") {
            timeVals.push({ name: this.labelMaximum, value: this.findStat(this.filtered, 'MAX_TIMESTAMP') });
            timeVals.push({ name: this.labelMinimum, value: this.findStat(this.filtered, 'MIN_TIMESTAMP') });
        }
    };

    /**
     * Sets the values for the Top Values table.
     */
    selectTopValues = () => {
        var topN = this.findStat(this.filtered, 'TOP_N_VALUES');
        var topVals: any = [];
        if (_.isUndefined(topN)) {
            topVals = [];
        } else {
            var lines = topN.split("\n");
            topVals = _.map(lines, (line: any) => {
                var value = line.substring(line.indexOf(".") + 1, line.indexOf("("));
                var count = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                return { value: value, count: count };
            });
        }
        this.topvalues = topVals.slice(0, 10);
    };

    /**
     * Determines the type of the selected column.
     */
    selectType = () => {
        var type = this.findStat(this.filtered, 'COLUMN_DATATYPE');
        if (_.isUndefined(type)) {
            type = "UnknownType";
        }
        type = type.substring(0, type.indexOf("Type"));
        this.selectedRow.type = type;
        if (type === "String") {
            this.selectedRow.profile = "String";
        } else if (type === "Long" || type === "Double" || type === "Float" || type === "Byte" || type === "Integer" || type === "Decimal") {
            this.selectedRow.profile = "Numeric";
        } else if (type === "Timestamp" || type === "Date") {
            this.selectedRow.profile = "Time";
        } else {
            this.selectedRow.profile = "Unknown";
        }
    };

    /**
     * Gets the data for the Summary graph.
     *
     * @returns {Array.<Object>} the graph data
     */
    getSummaryData() {
        var nulls = this.findNumericStat(this.filtered, 'NULL_COUNT');
        var empty = this.findNumericStat(this.filtered, 'EMPTY_COUNT');
        var unique = this.findNumericStat(this.filtered, 'UNIQUE_COUNT');
        var invalid = this.findNumericStat(this.filtered, 'INVALID_COUNT');
        var valid = this.totalRows - invalid;

        //display negative values in red
        var color = this.CHART_COLOR;
        if (valid < 0) {
            color = "red";
        }

        var values = [];
        values.push({ "label": this.labelTotal, "value": this.totalRows });
        values.push({ "label": this.labelValid, "value": valid, "color": color });
        values.push({ "label": this.labelInvalid, "value": invalid });

        if (this.selectedRow.columnName !== '(ALL)') {
            values.push({ "label": this.labelUnique, "value": unique });
            values.push({ "label": this.labelMissing, "value": nulls + empty });
        }

        return [{ key: "Summary", values: values }];
    };

    /**
     * Updates the Summary and Relative Statistics charts.
     */
    updateCharts = () => {
        if (angular.isDefined(this.summaryApi.update)) {
            this.summaryApi.update();
        }
        if (angular.isDefined(this.percApi.update)) {
            this.percApi.update();
        }
        this.summaryData = this.getSummaryData();
        this.percData = this.getPercData();
    };


    showOnlyProfiledChange(): void {
        this.showOnlyProfiled = !this.showOnlyProfiled;
        this.onModelChange();
    }

}

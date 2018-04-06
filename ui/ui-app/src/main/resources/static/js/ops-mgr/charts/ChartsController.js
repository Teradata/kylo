define(["require", "exports", "angular", "./module-name", "./PivotTableUtil", "underscore", "moment"], function (require, exports, angular, module_name_1, PivotTableUtil_1, _, moment) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $http, HttpService, OpsManagerJobService, OpsManagerFeedService) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.HttpService = HttpService;
            this.OpsManagerJobService = OpsManagerJobService;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.removeAllFromArray = function (arr) {
                if (arr != null && arr.length > 0 && _.indexOf(arr, 'All') >= 0) {
                    return _.without(arr, 'All');
                }
                else {
                    return arr;
                }
            };
            this.refreshPivotTable = function () {
                var successFn = function (response) {
                    _this.responseData = response.data;
                    var data = response.data;
                    if (_this.responseData.length >= _this.limitRows && _this.filtered == true) {
                        _this.message = "Warning. Only returned the first " + _this.limitRows + " records. Either increase the limit or modify the filter.";
                        _this.isWarning = true;
                    }
                    else {
                        _this.message = 'Showing ' + data.length + ' jobs';
                    }
                    _this.loading = false;
                    _this.renderPivotTable(data);
                };
                var errorFn = function (err) {
                    console.log('error', err);
                };
                var finallyFn = function () {
                };
                var addToFilter = function (filterStr, addStr) {
                    if (filterStr != null && filterStr != "") {
                        filterStr += ",";
                    }
                    filterStr += addStr;
                    return filterStr;
                };
                var formParams = {};
                var startDateSet = false;
                var endDateSet = false;
                _this.filtered = false;
                formParams['limit'] = _this.limitRows;
                formParams['sort'] = '-executionid';
                var filter = "";
                if (!_.contains(_this.selectedFeedNames, 'All') && _this.selectedFeedNames.length > 0) {
                    filter = addToFilter(filter, "feedName==\"" + _this.selectedFeedNames.join(',') + "\"");
                    _this.filtered = true;
                }
                if (_this.startDate != null && _this.startDate !== '') {
                    var m = moment(_this.startDate);
                    var filterStr = 'startTimeMillis>' + m.toDate().getTime();
                    filter = addToFilter(filter, filterStr);
                    _this.filtered = true;
                    startDateSet = true;
                }
                if (_this.endDate != null && _this.endDate !== '') {
                    var m = moment(_this.endDate);
                    var filterStr = 'startTimeMillis<' + m.toDate().getTime();
                    filter = addToFilter(filter, filterStr);
                    _this.filtered = true;
                    endDateSet = true;
                }
                if (startDateSet && !endDateSet || startDateSet && endDateSet) {
                    formParams['sort'] = 'executionid';
                }
                formParams['filter'] = filter;
                $("#charts_tab_pivot_chart").html('<div class="bg-info"><i class="fa fa-refresh fa-spin"></i> Rendering Pivot Table...</div>');
                var rqst = _this.HttpService.newRequestBuilder(_this.OpsManagerJobService.JOBS_CHARTS_QUERY_URL).params(formParams).success(successFn).error(errorFn).finally(finallyFn).build();
                _this.currentRequest = rqst;
                _this.loading = true;
            };
            this.getFeedNames = function () {
                var successFn = function (response) {
                    if (response.data) {
                        _this.feedNames = _.unique(response.data);
                        _this.feedNames.unshift('All');
                    }
                };
                var errorFn = function (err) {
                };
                var finallyFn = function () {
                };
                _this.$http.get(_this.OpsManagerFeedService.FEED_NAMES_URL).then(successFn, errorFn);
            };
            this.renderPivotTable = function (tableData) {
                var hideColumns = ["exceptions", "executionContext", "jobParameters", "lastUpdated", "executedSteps", "jobConfigurationName", "executionId", "instanceId", "jobId", "latest", "exitStatus"];
                var pivotNameMap = {
                    "startTime": {
                        name: "Start Time", fn: function (val) {
                            return new Date(val);
                        }
                    },
                    "endTime": {
                        name: "End Time", fn: function (val) {
                            return new Date(val);
                        }
                    },
                    "runTime": {
                        name: "Duration (min)", fn: function (val) {
                            return val / 1000 / 60;
                        }
                    }
                };
                var pivotData = PivotTableUtil_1.default.transformToPivotTable(tableData, hideColumns, pivotNameMap);
                var renderers = $.extend($.pivotUtilities.renderers, $.pivotUtilities.c3_renderers);
                var derivers = $.pivotUtilities.derivers;
                var width = _this.getWidth();
                var height = _this.getHeight();
                $("#charts_tab_pivot_chart").pivotUI(pivotData, {
                    onRefresh: function (config) {
                        var config_copy = JSON.parse(JSON.stringify(config));
                        //delete some values which are functions
                        delete config_copy["aggregators"];
                        delete config_copy["renderers"];
                        delete config_copy["derivedAttributes"];
                        //delete some bulky default values
                        delete config_copy["rendererOptions"];
                        delete config_copy["localeStrings"];
                        _this.pivotConfig = config_copy;
                        _this.assignLabels();
                    },
                    renderers: renderers,
                    rendererOptions: { c3: { size: { width: width, height: height } } },
                    derivedAttributes: {
                        "Start Date": $.pivotUtilities.derivers.dateFormat("Start Time", "%y-%m-%d"),
                        "End Date": $.pivotUtilities.derivers.dateFormat("End Time", "%y-%m-%d"),
                        "Duration (sec)": function (mp) { return mp["Duration (min)"] * 60; }
                    },
                    rendererName: _this.pivotConfig.rendererName,
                    aggregatorName: _this.pivotConfig.aggregatorName,
                    vals: _this.pivotConfig.vals,
                    // rendererName: this.pivotConfig.rendererName,
                    cols: _this.pivotConfig.cols, rows: _this.pivotConfig.rows,
                    unusedAttrsVertical: _this.pivotConfig.unusedAttrsVertical
                }, true);
                _this.$scope.lastRefreshed = new Date();
            };
            this.getWidth = function () {
                var sideNav = $('md-sidenav').width();
                if ($('.toggle-side-nav').is(':visible')) {
                    sideNav = 0;
                }
                var rightCard = $('.filter-chart').width();
                return $(window).innerWidth() - (sideNav + 400) - rightCard;
            };
            this.getHeight = function () {
                var header = $('page-header').height();
                var height = $(window).innerHeight() - (header + 450);
                if (height < 400) {
                    height = 400;
                }
                return height;
            };
            this.onWindowResize = function () {
                $(window).on("resize.doResize", _.debounce(function () {
                    _this.$scope.$apply(function () {
                        if (_this.$scope.lastRefreshed) {
                            $("#charts_tab_pivot_chart").html('Rendering Chart ...');
                            _this.renderPivotTable(_this.responseData);
                        }
                    });
                }, 100));
            };
            this.assignLabels = function () {
                if ($('.pivot-label').length == 0) {
                    $('.pvtUi').find('tbody:first').prepend('<tr><td><div class="pivot-label accent-color-3">Chart Type</div></td><td><div class="pivot-label accent-color-3">Attributes (drag and drop to customize the chart)</div></td></tr>');
                    $('.pvtAggregator').parents('tr:first').before('<tr><td style="font-size:3px;">&nbsp;</td><td style="font-size:3px;">&nbsp;<td></tr>');
                    $('.pvtAggregator').parents('td:first').css('padding-bottom', '10px');
                    $('.pvtAggregator').before('<div class="pivot-label accent-color-3" style="padding-bottom:8px;">Aggregrator</div>');
                    $('.pvtRenderer').parent().css('vertical-align', 'top');
                    $('.pvtRenderer').parent().css('vertical-align', 'top');
                    var selectWidth = $('.pvtAggregator').width();
                    $('#charts_tab_pivot_chart').find('select').css('width', selectWidth);
                    $('.pvtCols').css('vertical-align', 'top');
                }
            };
            this.selectedFeedNames = ['All'];
            this.startDate = null;
            this.endDate = null;
            this.limitRows = 500;
            this.limitOptions = [200, 500, 1000, 5000, 10000];
            this.message = '';
            this.isWarning = false;
            this.filtered = false;
            this.loading = false;
            this.pivotConfig = {
                aggregatorName: "Average",
                vals: ["Duration (min)"],
                rendererName: "Stacked Bar Chart",
                cols: ["Start Date"], rows: ["Feed Name"],
                unusedAttrsVertical: false
            };
            this.refreshPivotTable();
            this.onWindowResize();
            this.getFeedNames();
            $scope.$on("$destroy", function () {
                $(window).off("resize.doResize"); //remove the handler added earlier
            });
        } // end of Constructor
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .controller('ChartsController', ["$scope", "$element", "$http", "HttpService", "OpsManagerJobService", "OpsManagerFeedService", controller]);
});
//# sourceMappingURL=ChartsController.js.map
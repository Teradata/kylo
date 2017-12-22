define(["require", "exports", "angular", "jquery", "underscore", "../services/wrangler-event-type", "./visual-query-painter.service", "./wrangler-table-model", "fattable"], function (require, exports, angular, $, _, wrangler_event_type_1, visual_query_painter_service_1, wrangler_table_model_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Maximum width of a column including padding.
     */
    var COLUMN_WIDTH_MAX = 300;
    /**
     * Minimum width of a column including padding.
     */
    var COLUMN_WIDTH_MIN = 150;
    /**
     * Width of the domain type icon.
     */
    var DOMAIN_TYPE_WIDTH = 30;
    /**
     * Width of the menu element in the header.
     */
    var MENU_WIDTH = 52;
    /**
     * Manages a data table for viewing the results of transformations.
     *
     * @constructor
     * @param $scope the application scope
     * @param $compile the compile service
     * @param $element the
     * @param $templateCache the template cache service
     * @param $templateRequest the template request service
     * @param $timeout the Angular timeout service
     * @param uiGridConstants the ui-grid constants
     */
    var VisualQueryTable = /** @class */ (function () {
        function VisualQueryTable($scope_, $element, $timeout_, painter, dataService, tableService, uiGridConstants_) {
            var _this = this;
            this.$scope_ = $scope_;
            this.$element = $element;
            this.$timeout_ = $timeout_;
            this.painter = painter;
            this.dataService = dataService;
            this.tableService = tableService;
            this.uiGridConstants_ = uiGridConstants_;
            /**
             * 2D rending context
             */
            this.canvasContext_ = null;
            /**
             * Width of the table at last refresh.
             */
            this.lastTableWidth_ = 0;
            /**
             * The table view.
             * @type {fattable.TableView}
             */
            this.table_ = null;
            this.painter.delegate = this;
            // Refresh table when model changes
            tableService.registerTable(function (event) {
                if (event.type === wrangler_event_type_1.WranglerEventType.REFRESH) {
                    _this.refresh();
                }
            });
            $scope_.$watchCollection(function () { return _this.columns; }, function () {
                _this.onColumnsChange();
                _this.refresh();
            });
            $scope_.$watchCollection(function () { return _this.domainTypes; }, function () {
                _this.painter.domainTypes = _this.domainTypes.sort(function (a, b) { return (a.title < b.title) ? -1 : 1; });
                _this.refresh();
            });
            $scope_.$watch(function () { return _this.options ? _this.options.headerFont : null; }, function () { return painter.headerFont = _this.options.headerFont; });
            $scope_.$watch(function () { return _this.options ? _this.options.rowFont : null; }, function () { return painter.rowFont = _this.options.rowFont; });
            $scope_.$watchCollection(function () { return _this.rows; }, function () {
                _this.onRowsChange();
            });
            $scope_.$watchCollection(function () { return _this.validationResults; }, function () {
                _this.onValidationResultsChange();
                _this.refresh();
            });
            var resizeTimeoutPromise = null;
            var resizeTimeout = function (callback, interval) {
                if (resizeTimeoutPromise != null) {
                    _this.$timeout_.cancel(resizeTimeoutPromise);
                }
                resizeTimeoutPromise = _this.$timeout_(callback, interval);
            };
            // Refresh table on resize
            $scope_.$watch(function () { return $element.height(); }, function () { return resizeTimeout(function () { return _this.refresh(); }, 500); });
            $scope_.$watch(function () { return $element.width(); }, function () { return resizeTimeout(function () { return _this.refresh(); }, 500); });
            // Listen for destroy event
            $scope_.$on("destroy", function () { return _this.$onDestroy(); });
        }
        VisualQueryTable.prototype.$onDestroy = function () {
            this.tableService.unsubscribe();
        };
        VisualQueryTable.prototype.$onInit = function () {
            this.onColumnsChange();
            this.rows = angular.copy(this.rows);
            this.init(this.$element);
        };
        /**
         * Initializes the table.
         *
         * @param {jQuery} element the table element
         */
        VisualQueryTable.prototype.init = function (element) {
            this.table_ = fattable({
                container: element.get(0),
                model: new wrangler_table_model_1.WranglerTableModel(this.dataService),
                nbRows: 0,
                rowHeight: visual_query_painter_service_1.VisualQueryPainterService.ROW_HEIGHT,
                headerHeight: visual_query_painter_service_1.VisualQueryPainterService.HEADER_HEIGHT,
                painter: this.painter,
                columnWidths: [0]
            });
            this.$timeout_(this.refresh.bind(this), 500);
        };
        /**
         * Redraws the table.
         */
        VisualQueryTable.prototype.refresh = function () {
            // Skip if table not initialized
            if (this.table_ === null) {
                return;
            }
            // Re-calculate column widths
            var widthDiff = Math.abs(this.lastTableWidth_ - $(this.table_.container).width());
            if (widthDiff > 1) {
                var columnWidths = this.getColumnWidths();
                this.table_.columnWidths = columnWidths;
                this.table_.nbCols = columnWidths.length;
                var columnOffset = _.reduce(columnWidths, function (memo, width) {
                    memo.push(memo[memo.length - 1] + width);
                    return memo;
                }, [0]);
                this.table_.columnOffset = columnOffset;
                this.table_.W = columnOffset[columnOffset.length - 1];
            }
            // Update table properties
            var rowCount = angular.isArray(this.dataService.rows_) ? this.dataService.rows_.length : 0;
            this.table_.nbRows = rowCount;
            this.table_.H = visual_query_painter_service_1.VisualQueryPainterService.ROW_HEIGHT * rowCount;
            // Rebuild table
            this.painter.hideTooltip();
            this.table_.setup();
        };
        /**
         * Refreshes the contents of rows.
         */
        VisualQueryTable.prototype.refreshRows = function () {
            this.table_.refreshAllContent(true);
        };
        /**
         * Sets the sorting on a given column, optionally resetting any existing sorting on the table.
         *
         * @param {VisualQueryTableHeader} header the column to set the sorting on
         * @param {VisualQueryTable.ASC|VisualQueryTable.DESC} direction the direction to sort by, either descending or ascending
         */
        VisualQueryTable.prototype.sortColumn = function (header, direction) {
            this.dataService.sortDirection_ = direction;
            this.dataService.sortIndex_ = header.index;
            this.onRowsChange();
        };
        /**
         * Removes sorting from the table.
         */
        VisualQueryTable.prototype.unsort = function () {
            this.dataService.sortDirection_ = null;
            this.dataService.sortIndex_ = null;
            this.onRowsChange();
        };
        /**
         * Gets a 2D rending context for calculating text width.
         *
         * @returns {CanvasRenderingContext2D} a 2D rendering context
         */
        VisualQueryTable.prototype.get2dContext = function () {
            if (this.canvasContext_ === null) {
                var canvas = document.createElement("canvas");
                document.createDocumentFragment().appendChild(canvas);
                this.canvasContext_ = canvas.getContext("2d");
                if (angular.isString(this.options.headerFont)) {
                    this.canvasContext_.font = this.options.headerFont;
                }
            }
            return this.canvasContext_;
        };
        /**
         * Calculates the width for every column.
         *
         * @returns {Array.<number>} the column widths
         */
        VisualQueryTable.prototype.getColumnWidths = function () {
            var _this = this;
            // Skip if no columns
            if (!angular.isArray(this.dataService.columns_) || this.dataService.columns_.length === 0) {
                return [];
            }
            // Determine column widths based on header size
            var context = this.get2dContext();
            context.font = this.painter.headerFont;
            var headerWidths = this.dataService.columns_.map(function (column, index) {
                var textWidth = Math.max(context.measureText(column.displayName).width, context.measureText(column.dataType).width);
                var padding = (index === 0) ? visual_query_painter_service_1.VisualQueryPainterService.COLUMN_PADDING_FIRST : visual_query_painter_service_1.VisualQueryPainterService.COLUMN_PADDING * 2;
                var menuWidth = (_this.domainTypes ? DOMAIN_TYPE_WIDTH : 0) + (index === 0 ? MENU_WIDTH * 1.5 : MENU_WIDTH);
                return Math.ceil(textWidth + padding + menuWidth);
            });
            // Determine column widths based on row sampling
            context.font = this.painter.rowFont;
            var rowWidths = _.map(this.dataService.columns_, function (column, index) {
                var textWidth = (column.longestValue != null) ? context.measureText(column.longestValue).width : 0;
                var padding = (index === 0) ? visual_query_painter_service_1.VisualQueryPainterService.COLUMN_PADDING_FIRST : visual_query_painter_service_1.VisualQueryPainterService.COLUMN_PADDING * 2;
                return Math.ceil(textWidth + padding);
            });
            // Calculate total width
            var columnWidths = [];
            var totalWidth = 0;
            for (var i = 0; i < this.dataService.columns_.length; ++i) {
                var width = Math.min(Math.max(headerWidths[i], rowWidths[i], COLUMN_WIDTH_MIN), COLUMN_WIDTH_MAX);
                columnWidths.push(width);
                totalWidth += width;
            }
            // Fit column widths to viewable width
            var padding = Math.max($(this.table_.container).width() - totalWidth, 0);
            return _.map(columnWidths, function (width) {
                return Math.floor(width + padding * width / totalWidth);
            });
        };
        /**
         * Applies filters to columns.
         */
        VisualQueryTable.prototype.onColumnsChange = function () {
            // Update properties
            _.each(this.columns, function (column) {
                column.visible = (column.visible !== false);
            });
            // Filter columns
            this.dataService.columns_ = _.filter(this.columns, function (column) {
                return (column.visible !== false);
            });
            // Update rows
            this.onRowsChange();
        };
        /**
         * Sorts and applies filters to rows.
         */
        VisualQueryTable.prototype.onRowsChange = function () {
            var self = this;
            // Filter rows
            this.dataService.rows_ = _.filter(this.rows, function (row) {
                return _.every(self.dataService.columns_, function (column, index) {
                    return _.every(column.filters, function (filter) {
                        if (angular.isUndefined(filter.term) || filter.term === null) {
                            return true;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.CONTAINS) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp(filter.term);
                            }
                            return filter.regex.test(row[index]);
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.LESS_THAN) {
                            return row[index] < filter.term;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.GREATER_THAN) {
                            return row[index] > filter.term;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.EXACT) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp("^" + filter.term + "$");
                            }
                            return filter.regex.test(row[index]);
                        }
                        else {
                            return false;
                        }
                    });
                });
            });
            // Sort rows
            if (angular.isNumber(this.dataService.sortIndex_) && this.dataService.sortIndex_ < this.dataService.columns_.length) {
                var column_1 = this.dataService.sortIndex_;
                var lessThan_1 = (this.dataService.sortDirection_ === VisualQueryTable.ASC) ? -1 : 1;
                var greaterThan_1 = -lessThan_1;
                this.dataService.rows_.sort(function (a, b) {
                    if (a[column_1] === b[column_1]) {
                        return 0;
                    }
                    else {
                        return (a[column_1] < b[column_1]) ? lessThan_1 : greaterThan_1;
                    }
                });
            }
        };
        VisualQueryTable.prototype.onValidationResultsChange = function () {
            this.dataService.validationResults = this.validationResults;
        };
        VisualQueryTable.$inject = ["$scope", "$element", "$timeout", "VisualQueryPainterService", "WranglerDataService", "WranglerTableService", "uiGridConstants"];
        /**
         * Indicates a column should be sorted in ascending order.
         */
        VisualQueryTable.ASC = "asc";
        /**
         * Indicates a column should be sorted in descending order.
         */
        VisualQueryTable.DESC = "desc";
        return VisualQueryTable;
    }());
    exports.VisualQueryTable = VisualQueryTable;
    angular.module(moduleName).directive("visualQueryTable", function () {
        return {
            bindToController: {
                columns: "=*tableColumns",
                domainTypes: "=*tableDomainTypes",
                options: "=*tableOptions",
                rows: "=*tableRows",
                validationResults: "=*tableValidation"
            },
            controller: VisualQueryTable,
            restrict: "E",
            link: function ($scope, element, attrs, controller) {
                controller.$onInit();
            }
        };
    });
});
//# sourceMappingURL=visual-query-table.component.js.map
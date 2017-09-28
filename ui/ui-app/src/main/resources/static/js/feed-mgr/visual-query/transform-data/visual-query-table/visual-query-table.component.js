define(["require", "exports", "../services/wrangler-event-type", "./wrangler-table-model", "fattable"], function (require, exports, wrangler_event_type_1, wrangler_table_model_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Left and right padding for normal columns.
     */
    var COLUMN_PADDING = 28;
    /**
     * Left padding for the first column.
     */
    var COLUMN_PADDING_FIRST = 24;
    /**
     * Maximum width of a column including padding.
     */
    var COLUMN_WIDTH_MAX = 300;
    /**
     * Minimum width of a column including padding.
     */
    var COLUMN_WIDTH_MIN = 150;
    /**
     * Default font.
     */
    var DEFAULT_FONT = "10px sans-serif";
    /**
     * Height of header row.
     */
    var HEADER_HEIGHT = 56;
    /**
     * HTML template for header cells.
     */
    var HEADER_TEMPLATE = "js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html";
    /**
     * Width of the menu element in the header.
     */
    var MENU_WIDTH = 52;
    /**
     * Pixel unit.
     */
    var PIXELS = "px";
    /**
     * Height of data rows.
     */
    var ROW_HEIGHT = 48;
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
    var VisualQueryTable = (function () {
        function VisualQueryTable($scope_, $compile_, $element, $templateCache_, $templateRequest_, $timeout_, dataService, tableService, uiGridConstants_) {
            this.$scope_ = $scope_;
            this.$compile_ = $compile_;
            this.$element = $element;
            this.$templateCache_ = $templateCache_;
            this.$templateRequest_ = $templateRequest_;
            this.$timeout_ = $timeout_;
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
            var self = this;
            // Refresh table when model changes
            var onColumnsChange = angular.bind(this, this.onColumnsChange);
            var onRowsChange = angular.bind(this, this.onRowsChange);
            var refresh = angular.bind(this, this.refresh);
            tableService.registerTable(function (event) {
                if (event.type === wrangler_event_type_1.WranglerEventType.REFRESH) {
                    self.refresh();
                }
            });
            $scope_.$watch(function () {
                return self.columns;
            }, function () {
                onColumnsChange();
                refresh();
            });
            $scope_.$watch(function () {
                return self.rows;
            }, function () {
                onRowsChange();
                refresh();
            });
            // Refresh table on resize
            $scope_.$watch(function () { return $element.height(); }, refresh);
            $scope_.$watch(function () { return $element.width(); }, refresh);
            // Listen for destroy event
            $scope_.$on("destroy", function () { return self.$onDestroy(); });
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
         * Gets the font for the header row.
         *
         * @returns {string}
         */
        VisualQueryTable.prototype.getHeaderFont = function () {
            return (angular.isObject(this.options) && angular.isString(this.options.headerFont)) ? this.options.headerFont : DEFAULT_FONT;
        };
        /**
         * Gets the font for the data rows.
         *
         * @returns {string}
         */
        VisualQueryTable.prototype.getRowFont = function () {
            return (angular.isObject(this.options) && angular.isString(this.options.rowFont)) ? this.options.rowFont : DEFAULT_FONT;
        };
        /**
         * Initializes the table.
         *
         * @param {jQuery} element the table element
         */
        VisualQueryTable.prototype.init = function (element) {
            var self = this;
            this.$templateRequest_(HEADER_TEMPLATE)
                .then(function () {
                self.table_ = fattable({
                    container: element.get(0),
                    model: new wrangler_table_model_1.WranglerTableModel(self.dataService),
                    nbRows: 0,
                    rowHeight: ROW_HEIGHT,
                    headerHeight: HEADER_HEIGHT,
                    painter: self,
                    columnWidths: [0]
                });
                self.$timeout_(angular.bind(self, self.refresh), 500);
            });
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
            this.table_.H = ROW_HEIGHT * rowCount;
            // Rebuild table
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
         * @private
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
         * @private
         * @returns {Array.<number>} the column widths
         */
        VisualQueryTable.prototype.getColumnWidths = function () {
            // Skip if no columns
            if (!angular.isArray(this.dataService.columns_) || this.dataService.columns_.length === 0) {
                return [];
            }
            // Determine column widths based on header size
            var context = this.get2dContext();
            context.font = this.getHeaderFont();
            var headerWidths = _.map(this.dataService.columns_, function (column, index) {
                var textWidth = context.measureText(column.displayName).width;
                var padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
                return Math.ceil(textWidth + padding + MENU_WIDTH);
            });
            // Determine column widths based on row sampling
            context.font = this.getRowFont();
            var rowWidths = _.map(this.dataService.columns_, function (column, index) {
                var textWidth = (column.longestValue != null) ? context.measureText(column.longestValue).width : 0;
                var padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
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
                return _.every(self.dataService.columns_, function (column) {
                    return _.every(column.filters, function (filter) {
                        if (angular.isUndefined(filter.term) || filter.term === null) {
                            return true;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.CONTAINS) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp(filter.term);
                            }
                            return filter.regex.test(row[column.name]);
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.LESS_THAN) {
                            return row[column.name] < filter.term;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.GREATER_THAN) {
                            return row[column.name] > filter.term;
                        }
                        else if (filter.condition === self.uiGridConstants_.filter.EXACT) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp("^" + filter.term + "$");
                            }
                            return filter.regex.test(row[column.name]);
                        }
                        else {
                            return false;
                        }
                    });
                });
            });
            // Sort rows
            if (angular.isNumber(this.dataService.sortIndex_) && this.dataService.sortIndex_ < this.dataService.columns_.length) {
                var field_1 = this.dataService.columns_[this.dataService.sortIndex_].name;
                var lessThan_1 = (this.dataService.sortDirection_ === VisualQueryTable.ASC) ? -1 : 1;
                var greaterThan_1 = -lessThan_1;
                this.dataService.rows_.sort(function (a, b) {
                    if (a[field_1] === b[field_1]) {
                        return 0;
                    }
                    else {
                        return (a[field_1] < b[field_1]) ? lessThan_1 : greaterThan_1;
                    }
                });
            }
        };
        return VisualQueryTable;
    }());
    /**
     * Indicates a column should be sorted in ascending order.
     */
    VisualQueryTable.ASC = "asc";
    /**
     * Indicates a column should be sorted in descending order.
     */
    VisualQueryTable.DESC = "desc";
    exports.VisualQueryTable = VisualQueryTable;
    angular.extend(VisualQueryTable.prototype, fattable.Painter.prototype, fattable.SyncTableModel.prototype, {
        /**
         * Will be called whenever a cell is put out of the DOM.
         *
         * @param {HTMLElement} cellDiv the cell <div> element
         */
        cleanUpCell: function (cellDiv) {
        },
        /**
         * Will be called whenever a column is put out of the DOM.
         *
         * @param {HTMLElement} headerDiv the header <div> element
         */
        cleanUpHeader: function (headerDiv) {
        },
        /**
         * Fills and style a cell div.
         *
         * @param {HTMLElement} cellDiv the cell <div> element
         * @param {VisualQueryTableCell|null} cell the cell object
         */
        fillCell: function (cellDiv, cell) {
            // Adjust padding based on column number
            if (cell !== null && cell.column === 0) {
                cellDiv.style.paddingLeft = COLUMN_PADDING_FIRST + PIXELS;
                cellDiv.style.paddingRight = 0 + PIXELS;
            }
            else {
                cellDiv.style.paddingLeft = COLUMN_PADDING + PIXELS;
                cellDiv.style.paddingRight = COLUMN_PADDING + PIXELS;
            }
            // Set contents
            if (cell === null) {
                cellDiv.textContent = "";
                cellDiv.title = "";
            }
            else if (cell.value !== null && cell.value.sqltypeName && cell.value.sqltypeName.startsWith("PERIOD")) {
                var value = "(" + cell.value.attributes.join(", ") + ")";
                cellDiv.textContent = value;
                cellDiv.title = value;
            }
            else {
                cellDiv.textContent = cell.value;
                cellDiv.title = cell.value;
            }
        },
        /**
         * Fills and style a column div.
         *
         * @param {HTMLElement} headerDiv the header <div> element
         * @param {VisualQueryTableHeader|null} header the column header
         */
        fillHeader: function (headerDiv, header) {
            // Adjust padding based on column number
            if (header !== null && header.index === 0) {
                headerDiv.style.paddingLeft = COLUMN_PADDING_FIRST + PIXELS;
                headerDiv.style.paddingRight = 0 + PIXELS;
            }
            else {
                headerDiv.style.paddingLeft = COLUMN_PADDING + PIXELS;
                headerDiv.style.paddingRight = COLUMN_PADDING + PIXELS;
            }
            // Update scope in a separate thread
            var $scope = angular.element(headerDiv).scope();
            var self = this;
            if ($scope.header !== header) {
                $scope.header = header;
                $scope.header.unsort = this.unsort.bind(this);
                $scope.table = self;
            }
        },
        /**
         * Setup method are called at the creation of the cells. That is during initialization and for all window resize event.
         *
         * Cells are recycled.
         *
         * @param {HTMLElement} cellDiv the cell <div> element
         */
        setupCell: function (cellDiv) {
            cellDiv.style.font = this.getRowFont();
            cellDiv.style.lineHeight = ROW_HEIGHT + PIXELS;
        },
        /**
         * Setup method are called at the creation of the column header. That is during initialization and for all window resize event.
         *
         * Columns are recycled.
         *
         * @param {HTMLElement} headerDiv the header <div> element
         */
        setupHeader: function (headerDiv) {
            // Set style attributes
            headerDiv.style.font = this.getHeaderFont();
            headerDiv.style.lineHeight = HEADER_HEIGHT + PIXELS;
            // Load template
            headerDiv.innerHTML = this.$templateCache_.get(HEADER_TEMPLATE);
            this.$compile_(headerDiv)(this.$scope_.$new(true));
        }
    });
    angular.module(moduleName).directive("visualQueryTable", function () {
        return {
            bindToController: {
                columns: "=*tableColumns",
                options: "=*tableOptions",
                rows: "=*tableRows"
            },
            controller: ["$scope", "$compile", "$element", "$templateCache", "$templateRequest", "$timeout", "WranglerDataService", "WranglerTableService", "uiGridConstants", VisualQueryTable],
            restrict: "E",
            link: function ($scope, element, attrs, controller) {
                controller.$onInit();
            }
        };
    });
});
//# sourceMappingURL=visual-query-table.component.js.map
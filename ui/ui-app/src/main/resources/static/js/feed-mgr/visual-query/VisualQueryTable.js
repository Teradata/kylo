define(["angular", "feed-mgr/visual-query/module-name", "fattable"], function (angular, moduleName) {

    /**
     * @typedef {Object} VisualQueryTableCell
     * @property {number} column
     * @property {number} row
     * @property {(*|null)} value
     */

    /**
     * @typedef {Object} VisualQueryTableHeader
     * @property {string} displayName
     * @property {number} index
     * @property {string} sort
     * @property {boolean} visible
     */

    /**
     * Left and right padding for normal columns.
     * @type {number}
     */
    var COLUMN_PADDING = 28;

    /**
     * Left padding for the first column.
     * @type {number}
     */
    var COLUMN_PADDING_FIRST = 24;

    /**
     * Maximum width of a column including padding.
     * @type {number}
     */
    var COLUMN_WIDTH_MAX = 300;

    /**
     * Minimum width of a column including padding.
     * @type {number}
     */
    var COLUMN_WIDTH_MIN = 150;

    /**
     * Default font.
     * @type {string}
     */
    var DEFAULT_FONT = "10px sans-serif";

    /**
     * Height of header row.
     * @type {number}
     */
    var HEADER_HEIGHT = 56;

    /**
     * HTML template for header cells.
     * @type {string}
     */
    var HEADER_TEMPLATE = "js/feed-mgr/visual-query/visual-query-table-header.html";

    /**
     * Width of the menu element in the header.
     * @type {number}
     */
    var MENU_WIDTH = 52;

    /**
     * Pixel unit.
     * @type {string}
     */
    var PIXELS = "px";

    /**
     * Height of data rows.
     * @type {number}
     */
    var ROW_HEIGHT = 48;

    /**
     * Manages a data table for viewing the results of transformations.
     *
     * @constructor
     * @param $scope the application scope
     * @param $compile the compile service
     * @param $templateCache the template cache service
     * @param $templateRequest the template request service
     * @param $timeout the Angular timeout service
     * @param uiGridConstants the ui-grid constants
     */
    function VisualQueryTable($scope, $compile, $templateCache, $templateRequest, $timeout, uiGridConstants) {
        var self = this;

        /**
         * The columns in this table.
         * @type {Array.<Object>}
         */
        this.columns = angular.isDefined(this.columns) ? this.columns : [];

        /**
         * The table options.
         * @type {Object} options
         * @type {string} [options.headerFont] the font for the header row
         * @type {string} [options.rowFont] the font for the data rows
         */
        this.options = angular.isDefined(this.options) ? this.options : {};

        /**
         * The data rows in this table.
         * @type {Array.<Object>}
         */
        this.rows = angular.isDefined(this.rows) ? this.rows : [];

        /**
         * The compile service.
         * @private
         */
        this.$compile_ = $compile;

        /**
         * The application scope for this instance.
         * @private
         */
        this.$scope_ = $scope;

        /**
         * The template cache service.
         * @private
         */
        this.$templateCache_ = $templateCache;

        /**
         * The template request service.
         * @private
         */
        this.$templateRequest_ = $templateRequest;

        /**
         * Angular timeout service
         * @private
         * @type {Function}
         */
        this.$timeout_ = $timeout;

        /**
         * 2D rending context
         * @private
         * @type {CanvasRenderingContext2D}
         */
        this.canvasContext_ = null;

        /**
         * The columns in this table with filters applied.
         * @private
         * @type {Array.<Object>}
         */
        this.columns_ = null;
        this.onColumnsChange();

        /**
         * Width of the table at last refresh.
         * @private
         * @type {number}
         */
        this.lastTableWidth_ = 0;

        /**
         * The data rows in this table with filters and sorting applied.
         * @private
         * @type {Array.<Object>}
         */
        this.rows_ = angular.copy(this.rows);

        /**
         * The sort direction.
         * @private
         * @type {VisualQueryTable.ASC|VisualQueryTable.DESC|null}
         */
        this.sortDirection_ = null;

        /**
         * The index of the column being sorted.
         * @private
         * @type {number|null}
         */
        this.sortIndex_ = null;

        /**
         * The table view.
         * @private
         * @type {fattable.TableView}
         */
        this.table_ = null;

        /**
         * Ui-grid constants.
         * @private
         */
        this.uiGridConstants_ = uiGridConstants;

        // Refresh table when model changes
        var onColumnsChange = angular.bind(this, this.onColumnsChange);
        var onRowsChange = angular.bind(this, this.onRowsChange);
        var refresh = angular.bind(this, this.refresh);

        $scope.$watch(function () {
            return self.columns;
        }, function () {
            onColumnsChange();
            refresh();
        });

        $scope.$watch(function () {
            return self.rows
        }, function () {
            onRowsChange();
            refresh();
        });

        // Refresh table on resize
        $(window).on("resize", refresh);
        $scope.$on("$destroy", function () {
            $(window).off("resize", refresh)
        })
    }

    angular.extend(VisualQueryTable, {
        /**
         * Indicates a column should be sorted in ascending order.
         * @type {string}
         */
        ASC: "asc",

        /**
         * Indicates a column should be sorted in descending order.
         * @type {string}
         */
        DESC: "desc"
    });

    //noinspection JSUnusedGlobalSymbols
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
            } else {
                cellDiv.style.paddingLeft = COLUMN_PADDING + PIXELS;
                cellDiv.style.paddingRight = COLUMN_PADDING + PIXELS;
            }

            // Set contents
            if (cell === null) {
                cellDiv.textContent = "";
                cellDiv.title = "";
            } else if (cell.value.sqltypeName && cell.value.sqltypeName.startsWith("PERIOD")) {
                var value = "(" + cell.value.attributes.join(", ") + ")";
                cellDiv.textContent = value;
                cellDiv.title = value;
            } else {
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
            } else {
                headerDiv.style.paddingLeft = COLUMN_PADDING + PIXELS;
                headerDiv.style.paddingRight = COLUMN_PADDING + PIXELS;
            }

            // Update scope in a separate thread
            var $scope = angular.element(headerDiv).scope();
            var self = this;

            if ($scope.header !== header) {
                $scope.header = header;
                $scope.table = self;
            }
        },

        /**
         * Gets the value for the specified cell.
         *
         * @param {number} i the row number
         * @param {number} j the column number
         * @returns {VisualQueryTableCell|null} the cell object
         */
        getCellSync: function (i, j) {
            var column = this.columns_[j];
            if (i >= 0 && i < this.rows_.length) {
                return {
                    column: j,
                    row: i,
                    value: this.rows_[i][column.name]
                };
            } else {
                return null;
            }
        },

        /**
         * Gets the header of the specified column.
         *
         * @param {number} j the column number
         * @returns {VisualQueryTableHeader|null} the column header
         */
        getHeaderSync: function (j) {
            if (j >= 0 && j < this.columns_.length) {
                var self = this;
                return angular.extend(this.columns_[j], {
                    field: this.columns_[j].name,
                    index: j,
                    sort: {
                        direction: (this.sortIndex_ === j) ? this.sortDirection_ : null
                    },
                    unsort: angular.bind(self, self.unsort)
                });
            } else {
                return null;
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

    angular.extend(VisualQueryTable.prototype, {

        /**
         * Gets the font for the header row.
         *
         * @returns {string}
         */
        getHeaderFont: function () {
            return (angular.isObject(this.options) && angular.isString(this.options.headerFont)) ? this.options.headerFont : DEFAULT_FONT;
        },

        /**
         * Gets the font for the data rows.
         *
         * @returns {string}
         */
        getRowFont: function () {
            return (angular.isObject(this.options) && angular.isString(this.options.rowFont)) ? this.options.rowFont : DEFAULT_FONT;
        },

        /**
         * Initializes the table.
         *
         * @param {jQuery} element the table element
         */
        init: function (element) {
            var self = this;
            this.$templateRequest_(HEADER_TEMPLATE)
                .then(function () {
                    self.table_ = fattable({
                        container: element.get(0),
                        model: self,
                        nbRows: 0,
                        rowHeight: ROW_HEIGHT,
                        headerHeight: HEADER_HEIGHT,
                        painter: self,
                        columnWidths: [0]
                    });

                    self.$timeout_(angular.bind(self, self.refresh), 500);
                });
        },

        /**
         * Redraws the table.
         */
        refresh: function () {
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
            var rowCount = angular.isArray(this.rows_) ? this.rows_.length : 0;
            this.table_.nbRows = rowCount;
            this.table_.H = ROW_HEIGHT * rowCount;

            // Rebuild table
            this.table_.setup();
        },

        /**
         * Refreshes the contents of rows.
         */
        refreshRows: function () {
            this.table_.refreshAllContent(true);
        },

        /**
         * Sets the sorting on a given column, optionally resetting any existing sorting on the table.
         *
         * @param {VisualQueryTableHeader} header the column to set the sorting on
         * @param {VisualQueryTable.ASC|VisualQueryTable.DESC} direction the direction to sort by, either descending or ascending
         */
        sortColumn: function (header, direction) {
            this.sortDirection_ = direction;
            this.sortIndex_ = header.index;
            this.onRowsChange();
        },

        /**
         * Removes sorting from the table.
         */
        unsort: function () {
            this.sortDirection_ = null;
            this.sortIndex_ = null;
            this.onRowsChange();
        },

        /**
         * Gets a 2D rending context for calculating text width.
         *
         * @private
         * @returns {CanvasRenderingContext2D} a 2D rendering context
         */
        get2dContext: function () {
            if (this.canvasContext_ === null) {
                var canvas = document.createElement("canvas");
                document.createDocumentFragment().appendChild(canvas);

                this.canvasContext_ = canvas.getContext("2d");
                if (angular.isString(this.options.headerFont)) {
                    this.canvasContext_.font = this.options.headerFont;
                }
            }
            return this.canvasContext_;
        },

        /**
         * Calculates the width for every column.
         *
         * @private
         * @returns {Array.<number>} the column widths
         */
        getColumnWidths: function () {
            // Skip if no columns
            if (!angular.isArray(this.columns_) || this.columns_.length === 0) {
                return [];
            }

            // Determine column widths based on header size
            var context = this.get2dContext();
            context.font = this.getHeaderFont();
            var self = this;

            var headerWidths = _.map(this.columns_, function (column, index) {
                var textWidth = context.measureText(column.displayName).width;
                var padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
                return Math.ceil(textWidth + padding + MENU_WIDTH);
            });

            // Determine column widths based on row sampling
            context.font = this.getRowFont();

            var rowWidths = _.map(this.columns_, function (column, index) {
                var textWidth = (column.longestValue != null) ? context.measureText(column.longestValue).width : 0;
                var padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
                return Math.ceil(textWidth + padding);
            });

            // Calculate total width
            var columnWidths = [];
            var totalWidth = 0;

            for (var i = 0; i < this.columns_.length; ++i) {
                var width = Math.min(Math.max(headerWidths[i], rowWidths[i], COLUMN_WIDTH_MIN), COLUMN_WIDTH_MAX);
                columnWidths.push(width);
                totalWidth += width;
            }

            // Fit column widths to viewable width
            var padding = Math.max($(this.table_.container).width() - totalWidth, 0);

            return _.map(columnWidths, function (width) {
                return Math.floor(width + padding * width / totalWidth);
            });
        },

        /**
         * Applies filters to columns.
         */
        onColumnsChange: function () {
            // Update properties
            _.each(this.columns, function (column) {
                column.visible = (column.visible !== false);
            });

            // Filter columns
            this.columns_ = _.filter(this.columns, function (column) {
                return (column.visible !== false);
            });

            // Update rows
            this.onRowsChange();
        },

        /**
         * Sorts and applies filters to rows.
         */
        onRowsChange: function () {
            var self = this;

            // Filter rows
            this.rows_ = _.filter(this.rows, function (row) {
                return _.every(self.columns_, function (column) {
                    return _.every(column.filters, function (filter) {
                        if (angular.isUndefined(filter.term) || filter.term === null) {
                            return true;
                        } else if (filter.condition === self.uiGridConstants_.filter.CONTAINS) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp(filter.term);
                            }
                            return filter.regex.test(row[column.name]);
                        } else if (filter.condition === self.uiGridConstants_.filter.LESS_THAN) {
                            return row[column.name] < filter.term;
                        } else if (filter.condition === self.uiGridConstants_.filter.GREATER_THAN) {
                            return row[column.name] > filter.term;
                        } else if (filter.condition === self.uiGridConstants_.filter.EXACT) {
                            if (angular.isUndefined(filter.regex)) {
                                filter.regex = new RegExp("^" + filter.term + "$");
                            }
                            return filter.regex.test(row[column.name]);
                        } else {
                            return false;
                        }
                    });
                });
            });

            // Sort rows
            if (angular.isNumber(this.sortIndex_) && this.sortIndex_ < this.columns_.length) {
                var field = this.columns_[this.sortIndex_].name;
                var lessThan = (this.sortDirection_ === VisualQueryTable.ASC) ? -1 : 1;
                var greaterThan = -lessThan;

                this.rows_.sort(function (a, b) {
                    if (a[field] === b[field]) {
                        return 0;
                    } else {
                        return (a[field] < b[field]) ? lessThan : greaterThan;
                    }
                });
            }
        }
    });

    angular.module(moduleName).controller("VisualQueryTable", ["$scope", "$compile", "$templateCache", "$templateRequest", "$timeout", "uiGridConstants", VisualQueryTable]);
    angular.module(moduleName).directive("visualQueryTable", function () {
        return {
            bindToController: {
                columns: "=*tableColumns",
                options: "=*tableOptions",
                rows: "=*tableRows"
            },
            controller: "VisualQueryTable",
            restrict: "E",
            link: function ($scope, element, attrs, controller) {
                controller.init(element);
            }
        };
    });
});

import {IAngularStatic, IAugmentedJQuery, ICompileService, IScope, ITemplateCacheService, ITemplateRequestService, ITimeoutService} from "angular";
import "fattable";
import {UnderscoreStatic} from "underscore";
import {WranglerTableService} from "../services/wrangler-table.service";
import {WranglerEventType} from "../services/wrangler-event-type";
import {WranglerDataService} from "../services/wrangler-data.service";
import {WranglerTableModel} from "./wrangler-table-model";

declare const _: UnderscoreStatic;
declare const $: JQueryStatic;
declare const angular: IAngularStatic;

const moduleName: string = require("feed-mgr/visual-query/module-name");

export interface VisualQueryTableCell {
    column: number;
    row: number;
    value: any;
}

export interface VisualQueryTableHeader {
    displayName: string;
    index: number;
    sort: string;
    visible: boolean;
}

/**
 * Left and right padding for normal columns.
 */
const COLUMN_PADDING = 28;

/**
 * Left padding for the first column.
 */
const COLUMN_PADDING_FIRST = 24;

/**
 * Maximum width of a column including padding.
 */
const COLUMN_WIDTH_MAX = 300;

/**
 * Minimum width of a column including padding.
 */
const COLUMN_WIDTH_MIN = 150;

/**
 * Default font.
 */
const DEFAULT_FONT = "10px sans-serif";

/**
 * Height of header row.
 */
const HEADER_HEIGHT = 56;

/**
 * HTML template for header cells.
 */
const HEADER_TEMPLATE = "js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html";

/**
 * Width of the menu element in the header.
 */
const MENU_WIDTH = 52;

/**
 * Pixel unit.
 */
const PIXELS = "px";

/**
 * Height of data rows.
 */
const ROW_HEIGHT = 48;

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
export class VisualQueryTable {

    /**
     * Indicates a column should be sorted in ascending order.
     */
    static readonly ASC = "asc";

    /**
     * Indicates a column should be sorted in descending order.
     */
    static readonly DESC = "desc";

    /**
     * The columns in this table.
     * @type {Array.<Object>}
     */
    columns: object[];

    /**
     * The table options.
     * @type {Object} options
     * @type {string} [options.headerFont] the font for the header row
     * @type {string} [options.rowFont] the font for the data rows
     */
    options: {headerFont?: string, rowFont?: string};

    /**
     * The data rows in this table.
     */
    rows: object[];

    /**
     * 2D rending context
     */
    private canvasContext_: CanvasRenderingContext2D = null;

    /**
     * Width of the table at last refresh.
     */
    private lastTableWidth_ = 0;

    /**
     * The table view.
     * @type {fattable.TableView}
     */
    private table_: any = null;

    constructor (private $scope_: IScope, private $compile_: ICompileService, private $element: any, private $templateCache_: ITemplateCacheService, private $templateRequest_: ITemplateRequestService,
                 private $timeout_: ITimeoutService, private dataService: WranglerDataService, private tableService: WranglerTableService, private uiGridConstants_: any) {
        const self = this;

        // Refresh table when model changes
        const onColumnsChange = angular.bind(this, this.onColumnsChange);
        const onRowsChange = angular.bind(this, this.onRowsChange);
        const refresh: any = angular.bind(this, this.refresh);

        tableService.registerTable((event) => {
            if (event.type === WranglerEventType.REFRESH) {
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
            return self.rows
        }, function () {
            onRowsChange();
            refresh();
        });

        // Refresh table on resize
        $scope_.$watch(() => $element.height(), refresh);
        $scope_.$watch(() => $element.width(), refresh);

        // Listen for destroy event
        $scope_.$on("destroy", () => self.$onDestroy());
    }

    $onDestroy() {
        this.tableService.unsubscribe();
    }

    $onInit() {
        this.onColumnsChange();
        this.rows = angular.copy(this.rows);
        this.init(this.$element);
    }

    /**
     * Gets the font for the header row.
     *
     * @returns {string}
     */
    getHeaderFont() {
        return (angular.isObject(this.options) && angular.isString(this.options.headerFont)) ? this.options.headerFont : DEFAULT_FONT;
    }

    /**
     * Gets the font for the data rows.
     *
     * @returns {string}
     */
    getRowFont() {
        return (angular.isObject(this.options) && angular.isString(this.options.rowFont)) ? this.options.rowFont : DEFAULT_FONT;
    }

    /**
     * Initializes the table.
     *
     * @param {jQuery} element the table element
     */
    init(element: JQueryStatic) {
        const self = this;
        this.$templateRequest_(HEADER_TEMPLATE)
            .then(function () {
                self.table_ = fattable({
                    container: element.get(0),
                    model: new WranglerTableModel(self.dataService),
                    nbRows: 0,
                    rowHeight: ROW_HEIGHT,
                    headerHeight: HEADER_HEIGHT,
                    painter: self,
                    columnWidths: [0]
                });

                self.$timeout_(angular.bind(self, self.refresh) as any, 500);
            });
    }

    /**
     * Redraws the table.
     */
    refresh() {
        // Skip if table not initialized
        if (this.table_ === null) {
            return;
        }

        // Re-calculate column widths
        const widthDiff = Math.abs(this.lastTableWidth_ - $(this.table_.container).width());

        if (widthDiff > 1) {
            const columnWidths: number[] = this.getColumnWidths();
            this.table_.columnWidths = columnWidths;
            this.table_.nbCols = columnWidths.length;

            const columnOffset = _.reduce(columnWidths, function (memo, width) {
                memo.push(memo[memo.length - 1] + width);
                return memo;
            }, [0]);
            this.table_.columnOffset = columnOffset;
            this.table_.W = columnOffset[columnOffset.length - 1];
        }

        // Update table properties
        const rowCount = angular.isArray(this.dataService.rows_) ? this.dataService.rows_.length : 0;
        this.table_.nbRows = rowCount;
        this.table_.H = ROW_HEIGHT * rowCount;

        // Rebuild table
        this.table_.setup();
    }

    /**
     * Refreshes the contents of rows.
     */
    refreshRows() {
        this.table_.refreshAllContent(true);
    }

    /**
     * Sets the sorting on a given column, optionally resetting any existing sorting on the table.
     *
     * @param {VisualQueryTableHeader} header the column to set the sorting on
     * @param {VisualQueryTable.ASC|VisualQueryTable.DESC} direction the direction to sort by, either descending or ascending
     */
    sortColumn(header: any, direction: any) {
        this.dataService.sortDirection_ = direction;
        this.dataService.sortIndex_ = header.index;
        this.onRowsChange();
    }

    /**
     * Removes sorting from the table.
     */
    unsort() {
        this.dataService.sortDirection_ = null;
        this.dataService.sortIndex_ = null;
        this.onRowsChange();
    }

    /**
     * Gets a 2D rending context for calculating text width.
     *
     * @private
     * @returns {CanvasRenderingContext2D} a 2D rendering context
     */
    get2dContext() {
        if (this.canvasContext_ === null) {
            const canvas = document.createElement("canvas");
            document.createDocumentFragment().appendChild(canvas);

            this.canvasContext_ = canvas.getContext("2d");
            if (angular.isString(this.options.headerFont)) {
                this.canvasContext_.font = this.options.headerFont;
            }
        }
        return this.canvasContext_;
    }

    /**
     * Calculates the width for every column.
     *
     * @private
     * @returns {Array.<number>} the column widths
     */
    getColumnWidths(): number[] {
        // Skip if no columns
        if (!angular.isArray(this.dataService.columns_) || this.dataService.columns_.length === 0) {
            return [];
        }

        // Determine column widths based on header size
        const context = this.get2dContext();
        context.font = this.getHeaderFont();

        const headerWidths = _.map(this.dataService.columns_, function (column: any, index) {
            const textWidth = context.measureText(column.displayName).width;
            const padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
            return Math.ceil(textWidth + padding + MENU_WIDTH);
        });

        // Determine column widths based on row sampling
        context.font = this.getRowFont();

        const rowWidths = _.map(this.dataService.columns_, function (column: any, index) {
            const textWidth = (column.longestValue != null) ? context.measureText(column.longestValue).width : 0;
            const padding = (index === 0) ? COLUMN_PADDING_FIRST : COLUMN_PADDING * 2;
            return Math.ceil(textWidth + padding);
        });

        // Calculate total width
        const columnWidths = [];
        let totalWidth = 0;

        for (let i = 0; i < this.dataService.columns_.length; ++i) {
            const width = Math.min(Math.max(headerWidths[i], rowWidths[i], COLUMN_WIDTH_MIN), COLUMN_WIDTH_MAX);
            columnWidths.push(width);
            totalWidth += width;
        }

        // Fit column widths to viewable width
        const padding = Math.max($(this.table_.container).width() - totalWidth, 0);

        return _.map(columnWidths, function (width) {
            return Math.floor(width + padding * width / totalWidth);
        });
    }

    /**
     * Applies filters to columns.
     */
    onColumnsChange() {
        // Update properties
        _.each(this.columns, function (column: any) {
            column.visible = (column.visible !== false);
        });

        // Filter columns
        this.dataService.columns_ = _.filter(this.columns, function (column: any) {
            return (column.visible !== false);
        });

        // Update rows
        this.onRowsChange();
    }

    /**
     * Sorts and applies filters to rows.
     */
    onRowsChange() {
        const self = this;

        // Filter rows
        this.dataService.rows_ = _.filter(this.rows, function (row) {
            return _.every(self.dataService.columns_, function (column: any) {
                return _.every(column.filters, function (filter: any) {
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
        if (angular.isNumber(this.dataService.sortIndex_) && this.dataService.sortIndex_ < this.dataService.columns_.length) {
            const field = (this.dataService.columns_[this.dataService.sortIndex_] as any).name;
            const lessThan = (this.dataService.sortDirection_ === (VisualQueryTable as any).ASC) ? -1 : 1;
            const greaterThan = -lessThan;

            this.dataService.rows_.sort(function (a, b) {
                if (a[field] === b[field]) {
                    return 0;
                } else {
                    return (a[field] < b[field]) ? lessThan : greaterThan;
                }
            });
        }
    }
}

angular.extend(VisualQueryTable.prototype, fattable.Painter.prototype, fattable.SyncTableModel.prototype, {
    /**
     * Will be called whenever a cell is put out of the DOM.
     *
     * @param {HTMLElement} cellDiv the cell <div> element
     */
    cleanUpCell(cellDiv: HTMLElement) {

    },

    /**
     * Will be called whenever a column is put out of the DOM.
     *
     * @param {HTMLElement} headerDiv the header <div> element
     */
    cleanUpHeader(headerDiv: HTMLElement) {

    },

    /**
     * Fills and style a cell div.
     *
     * @param {HTMLElement} cellDiv the cell <div> element
     * @param {VisualQueryTableCell|null} cell the cell object
     */
    fillCell(cellDiv: HTMLElement, cell: any) {
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
        } else if (cell.value !== null && cell.value.sqltypeName && cell.value.sqltypeName.startsWith("PERIOD")) {
            const value = "(" + cell.value.attributes.join(", ") + ")";
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
    fillHeader(headerDiv: HTMLElement, header: any) {
        // Adjust padding based on column number
        if (header !== null && header.index === 0) {
            headerDiv.style.paddingLeft = COLUMN_PADDING_FIRST + PIXELS;
            headerDiv.style.paddingRight = 0 + PIXELS;
        } else {
            headerDiv.style.paddingLeft = COLUMN_PADDING + PIXELS;
            headerDiv.style.paddingRight = COLUMN_PADDING + PIXELS;
        }

        // Update scope in a separate thread
        const $scope: any = angular.element(headerDiv).scope();
        const self = this;

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
    setupCell(cellDiv: HTMLElement) {
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
    setupHeader(headerDiv: HTMLElement) {
        // Set style attributes
        headerDiv.style.font = this.getHeaderFont();
        headerDiv.style.lineHeight = HEADER_HEIGHT + PIXELS;

        // Load template
        headerDiv.innerHTML = this.$templateCache_.get(HEADER_TEMPLATE) as string;
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
            (controller as VisualQueryTable).$onInit();
        }
    };
});

import * as angular from "angular";
import "fattable";
import * as $ from "jquery";
import * as _ from "underscore";

import {DomainType} from "../../../services/DomainTypesService";
import {TransformValidationResult} from "../../wrangler/model/transform-validation-result";
import {WranglerDataService} from "../services/wrangler-data.service";
import {WranglerEventType} from "../services/wrangler-event-type";
import {WranglerTableService} from "../services/wrangler-table.service";
import {VisualQueryPainterService} from "./visual-query-painter.service";
import {WranglerTableModel} from "./wrangler-table-model";

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
 * Maximum width of a column including padding.
 */
const COLUMN_WIDTH_MAX = 300;

/**
 * Minimum width of a column including padding.
 */
const COLUMN_WIDTH_MIN = 150;

/**
 * Width of the domain type icon.
 */
const DOMAIN_TYPE_WIDTH = 30;

/**
 * Width of the menu element in the header.
 */
const MENU_WIDTH = 52;

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

    static readonly $inject = ["$scope", "$element", "$timeout", "VisualQueryPainterService", "WranglerDataService", "WranglerTableService", "uiGridConstants"];

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
     * List of the available domain types.
     */
    domainTypes: DomainType[];

    /**
     * The table options.
     * @type {Object} options
     * @type {string} [options.headerFont] the font for the header row
     * @type {string} [options.rowFont] the font for the data rows
     */
    options: { headerFont?: string, rowFont?: string };

    /**
     * The data rows in this table.
     */
    rows: any[][];

    /**
     * Validation results for the data.
     */
    validationResults: TransformValidationResult[][];

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
    private table_: fattable.TableView = null;

    constructor(private $scope_: angular.IScope, private $element: angular.IAugmentedJQuery, private $timeout_: angular.ITimeoutService, private painter: VisualQueryPainterService,
                private dataService: WranglerDataService, private tableService: WranglerTableService, private uiGridConstants_: any) {
        this.painter.delegate = this;


        // Refresh table when model changes
        tableService.registerTable((event) => {
            if (event.type === WranglerEventType.REFRESH) {
                this.refresh();
            }
        });

        $scope_.$watchCollection(() => this.columns, () => {
            this.onColumnsChange();
            this.refresh();
        });

        $scope_.$watchCollection(() => this.domainTypes, () => {
            this.painter.domainTypes = this.domainTypes.sort((a, b) => (a.title < b.title) ? -1 : 1);
            this.refresh()
        });

        $scope_.$watch(() => this.options ? this.options.headerFont : null, () => painter.headerFont = this.options.headerFont);
        $scope_.$watch(() => this.options ? this.options.rowFont : null, () => painter.rowFont = this.options.rowFont);

        $scope_.$watchCollection(() => this.rows, () => {
            this.onRowsChange();
        });

        $scope_.$watchCollection(() => this.validationResults, () => {
            this.onValidationResultsChange();
            this.refresh();
        });

        let resizeTimeoutPromise: any = null;

        let resizeTimeout = <T>(callback: (...args: any[]) => T, interval: number) => {
            if (resizeTimeoutPromise != null) {
                this.$timeout_.cancel(resizeTimeoutPromise);
            }
            resizeTimeoutPromise = this.$timeout_(callback, interval);
        };

        // Refresh table on resize
        $scope_.$watch(() => $element.height(), () => resizeTimeout(() => this.refresh(), 500));

        $scope_.$watch(() => $element.width(), () => resizeTimeout(() => this.refresh(), 500));

        // Listen for destroy event
        $scope_.$on("destroy", () => this.$onDestroy());
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
     * Initializes the table.
     *
     * @param {jQuery} element the table element
     */
    init(element: angular.IAugmentedJQuery) {
        this.table_ = fattable({
            container: element.get(0),
            model: new WranglerTableModel(this.dataService),
            nbRows: 0,
            rowHeight: VisualQueryPainterService.ROW_HEIGHT,
            headerHeight: VisualQueryPainterService.HEADER_HEIGHT,
            painter: this.painter,
            columnWidths: [0]
        });

        this.$timeout_(this.refresh.bind(this), 500);
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
        const widthDiff = Math.abs(this.lastTableWidth_ - $((this.table_ as any).container).width());

        if (widthDiff > 1) {
            const columnWidths: number[] = this.getColumnWidths();
            (this.table_ as any).columnWidths = columnWidths;
            (this.table_ as any).nbCols = columnWidths.length;

            const columnOffset = _.reduce(columnWidths, function (memo, width) {
                memo.push(memo[memo.length - 1] + width);
                return memo;
            }, [0]);
            (this.table_ as any).columnOffset = columnOffset;
            (this.table_ as any).W = columnOffset[columnOffset.length - 1];
        }

        // Update table properties
        const rowCount = angular.isArray(this.dataService.rows_) ? this.dataService.rows_.length : 0;
        (this.table_ as any).nbRows = rowCount;
        (this.table_ as any).H = VisualQueryPainterService.ROW_HEIGHT * rowCount;

        // Rebuild table
        this.painter.hideTooltip();
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
     * @returns {CanvasRenderingContext2D} a 2D rendering context
     */
    private get2dContext() {
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
     * @returns {Array.<number>} the column widths
     */
    private getColumnWidths(): number[] {
        // Skip if no columns
        if (!angular.isArray(this.dataService.columns_) || this.dataService.columns_.length === 0) {
            return [];
        }

        // Determine column widths based on header size
        const context = this.get2dContext();
        context.font = this.painter.headerFont;

        const headerWidths = this.dataService.columns_.map((column: any, index) => {
            const textWidth = Math.max(context.measureText(column.displayName).width, context.measureText(column.dataType).width);
            const padding = (index === 0) ? VisualQueryPainterService.COLUMN_PADDING_FIRST : VisualQueryPainterService.COLUMN_PADDING * 2;
            const menuWidth = (this.domainTypes ? DOMAIN_TYPE_WIDTH : 0) + (index === 0 ? MENU_WIDTH * 1.5 : MENU_WIDTH);
            return Math.ceil(textWidth + padding + menuWidth);
        });

        // Determine column widths based on row sampling
        context.font = this.painter.rowFont;

        const rowWidths = _.map(this.dataService.columns_, function (column: any, index) {
            const textWidth = (column.longestValue != null) ? context.measureText(column.longestValue).width : 0;
            const padding = (index === 0) ? VisualQueryPainterService.COLUMN_PADDING_FIRST : VisualQueryPainterService.COLUMN_PADDING * 2;
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
        const padding = Math.max($((this.table_ as any).container).width() - totalWidth, 0);

        return _.map(columnWidths, function (width) {
            return Math.floor(width + padding * width / totalWidth);
        });
    }

    /**
     * Applies filters to columns.
     */
    private onColumnsChange() {
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
    private onRowsChange() {
        const self = this;

        // Filter rows
        this.dataService.rows_ = _.filter(this.rows, function (row) {
            return _.every(self.dataService.columns_, function (column: any, index) {
                return _.every(column.filters, function (filter: any) {
                    if (angular.isUndefined(filter.term) || filter.term === null) {
                        return true;
                    } else if (filter.condition === self.uiGridConstants_.filter.CONTAINS) {
                        if (angular.isUndefined(filter.regex)) {
                            filter.regex = new RegExp(filter.term);
                        }
                        return filter.regex.test(row[index]);
                    } else if (filter.condition === self.uiGridConstants_.filter.LESS_THAN) {
                        return row[index] < filter.term;
                    } else if (filter.condition === self.uiGridConstants_.filter.GREATER_THAN) {
                        return row[index] > filter.term;
                    } else if (filter.condition === self.uiGridConstants_.filter.EXACT) {
                        if (angular.isUndefined(filter.regex)) {
                            filter.regex = new RegExp("^" + filter.term + "$");
                        }
                        return filter.regex.test(row[index]);
                    } else {
                        return false;
                    }
                });
            });
        });

        // Sort rows
        if (angular.isNumber(this.dataService.sortIndex_) && this.dataService.sortIndex_ < this.dataService.columns_.length) {
            const column = this.dataService.sortIndex_;
            const lessThan = (this.dataService.sortDirection_ === (VisualQueryTable as any).ASC) ? -1 : 1;
            const greaterThan = -lessThan;

            this.dataService.rows_.sort(function (a, b) {
                if (a[column] === b[column]) {
                    return 0;
                } else {
                    return (a[column] < b[column]) ? lessThan : greaterThan;
                }
            });
        }
    }

    private onValidationResultsChange() {
        this.dataService.validationResults = this.validationResults;
    }
}

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
            (controller as VisualQueryTable).$onInit();
        }
    };
});

import * as angular from "angular";
import "fattable";
import * as $ from "jquery";
import * as _ from "underscore";

import {DomainType} from "../../../services/DomainTypesService";
import {TransformValidationResult} from "../../wrangler/model/transform-validation-result";
import {WranglerDataService} from "../services/wrangler-data.service";
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
const COLUMN_WIDTH_MIN = 100;

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

    static readonly $inject = ["$scope", "$element", "$timeout", "VisualQueryPainterService", "WranglerDataService", "WranglerTableService", "uiGridConstants", "$window"];

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
     * Whether the data has been actually modified/transformed (vs simple paging)
     */
    tableState: number;

    /**
     * Previous state
     */
    lastState: number = -1;

    /**
     * TODO: Remove Validation results for the data.
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

    private actualRows : number;

    private actualCols : number;

    /**
     * The table view.
     * @type {fattable.TableView}
     */
    private table_: fattable.TableView = null;

    private tableModel: any;

    constructor(private $scope_: angular.IScope, private $element: angular.IAugmentedJQuery, private $timeout_: angular.ITimeoutService, private painter: VisualQueryPainterService,
                private dataService: WranglerDataService, private tableService: WranglerTableService, private uiGridConstants_: any, private $window: angular.IWindowService) {
        this.painter.delegate = this;

        /* Watch on columns indicating model changed */
        $scope_.$watchCollection(() => this.columns, () => {

            this.dataService.state = this.tableState;
            this.dataService.columns_ = this.columns;
            this.onColumnsChange();
          //  this.onRowsChange();
            this.refresh();
            this.lastState = this.tableState;
        });

        $scope_.$watch(() => this.options ? this.options.headerFont : null, () => painter.headerFont = this.options.headerFont);
        $scope_.$watch(() => this.options ? this.options.rowFont : null, () => painter.rowFont = this.options.rowFont);

        let resizeTimeoutPromise: any = null;

        let resizeTimeout = <T>(callback: (...args: any[]) => T, interval: number) => {
            if (resizeTimeoutPromise != null) {
                this.$timeout_.cancel(resizeTimeoutPromise);
            }
            resizeTimeoutPromise = this.$timeout_(callback, interval);
        };

        // Refresh table on resize
        //$scope_.$watch(() => $element.width(), () => resizeTimeout(() => this.refresh(), 50));
        angular.element($window).bind('resize',()=> resizeTimeout(() => this.refresh(), 150));

        // Listen for destroy event
        $scope_.$on("destroy", () => this.$onDestroy());
    }

    $onDestroy() {
        this.tableService.unsubscribe();
    }

    $onInit() {
        this.rows = angular.copy(this.rows);
        this.init(this.$element);
    }

    /**
     * Initializes the table.
     *
     * @param {jQuery} element the table element
     */
    init(element: angular.IAugmentedJQuery) {
        this.tableModel = new WranglerTableModel(this.dataService);

        this.table_ = fattable({
            container: element.get(0),
            model: this.tableModel,
            nbRows: 23399,
            rowHeight: VisualQueryPainterService.ROW_HEIGHT,
            headerHeight: VisualQueryPainterService.HEADER_HEIGHT,
            painter: this.painter,
            columnWidths: [180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180,180],
            autoSetup: false
        });
    }

    /**
     * Redraws the table.
     */
    refresh() : void {

        // Skip if table not initialized
        if (this.table_ === null) {
            return;
        }

        if (this.columns != null && this.columns.length > 0) {
            this.painter.domainTypes = this.domainTypes.sort((a, b) => (a.title < b.title) ? -1 : 1);

            // Re-calculate column widths
            const widthDiff = Math.abs(this.lastTableWidth_ - $((this.table_ as any).container).width());

            if (widthDiff > 1) {
                let columnWidths: number[] = this.getColumnWidths();

                (this.table_ as any).columnWidths = columnWidths;
                (this.table_ as any).nbCols = (this.actualCols != null ? this.actualCols : columnWidths.length);

                const columnOffset = _.reduce(columnWidths, function (memo, width) {
                    memo.push(memo[memo.length - 1] + width);
                    return memo;
                }, [0]);
                (this.table_ as any).columnOffset = columnOffset;
                (this.table_ as any).W = columnOffset[columnOffset.length - 1];
            }
            // Update table properties

            if (this.actualRows != null) {
                (this.table_ as any).nbRows = this.actualRows;
                (this.table_ as any).H = VisualQueryPainterService.ROW_HEIGHT * this.actualRows;
            }

            // Rebuild table
            this.painter.hideTooltip();
        }

        var scrollPosition = this.savePosition();
        this.table_.setup();
        this.restorePosition(scrollPosition);
    }

    restorePosition(sp: ScrollPosition) {
        var ourTable : any  = (this.table_ as any);
        ourTable.scroll.setScrollXY(sp.left, sp.top);
    }

    savePosition() : ScrollPosition {
        // Preserve scroll position
        var priorScrollLeft : number = 0;
        var priorScrollTop : number = 0;

        var ourTable : any  = (this.table_ as any);
        if (!angular.isUndefined(ourTable.scroll)) {
            var ratioX = 0;
            var ratioY = 0;

            ratioX = ourTable.scroll.scrollLeft / ourTable.W;
            ratioY = ourTable.scroll.scrollTop  / ourTable.H;

            var newX = (ourTable.W *ratioX) | 0;
            var newY = (ourTable.H *ratioY) | 0;

            if (ourTable.scroll) {
                var scrollBar = ourTable.scroll;
                priorScrollLeft = scrollBar.scrollLeft;
                priorScrollTop = scrollBar.scrollTop;
            }

            // If scrolling we will preserve both, if transformation we will only keep left position
            if (this.tableState !== this.lastState) {
                priorScrollTop = 0;
            }
        }
        return { left: priorScrollLeft, top: priorScrollTop};
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
    * Calculate row widths by sampling values
   */
    private sampleMaxWidth(col: number) : string {

         let maxValue : string = "";
         // Sample up to 20 rows
         for (var row = 0; row < this.rows.length && row < 20 ; row++) {
             var val = this.rows[row][col];
             if (val && val.length > maxValue.length) {
                 maxValue = val;
             }
         }
         // Avoid letting one column dominate so we limit max
         return maxValue;
    }

    /**
     * Calculates the width for every column.
     *
     * @returns {Array.<number>} the column widths
     */
    private getColumnWidths(): number[] {
        var self = this;
        // Skip if no columns
        if (!angular.isArray(this.columns) || this.columns.length === 0) {
            return [];
        }

        // Determine column widths based on header size
        const context = this.get2dContext();
        context.font = this.painter.headerFont;

        const headerWidths = this.columns.map((column: any, index) => {
            const textWidth = Math.max(context.measureText(column.displayName).width, context.measureText(column.dataType).width);
            const padding = (index === 0) ? VisualQueryPainterService.COLUMN_PADDING_FIRST : VisualQueryPainterService.COLUMN_PADDING * 3;
            const menuWidth = (this.domainTypes ? DOMAIN_TYPE_WIDTH : 0) + (index === 0 ? MENU_WIDTH * 1.5 : MENU_WIDTH);
            return Math.ceil(textWidth + padding + menuWidth);
        });

        // Determine column widths based on row sampling
        context.font = this.painter.rowFont;

        const rowWidths = _.map(this.columns, function (column: any, index) {
            let textWidthChars = (column.longestValue != null ? column.longestValue : self.sampleMaxWidth(index));
            const textWidth = context.measureText(textWidthChars).width;
            const padding = (index === 0) ? VisualQueryPainterService.COLUMN_PADDING_FIRST : VisualQueryPainterService.COLUMN_PADDING * 3;
            return Math.ceil(textWidth + padding);
        });

        // Calculate total width
        const columnWidths = [];
        let totalWidth = 0;

        for (let i = 0; i < this.columns.length; ++i) {
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
        this.columns = _.filter(this.columns, function (column: any) {
            return (column.visible !== false);
        });
    }

    /**
     * Sorts and applies filters to rows.
     */
    private onRowsChange() {
        const self = this;

        // Add index column
        if (this.rows && this.rows.length > 0 && this.rows[0].length === this.columns.length) {
            this.rows.forEach((row, index) => row.push(index));
        }

        //sorts and filters are now applied server side

    }



}

export class ScrollPosition {
    left: number;
    top: number;
}

angular.module(moduleName).directive("visualQueryTable", function () {
    return {
        bindToController: {
            columns: "=*tableColumns",
            domainTypes: "=*tableDomainTypes",
            options: "=*tableOptions",
            rows: "=*tableRows",
            validationResults: "=*tableValidation",
            tableState: "=",
            actualCols: "=",
            actualRows: "="
        },
        controller: VisualQueryTable,
        restrict: "E",
        link: function ($scope, element, attrs, controller) {
            //(controller as VisualQueryTable).$onInit();
        }
    };
});

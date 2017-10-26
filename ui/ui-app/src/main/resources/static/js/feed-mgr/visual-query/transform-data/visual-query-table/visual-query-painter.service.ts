import * as angular from "angular";
import "fattable";

import {DomainType} from "../../../services/DomainTypesService";

/**
 * Default font.
 */
const DEFAULT_FONT = "10px sans-serif";

/**
 * HTML template for header cells.
 */
const HEADER_TEMPLATE = "js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html";

/**
 * Pixel unit.
 */
const PIXELS = "px";

export class VisualQueryPainterService extends fattable.Painter {

    /**
     * Left and right padding for normal columns.
     */
    static readonly COLUMN_PADDING = 28;

    /**
     * Left padding for the first column.
     */
    static readonly COLUMN_PADDING_FIRST = 24;

    /**
     * Height of header row.
     */
    static readonly HEADER_HEIGHT = 56;

    /**
     * Height of data rows.
     */
    static readonly ROW_HEIGHT = 48;

    /**
     * Visual Query Component instance.
     */
    private _delegate: any;

    /**
     * List of available domain types.
     */
    private _domainTypes: DomainType[];

    /**
     * Font for the header row
     */
    private _headerFont: string;

    /**
     * Font for the data rows
     */
    private _rowFont: string;

    /**
     * Panel containing the tooltip.
     */
    private tooltipPanel: angular.material.IPanelRef;

    /**
     * Indicates that the tooltip should be visible.
     */
    private tooltipVisible: boolean = false;

    static readonly $inject = ["$compile", "$mdPanel", "$rootScope", "$templateCache", "$templateRequest", "$timeout", "$window"];

    /**
     * Constructs a {@code VisualQueryPainterService}.
     */
    constructor(private $compile: angular.ICompileService, private $mdPanel: angular.material.IPanelService, private $scope: angular.IRootScopeService,
                private $templateCache: angular.ITemplateCacheService, private $templateRequest: angular.ITemplateRequestService, private $timeout: angular.ITimeoutService,
                private $window: angular.IWindowService) {
        super();

        $templateRequest(HEADER_TEMPLATE);

        this.tooltipPanel = $mdPanel.create({
            animation: this.$mdPanel.newPanelAnimation().withAnimation({open: 'md-show', close: 'md-hide'}),
            attachTo: angular.element(document.body),
            template: `{{value}}<ul><li ng-repeat="item in validation">{{item.rule}}: {{item.reason}}</li></ul>`,
            focusOnOpen: false,
            panelClass: 'md-tooltip md-origin-bottom visual-query-tooltip',
            propagateContainerEvents: true,
            zIndex: 100
        } as any);
        this.tooltipPanel.attach();

        $window.addEventListener("scroll", () => this.hideTooltip(), true);
    }

    /**
     * Gets the Visual Query Component for this painter.
     */
    get delegate(): any {
        return this._delegate;
    }

    set delegate(value: any) {
        this._delegate = value;
    }

    /**
     * Gets the list of available domain types.
     */
    get domainTypes(): DomainType[] {
        return this._domainTypes;
    }

    set domainTypes(value: DomainType[]) {
        this._domainTypes = value;
    }

    /**
     * Gets the font for the header row.
     */
    get headerFont() {
        return (this._headerFont != null) ? this._headerFont : DEFAULT_FONT;
    }

    set headerFont(value: string) {
        this._headerFont = value;
    }

    /**
     * Gets the font for the data rows.
     */
    get rowFont() {
        return (this._rowFont != null) ? this._rowFont : DEFAULT_FONT;
    }

    set rowFont(value: string) {
        this._rowFont = value;
    }

    /**
     * Fills and style a cell div.
     *
     * @param {HTMLElement} cellDiv the cell <div> element
     * @param {VisualQueryTableCell|null} cell the cell object
     */
    fillCell(cellDiv: HTMLElement, cell: any) {
        // Adjust padding based on column number
        if (cell !== null && cell.column === 0) {
            cellDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING_FIRST + PIXELS;
            cellDiv.style.paddingRight = 0 + PIXELS;
        } else {
            cellDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
            cellDiv.style.paddingRight = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
        }

        // Set style
        if (cell === null) {
            cellDiv.className = "";
        } else if (cell.validation) {
            cellDiv.className = "invalid";
        } else if (cell.value === null) {
            cellDiv.className = "null";
        } else {
            cellDiv.className = "";
        }

        // Set contents
        if (cell === null) {
            cellDiv.textContent = "";
        } else if (cell.value !== null && cell.value.sqltypeName && cell.value.sqltypeName.startsWith("PERIOD")) {
            cellDiv.textContent = "(" + cell.value.attributes.join(", ") + ")";
        } else {
            cellDiv.textContent = cell.value;
        }

        if (cell !== null) {
            angular.element(cellDiv).data("validation", cell.validation);
        }
    }

    /**
     * Fills and style a column div.
     *
     * @param {HTMLElement} headerDiv the header <div> element
     * @param {VisualQueryTableHeader|null} header the column header
     */
    fillHeader(headerDiv: HTMLElement, header: any) {
        // Adjust padding based on column number
        if (header !== null && header.index === 0) {
            headerDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING_FIRST + PIXELS;
            headerDiv.style.paddingRight = 0 + PIXELS;
        } else {
            headerDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
            headerDiv.style.paddingRight = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
        }

        // Update scope in a separate thread
        const $scope: any = angular.element(headerDiv).scope();

        if ($scope.header !== header) {
            $scope.availableDomainTypes = this.domainTypes;
            $scope.domainType = header.domainTypeId ? this.domainTypes.find((domainType: DomainType) => domainType.id === header.domainTypeId) : null;
            $scope.header = header;
            $scope.header.unsort = this.unsort.bind(this);
            $scope.table = this.delegate;
        }
    }

    /**
     * Hides the tooltip.
     */
    hideTooltip() {
        this.tooltipVisible = false;
        this.$timeout(() => {
            if (this.tooltipVisible === false) {
                this.tooltipPanel.hide();
            }
        }, 75);
    }

    /**
     * Setup method are called at the creation of the cells. That is during initialization and for all window resize event.
     *
     * Cells are recycled.
     *
     * @param {HTMLElement} cellDiv the cell <div> element
     */
    setupCell(cellDiv: HTMLElement) {
        angular.element(cellDiv).on("mouseenter", () => this.showTooltip(cellDiv));
        angular.element(cellDiv).on("mouseleave", () => this.hideTooltip());

        cellDiv.style.font = this.rowFont;
        cellDiv.style.lineHeight = VisualQueryPainterService.ROW_HEIGHT + PIXELS;
    }

    /**
     * Setup method are called at the creation of the column header. That is during initialization and for all window resize event.
     *
     * Columns are recycled.
     *
     * @param {HTMLElement} headerDiv the header <div> element
     */
    setupHeader(headerDiv: HTMLElement) {
        // Set style attributes
        headerDiv.style.font = this.headerFont;
        headerDiv.style.lineHeight = VisualQueryPainterService.HEADER_HEIGHT + PIXELS;

        // Load template
        headerDiv.innerHTML = this.$templateCache.get(HEADER_TEMPLATE) as string;
        this.$compile(headerDiv)(this.$scope.$new(true));
    }

    /**
     * Shows the tooltip on the specified cell.
     */
    private showTooltip(cellDiv: HTMLElement) {
        this.tooltipVisible = true;

        // Update content
        const $scope = this.tooltipPanel.panelEl.scope() as any;
        $scope.validation = angular.element(cellDiv).data("validation");
        $scope.value = cellDiv.innerText;

        // Update position
        const cellOffset = angular.element(cellDiv).offset();
        let offsetY;
        let yPosition;

        if (cellOffset.top + VisualQueryPainterService.ROW_HEIGHT * 3 > this.$window.innerHeight) {
            offsetY = "-27" + PIXELS;
            yPosition = this.$mdPanel.yPosition.ABOVE;
        } else {
            offsetY = "0";
            yPosition = this.$mdPanel.yPosition.BELOW;
        }

        this.tooltipPanel.updatePosition(
            this.$mdPanel.newPanelPosition()
                .relativeTo(cellDiv)
                .addPanelPosition(this.$mdPanel.xPosition.ALIGN_START, yPosition)
                .withOffsetX("28px")
                .withOffsetY(offsetY)
        );

        // Show tooltip
        this.tooltipPanel.open();
    }

    /**
     * Turns off sorting.
     */
    private unsort() {
        if (this.delegate) {
            this.delegate.unsort();
        }
    }
}

angular.module(require("feed-mgr/visual-query/module-name")).service("VisualQueryPainterService", VisualQueryPainterService);

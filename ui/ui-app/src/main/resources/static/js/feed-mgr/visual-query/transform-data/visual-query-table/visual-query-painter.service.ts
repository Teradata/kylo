import * as angular from "angular";
import "fattable";

import {DomainType} from "../../../services/DomainTypesService";
import {DataCategory} from "../../wrangler/column-delegate";

/**
 * Default font.
 */
const DEFAULT_FONT = "10px ''SourceSansPro'";

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
     * Maximum display length for column context functions before they are ellipses (asthetics)
     */
    static readonly MAX_DISPLAY_LENGTH = 25;

    /**
     * Left and right padding for normal columns.
     */
    static readonly COLUMN_PADDING = 5;

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
    static readonly ROW_HEIGHT = 27;

    /**
     * Class for selected cells.
     */
    static readonly SELECTED_CLASS = "selected";

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
     * Panel containing the cell menu.
     */
    private menuPanel: angular.material.IPanelRef;

    /**
     * Indicates that the menu should be visible.
     */
    private menuVisible: boolean = false;

    /**
     * Cell that was last clicked.
     */
    private selectedCell: HTMLElement;

    /**
     * Panel containing the tooltip.
     */
    private tooltipPanel: angular.material.IPanelRef;

    /**
     * Indicates that the tooltip should be visible.
     */
    private tooltipVisible: boolean = false;

    /**
     * Indicate thate the header template has been loaded into the $templateCache
     * @type {boolean}
     */
    private headerTemplateLoaded : boolean = false;


    /**
     * Array of header div HTMLElements that are waiting for the HEADER_TEMPLATE to get loaded.
     * Once the template is loaded these elements will get filled
     * @type {any[]}
     */
    private waitingHeaderDivs : HTMLElement[] = [];

    static readonly $inject = ["$compile", "$mdPanel", "$rootScope", "$templateCache", "$templateRequest", "$timeout", "$window"];

    /**
     * Constructs a {@code VisualQueryPainterService}.
     */
    constructor(private $compile: angular.ICompileService, private $mdPanel: angular.material.IPanelService, private $scope: angular.IRootScopeService,
                private $templateCache: angular.ITemplateCacheService, private $templateRequest: angular.ITemplateRequestService, private $timeout: angular.ITimeoutService,
                private $window: angular.IWindowService) {
        super();

        //Request the Header template and fill in the contents of any header divs waiting on the template.
        $templateRequest(HEADER_TEMPLATE).then((response) => {
            this.headerTemplateLoaded = true;
            angular.forEach(this.waitingHeaderDivs,(headerDiv : HTMLElement) => {
                this.compileHeader(headerDiv);
            });
            this.waitingHeaderDivs = [];
        });

        // Hide tooltip on scroll. Skip Angular change detection.
       /*
        window.addEventListener("scroll", () => {
            if (this.tooltipVisible) {
                this.hideTooltip();
            }
        }, {passive:true, capture:true});
    */
        // Create menu
        this.menuPanel = $mdPanel.create({
            animation: this.$mdPanel.newPanelAnimation().withAnimation({open: 'md-active md-clickable', close: 'md-leave'}),
            attachTo: angular.element(document.body),
            clickOutsideToClose: true,
            escapeToClose: true,
            focusOnOpen: true,
            panelClass: "_md md-open-menu-container md-whiteframe-z2 visual-query-menu",
            templateUrl: "js/feed-mgr/visual-query/transform-data/visual-query-table/cell-menu.template.html"
        });
        this.menuPanel.attach();

        // Create tooltip
        this.tooltipPanel = $mdPanel.create({
            animation: this.$mdPanel.newPanelAnimation().withAnimation({open: "md-show", close: "md-hide"}),
            attachTo: angular.element(document.body),
            template: `{{value}}<ul><li ng-repeat="item in validation">{{item.rule}}: {{item.reason}}</li></ul>`,
            focusOnOpen: false,
            panelClass: "md-tooltip md-origin-bottom visual-query-tooltip",
            propagateContainerEvents: true,
            zIndex: 100
        });
        this.tooltipPanel.attach();

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

    fillCellPending(cellDiv: HTMLElement) {
        cellDiv.textContent = "Loading...";
        cellDiv.className = "pending";
    }

    fillHeaderPending(cellDiv: HTMLElement) {
        // Override so it doesn't replace our angular template for column cell
    }

    /**
     * Fills and style a cell div.
     *
     * @param {HTMLElement} cellDiv the cell <div> element
     * @param {VisualQueryTableCell|null} cell the cell object
     */
    fillCell(cellDiv: HTMLElement, cell: any) {
        // Set style

        if (cell === null) {
            cellDiv.className = "";
        } else if (cell.validation) {
            $(cellDiv).addClass("invalid");
        } else if (cell.value === null) {
            cellDiv.className = "null";
        } else {
            cellDiv.className = "";
        }

        // Adjust padding based on column number
        if (cell !== null && cell.column === 0) {
            cellDiv.className += " first-column ";
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
            cellDiv.className += cellDiv.className + " " + (cell.row % 2 == 0 ? "even" : "odd");

            angular.element(cellDiv)
                .data("column", cell.column)
                .data("validation", cell.validation);
        }
    }

    /**
     * Fills and style a column div.
     *
     * @param {HTMLElement} headerDiv the header <div> element
     * @param {VisualQueryTableHeader|null} header the column header
     */
    fillHeader(headerDiv: HTMLElement, header: any) {

        // Update scope in a separate thread
        const $scope: any = angular.element(headerDiv).scope();

        if (header != null && $scope.header !== header && header.delegate != undefined) {
            $scope.header = header;
            $scope.table = this.delegate;
            $scope.availableCasts = header.delegate.getAvailableCasts();
            $scope.availableDomainTypes = this.domainTypes;
            $scope.domainType = header.domainTypeId ? this.domainTypes.find((domainType: DomainType) => domainType.id === header.domainTypeId) : null;
            $scope.header.unsort = this.unsort.bind(this);
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

        angular.element(cellDiv)
            .on("contextmenu", () => false)
            .on("mousedown", () => this.setSelected(cellDiv))
            .on("mouseenter", () => this.showTooltip(cellDiv))
            .on("mouseleave", () => this.hideTooltip())
            .on("mouseup", event => this.showMenu(cellDiv, event));

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
        //if the header template is not loaded yet then fill it with Loading text.
        // the callback on the templateRequest will compile those headers waiting
        if(!this.headerTemplateLoaded) {
            headerDiv.textContent = "Loading...";
            headerDiv.className = "pending";
            this.waitingHeaderDivs.push(headerDiv)
        }
        else {
            this.compileHeader(headerDiv);
        }

    }

    /**
     * Cleanup any events attached to the header
     * @param headerDiv
     */
    cleanUpHeader(headerDiv: HTMLElement){
        //destroy the old scope if it exists
        let oldScope = angular.element(headerDiv).isolateScope();
        if(angular.isDefined(oldScope)){
            oldScope.$destroy();
        }
    }

    /**
     * Cleanup any events attached to the cell
     * @param cellDiv
     */
    cleanUpCell(cellDiv: HTMLElement) {
       angular.element(cellDiv).unbind();
    }

    /**
     * Called when the table is refreshed
     * This should cleanup any events/bindings/scopes created by the prior render of the table
     * @param table
     */
    cleanUp(table:HTMLElement){
        //remove all header scopes
        this.headerScopes.forEach((headerScope: IScope) => {
            headerScope.$destroy();
        });
        this.headerScopes = [];

        super.cleanUp(table);
        angular.element(table).unbind();
    }
    private headerScopes : IScope[] = []

    private compileHeader(headerDiv: HTMLElement) {
        // Load template
        headerDiv.innerHTML = this.$templateCache.get(HEADER_TEMPLATE) as string;

        let newScope = this.$scope.$new(true)
        this.headerScopes.push(newScope);
        this.$compile(headerDiv)(newScope);


    }

    /**
     * Hides the cell menu.
     */
    private hideMenu() {
        this.menuVisible = false;
        this.$timeout(() => {
            if (this.menuVisible === false) {
                this.menuPanel.close();
            }
        }, 75);
    }

    /**
     * Sets the currently selected cell.
     */
    private setSelected(cellDiv: HTMLElement) {
        // Remove previous selection
        if (this.selectedCell) {
            angular.element(this.selectedCell).removeClass(VisualQueryPainterService.SELECTED_CLASS);
        }

        // Set new selection
        this.selectedCell = cellDiv;
        angular.element(this.selectedCell).addClass(VisualQueryPainterService.SELECTED_CLASS);
    }

    /**
     * Shows the cell menu on the specified cell.
     */
    private showMenu(cellDiv: HTMLElement, event: JQueryEventObject) {
        // Get column info
        const cell = angular.element(cellDiv);
        const column = cell.data("column");
        const header = this.delegate.columns[column];
        const isNull = cell.hasClass("null");
        const selection = this.$window.getSelection();

        if (this.selectedCell !== event.target || (selection.anchorNode !== null && selection.anchorNode !== selection.focusNode)) {
            return;  // ignore dragging between elements
        }
        if (angular.element(document.body).children(".CodeMirror-hints").length > 0) {
            return;  // ignore clicks when CodeMirror function list is active
        } else if (header.delegate.dataCategory === DataCategory.DATETIME || header.delegate.dataCategory === DataCategory.NUMERIC || header.delegate.dataCategory === DataCategory.STRING) {
            this.menuVisible = true;
        } else {
            return;  // ignore clicks on columns with unsupported data types
        }

        // Update content
        const $scope: IScope = (this.menuPanel.config as any).scope;
        $scope.DataCategory = DataCategory;
        $scope.header = header;
        $scope.selection = (header.delegate.dataCategory === DataCategory.STRING) ? selection.toString() : null;
        $scope.table = this.delegate;
        $scope.value = isNull ? null : cellDiv.innerText;
        $scope.displayValue = ($scope.value.length > VisualQueryPainterService.MAX_DISPLAY_LENGTH ? $scope.value.substring(0, VisualQueryPainterService.MAX_DISPLAY_LENGTH) + "...": $scope.value)

        // Update position
        this.menuPanel.updatePosition(
            this.$mdPanel.newPanelPosition()
                .left(event.clientX + PIXELS)
                .top(event.clientY + PIXELS)
        );

        // Show menu
        this.menuPanel.open()
            .then(() => {
                // Add click listener
                this.menuPanel.panelEl.on("click", "button", () => this.hideMenu());

                // Calculate position
                const element = angular.element(this.menuPanel.panelEl);
                const height = element.height();
                const offset = element.offset();
                const width = element.width();

                // Fix position if off screen
                const left = (offset.left + width > this.$window.innerWidth) ? this.$window.innerWidth - width - 8 : event.clientX;
                const top = (offset.top + height > this.$window.innerHeight) ? this.$window.innerHeight - height - 8 : event.clientY;
                if (left !== event.clientX || top !== event.clientY) {
                    this.menuPanel.updatePosition(
                        this.$mdPanel.newPanelPosition()
                            .left(left + PIXELS)
                            .top(top + PIXELS)
                    );
                }
            });
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

import * as angular from "angular";
import * as $ from "jquery";
import "fattable";

import { DomainType } from "../../../services/DomainTypesService";
import { DataCategory } from "../../wrangler/column-delegate";
import { Injectable,  Injector, ComponentFactoryResolver, ApplicationRef, EmbeddedViewRef } from "@angular/core";
import { downgradeInjectable } from "@angular/upgrade/static";
import { moduleName } from "../../../module-name";
import { VisualQueryTableHeader } from "./visual-query-table-header.component";
import { CellMenuComponent } from "./cell-menu.component";
import {BroadcastService} from "../../../../services/broadcast-service";

/**
 * Default font.
 */
const DEFAULT_FONT = "10px ''SourceSansPro'";

/**
 * Pixel unit.
 */
const PIXELS = "px";

@Injectable()
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
     * Cell that was last clicked.
     */
    private selectedCell: HTMLElement;

    /**
     * Indicates that the tooltip should be visible.
     */
    private tooltipVisible: boolean = false;

    /**
     * Array of header div HTMLElements that are waiting for the HEADER_TEMPLATE to get loaded.
     * Once the template is loaded these elements will get filled
     * @type {any[]}
     */
    private waitingHeaderDivs: HTMLElement[] = [];


    private componentRef: any;

    private componentRefMenu: any;
    private componentFactoryMenu: any;


    private previousCellDiv: HTMLElement;
    /**
     * Constructs a {@code VisualQueryPainterService}.
     */
    constructor(private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private _appRef: ApplicationRef,
        private broadcastService: BroadcastService) {

        super();
        this.broadcastService.subscribe(null, "CLOSE_CELL_MENU", () => this.hideCellMenu());
        // Create tooltip   
        $(document.body).append("<div id=\"cellTooltip\" style=\"position: absolute; z-index: 101; background-color:#e63838; padding: 4px; border-radius: 5px; font-size: 10px;\">");
    }

    ngOnDestroy() {
        this._appRef.detachView(this.componentRef.hostView);
        this.componentRef.destroy();
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
            cellDiv.textContent = cell.value
        }

        if (cell !== null) {
            cellDiv.className += cellDiv.className + " " + (cell.row % 2 == 0 ? "even" : "odd");

            angular.element(cellDiv)
                .data("column", cell.column)
                .data("validation", cell.validation)
                .data("realValue", cell.value);
        }

        $(cellDiv).html(cellDiv.textContent.replace(/\s/g, "<span class='ws-text'>·</span>"))
    }

    /**
     * Fills and style a column div.
     *
     * @param {HTMLElement} headerDiv the header <div> element
     * @param {VisualQueryTableHeader|null} header the column header
     */
    fillHeader(headerDiv: HTMLElement, header: any) {
        if (header != null && header.delegate != undefined) {
            this.compileHeader(headerDiv, header);
        }
    }

    /**
     * Hides the tooltip.
     */
    hideTooltip() {
        this.tooltipVisible = false;
        setTimeout(() => {
            if (this.tooltipVisible === false) {
                $("#cellTooltip").css({ "display": "none" });
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
            .on("mouseenter", (event) => this.showTooltip(cellDiv, event))
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
        headerDiv.textContent = "Loading...";
        headerDiv.className = "pending";
        this.waitingHeaderDivs.push(headerDiv)

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
    cleanUp(table: HTMLElement) {
        super.cleanUp(table);
        angular.element(table).unbind();
    }

    private compileHeader(headerDiv: HTMLElement, header: any) {
        // Load template

        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(VisualQueryTableHeader);
        this.componentRef = componentFactory.create(this.injector);

        this.componentRef.instance.header = header;
        this.componentRef.instance.table = this.delegate;
        this.componentRef.instance.availableCasts = header.delegate.getAvailableCasts();
        this.componentRef.instance.availableDomainTypes = this.domainTypes;
        this.componentRef.instance.domainType = header.domainTypeId ? this.domainTypes.find((domainType: DomainType) => domainType.id === header.domainTypeId) : null;
        this.componentRef.instance.header.unsort = this.unsort.bind(this);

        this._appRef.attachView(this.componentRef.hostView);

        // outletElement should be the HTMLElement for the header
        headerDiv.replaceChild((this.componentRef.hostView as EmbeddedViewRef<any>).rootNodes[0] as HTMLElement, headerDiv.firstChild);
    }
    hideCellMenu() {
        this._appRef.detachView(this.componentRefMenu.hostView);
        this.componentRefMenu.destroy();
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
     * Create the display string for a selection
     */
    private niceSelection(selection: string): string {
        switch (selection) {
            case ' ':
                return '(space)';
            case '':
                return '(empty)';
            case null:
                return '(empty)';
            default:
                return selection;
        }
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
        const selection = window.getSelection();
        const range = selection.getRangeAt(0)

        if (this.selectedCell !== event.target || (selection.anchorNode !== null && selection.anchorNode !== selection.focusNode)) {
            return;  // ignore dragging between elements
        }
        if (angular.element(document.body).children(".CodeMirror-hints").length > 0) {
            return;  // ignore clicks when CodeMirror function list is active
        } else if (header.delegate.dataCategory === DataCategory.DATETIME || header.delegate.dataCategory === DataCategory.NUMERIC || header.delegate.dataCategory === DataCategory.STRING) {
            // Update Menu
            this.componentFactoryMenu = this.componentFactoryResolver.resolveComponentFactory(CellMenuComponent);
            this.componentRefMenu = this.componentFactoryMenu.create(this.injector);

            this.componentRefMenu.instance.DataCategory = DataCategory;
            this.componentRefMenu.instance.header = header;
            this.componentRefMenu.instance.selection = (header.delegate.dataCategory === DataCategory.STRING) ? selection.toString().replace("·", " ") : null;
            this.componentRefMenu.instance.selectionDisplay = this.niceSelection(this.componentRefMenu.instance.selection)
            this.componentRefMenu.instance.range = range;
            this.componentRefMenu.instance.table = this.delegate;
            this.componentRefMenu.instance.value = isNull ? null : $(cellDiv).data('realValue');
            this.componentRefMenu.instance.displayValue = (this.componentRefMenu.instance.value.length > VisualQueryPainterService.MAX_DISPLAY_LENGTH ? this.componentRefMenu.instance.value.substring(0, VisualQueryPainterService.MAX_DISPLAY_LENGTH) + "..." : this.componentRefMenu.instance.value)

            const left = (event.clientX > window.innerWidth) ? (window.innerWidth - event.clientX - 8) : event.clientX;
            const top = (event.clientY > window.innerHeight) ? (window.innerHeight - event.clientY - 8) : event.clientY;

            this.componentRefMenu.instance.menuTop = top + PIXELS;
            this.componentRefMenu.instance.menuLeft = left + PIXELS;

            this._appRef.attachView(this.componentRefMenu.hostView);
            // outletElement should be the HTMLElement for the header
            $(document.body).append((this.componentRefMenu.hostView as EmbeddedViewRef<any>).rootNodes[0] as HTMLElement);
        } else {
            return;  // ignore clicks on columns with unsupported data types
        }
    }
    /**
     * Shows the tooltip on the specified cell.
     */
    private showTooltip(cellDiv: HTMLElement, event: any) {

        // Update position
        if (!this.previousCellDiv || (this.previousCellDiv != cellDiv)) {
            const cellOffset = $(cellDiv).offset();
            let offsetY;
            let yPosition;

            if (cellOffset.top + VisualQueryPainterService.ROW_HEIGHT * 3 > window.innerHeight) {
                offsetY = "-27";
            } else {
                offsetY = "0";
            }

            const left = (event.clientX > window.innerWidth) ? (window.innerWidth - event.clientX - 8) : event.clientX;
            const top = (event.clientY > window.innerHeight) ? (window.innerHeight - event.clientY - 8) : event.clientY;

            if (!this.tooltipVisible) {
                $("#cellTooltip").html(cellDiv.innerText);
                $("#cellTooltip").css({ "top": top, "left": left, "display": "block" });
            }
        }

        this.tooltipVisible = true;
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
angular.module(moduleName).service('VisualQueryPainterService', downgradeInjectable(VisualQueryPainterService));

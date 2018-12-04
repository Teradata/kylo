import * as angular from 'angular';
import * as _ from "underscore";
import {VisualQueryPainterService} from '../../visual-query/transform-data/visual-query-table/visual-query-painter.service';
import {DOCUMENT} from '@angular/platform-browser';
import {Component, ElementRef, HostListener, Inject, Input} from "@angular/core";


export class ActiveResize {

    resizeInProgress:boolean;
    headerDiv:JQuery;
    headerElem:HTMLElement;
    initialHeaderWidth :number;

    /**
     * the id of the column we are resizing
     */
    columnId:string;

    /**
     * the start x of the resize
     */
    startX:number;

    /**
     * resize amount
     * @type {number}
     */
    diff:number  = 0;

    /**
     * the actual width of the column
     */
    width:number;

    /**
     * Min width acceptable for the column
     */
    minColWidth?:number;

    constructor(private document:any){


    }

    start(columnId:string, seperator:JQuery, startX:number, width:number,  minColWidth?:number){
        this.startX = startX;
        this.columnId = columnId;
        this.headerDiv = seperator.parent();
        this.headerElem = this.headerDiv.get(0);
        this.initialHeaderWidth = this.headerElem.offsetWidth;;

        this.minColWidth = minColWidth;

        this.width = width;


        this.headerDiv.css("z-index", "1"); //to hide other columns behind the one being resized
        this.document.body.style.cursor = "col-resize";
        this.diff = 0;

        this.resizeInProgress = true;
    }

    resize(endX:number){
        this.diff = endX - this.startX;
        let updatedWith = this.initialHeaderWidth + this.diff;
        this.width = updatedWith < this.minColWidth ? this.minColWidth : updatedWith;
        this.headerElem.style.width = this.width + "px";
    }

    getWidth(){
        return this.width;
    }

    stop() :number {
        this.headerDiv.css("z-index", "unset");
        document.body.style.cursor = null;
        this.resizeInProgress = false;
        this.minColWidth = undefined;
        return this.getWidth();
    }

    isResizing(){
        return this.resizeInProgress;
    }


}

export class FattableOptions {
    tableContainerId:string
    headers: any[]
    rows: any[]
    minColumnWidth: number = 50
    maxColumnWidth: number =  300;
    rowHeight: number =  27;
    headerHeight: number =  40;
    headerPadding: number =  5;
    padding: number =  50;
    firstColumnPaddingLeft: number =  24;
    headerFontFamily = "Roboto, \"Helvetica Neue\", sans-serif"
    headerFontSize = "13px"
    headerFontWeight = "500"
    rowFontFamily = "sans-serif"
    rowFontSize = "13px"
    rowFontWeight = "normal"
    setupRefreshDebounce:number = 300

    public constructor(init?: Partial<FattableOptions>) {
        Object.assign(this, init);
    }

    headerText(header: any) {
        if(typeof header == "string"){
            return header;
        }
        else {
            return header.displayName;
        }

    }


    cellText (row: any, column: any) {
        if(typeof column =="string") {
            return row[column];
        }
        else {
            return row[column.displayName];
        }
    }
    fillCell (cellDiv: any, data: any) {
        cellDiv.innerHTML = _.escape(data.value);
    }
    getCellSync (i: any, j: any) {
        const displayName = this.headerText(this.headers[j]) //.displayName;
        const row = this.rows[i];
        if (row === undefined) {
            //occurs when filtering table
            return undefined;
        }
        return {
            "value": row[displayName]
        }
    }
    fillHeader(headerDiv: any, header: any) {
        headerDiv.innerHTML = _.escape(header.value);
    }
    getHeaderSync(j: any) {
        return this.headerText(this.headers[j]);// this.headers[j].displayName;
    }



}

@Component({
    selector:"fattable",
    template:`<div style="width:700px;height:500px;" ></div>`
})
export class FattableComponent {

    ATTR_DATA_COLUMN_ID = "data-column-id";

   activeResize:ActiveResize;


   @Input()
   options:FattableOptions;

    optionDefaults: FattableOptions = new FattableOptions();


    scrollXY: number[] = [];
    table:fattable.TableView;
    tableData:fattable.SyncTableModel = new fattable.SyncTableModel();
    painter:fattable.Painter =new fattable.Painter();
    settings:FattableOptions;
    columnWidths:number[]
    selector:string;



    constructor(private ele:ElementRef, @Inject(DOCUMENT) private document: any){
        this.activeResize = new ActiveResize(this.document)
    }

    setupTable(){
        this.settings = new FattableOptions(this.options);
        //add the array of column widths to the settings to populate
        this.columnWidths = [];
        this.populateHeaders();
        this.setupPainter();
        this.setupTableData();
        this.createTable();

    }

    ngOnInit() {
        this.setupTable();

    }


    /**
     * populate the Headers with data from the settings
     * @param settings
     * @param {fattable.TableModel} tableData
     */
    private populateHeaders(){
        const headers = this.settings.headers;
        const rows = this.settings.rows;

        this.tableData.columnHeaders = [];
        const headerContext = this.get2dContext(this.settings.headerFontWeight + " " + this.settings.headerFontSize + " " + this.settings.headerFontFamily);
        const rowContext = this.get2dContext(this.settings.rowFontWeight + " " + this.settings.rowFontSize + " " + this.settings.rowFontFamily);
        _.each(this.settings.headers, (column) =>{
            const headerText = this.settings.headerText(column);
            const headerTextWidth = headerContext.measureText(headerText).width;
            const longestColumnText = _.reduce(rows, (previousMax, row) =>{
                const cellText = this.settings.cellText(row, column);
                const cellTextLength = cellText === undefined || cellText === null ? 0 : cellText.length;
                return previousMax.length < cellTextLength ? cellText : previousMax;
            }, "");

            const columnTextWidth = rowContext.measureText(longestColumnText).width;
            this.columnWidths.push(Math.min(this.settings.maxColumnWidth, Math.max(this.settings.minColumnWidth, headerTextWidth, columnTextWidth)) + this.settings.padding);
            this.tableData.columnHeaders.push(headerText);
        });
    }

    private get2dContext(font: any) {
        const canvas = this.document.createElement("canvas");
        this.document.createDocumentFragment().appendChild(canvas);
        const context = canvas.getContext("2d");
        context.font = font;
        return context;
    }

    /**
     * setup the painter
     * @param {fattable.Painter} painter
     */
    private setupPainter(){



        let mousedown = (separator: JQuery, e: JQueryEventObject) => {
            e.preventDefault(); //prevent default action of selecting text

            const columnId = this.getColumnId(separator);
            let start = 0, diff = 0, newWidth = this.settings.minColumnWidth;
            if (e.pageX) {
                start = e.pageX;
            } else if (e.clientX) {
                start = e.clientX;
            }
            this.activeResize.start(columnId,separator,start,newWidth,this.settings.minColumnWidth);
        }

        this.painter.setupHeader = (div:Element) => {
            // console.log("setupHeader");
            let separator = jQuery('<div class="header-separator"></div>')
            let heading = jQuery('<div class="header-value ui-grid-header-cell-title"></div>')

           // div.insertAdjacentHTML("afterend",heading)

            let headerDiv =jQuery(div);

            separator.on("mousedown", event => mousedown(separator, event));
            headerDiv.append(heading).append(separator);
        }

        this.painter.fillCell = (div:any, data:any) =>{
            if (data === undefined) {
                return;
            }
            if (data.columnId === 0) {
                div.className = " first-column ";
            }
            div.style.fontSize = this.settings.rowFontSize;
            div.style.fontFamily = this.settings.rowFontFamily;
            div.className += "layout-column layout-align-center-start ";
            if (data["rowId"] % 2 === 0) {
                div.className += " even ";
            }
            else {
                div.className += " odd ";
            }
            this.settings.fillCell(div, data);
        };

        this.painter.fillHeader =  (div: any, header: any) =>{
            // console.log('fill header', header);
            div.style.fontSize = this.settings.headerFontSize;
            div.style.fontFamily = this.settings.headerFontFamily;
            div.style.fontWeight = this.settings.headerFontWeight;
            const children = jQuery(div).children();

            this.setColumnId(children.last(), header.id);

            const valueDiv = children.first();
            valueDiv.css("width", (this.table.columnWidths[header.id] - this.settings.headerPadding - 2) + "px"); //leave 2 pixels for column separator
            const valueSpan = valueDiv.get(0);
            this.settings.fillHeader(valueSpan, header);
        };
    }

    setupTableData(){
        this.tableData.getCellSync =  (i: any, j: any) => {
            let data :any = this.settings.getCellSync(i, j);
            if (data !== undefined) {
                //add row id so that we can add odd/even classes to rows
                data.rowId = i;
                data.columnId = j;
            }
            return data;
        };

        this.tableData.getHeaderSync = (j: number) =>{
            const header = this.settings.getHeaderSync(j);
            return {
                value: header,
                id: j
            };
        };
    }

    @HostListener('document:mousemove', ['$event'])
    onMouseMove(e:MouseEvent) {
        if(this.activeResize.isResizing()) {
            let end = 0;
            if (e.pageX) {
                end = e.pageX;
            } else if (e.clientX) {
                end = e.clientX;
            }
         this.activeResize.resize(end);
        }
    }

    @HostListener('document:mouseup', ['$event'])
    onMouseUp(e:MouseEvent) {
       // document.body.onmousemove = document.body.onmouseup = null;
        if(this.activeResize.isResizing()) {
            let columnId = this.activeResize.columnId;
            let newWidth = this.activeResize.stop();
            this.resizeColumn(columnId, newWidth);
        }
    }

   private resizeColumn(columnId: string, columnWidth: number) {
        const x = this.scrollXY[0];
        const y = this.scrollXY[1];
        // console.log('resize to new width', columnWidth);
        this.table.columnWidths[columnId] = columnWidth;
        const columnOffset = _.reduce((this.table.columnWidths as number[]), function (memo, width) {
            memo.push(memo[memo.length - 1] + width);
            return memo;
        }, [0]);
        // console.log('columnWidths, columnOffset', columnWidths, columnOffset);
        this.table.columnOffset = columnOffset;

      //  this.table.W = columnOffset[columnOffset.length - 1];

       this.table.setup();

        // console.log('displaying cells', scrolledCellX, scrolledCellY);
      // this.table.scroll.setScrollXY(x, y); //table.setup() scrolls to 0,0, here we scroll back to were we were while resizing column
    }

    createTable() {

        if(this.settings.tableContainerId == undefined){
            this.settings.tableContainerId = _.uniqueId("fattable-");
        }
        this.ele.nativeElement.id = this.settings.tableContainerId
        this.selector = "#" + this.settings.tableContainerId;
        const parameters = {
            "container": this.selector,
            "model": this.tableData,
            "nbRows":  this.settings.rows.length,
            "rowHeight":  this.settings.rowHeight,
            "headerHeight":  this.settings.headerHeight,
            "painter":  this.painter,
            "columnWidths":  this.columnWidths
        };

        let onScroll = (x: number, y: number) =>{
            this.scrollXY[0] = x;
            this.scrollXY[1] = y;
        }

        this.table = fattable(parameters);
        this.table.onScroll = onScroll;
        this.table.setup();







    }

    private getColumnId(separatorSpan: JQuery) {
        return separatorSpan.attr(this.ATTR_DATA_COLUMN_ID);
    }

    private setColumnId(separatorSpan: JQuery, id: string) {
        separatorSpan.attr(this.ATTR_DATA_COLUMN_ID, id);
    }

    private registerEvents(){
        const eventId = "resize.fattable." + this.settings.tableContainerId;
        jQuery( this.getWindow()).unbind(eventId);

        jQuery( this.getWindow()).on(eventId,  () =>{
            _.debounce(this.setupTable, this.settings.setupRefreshDebounce)
        });

        jQuery(this.selector).on('$destroy',  ()=> {
            jQuery(this.getWindow()).unbind(eventId);
        });
    }

    private getWindow(){
        return window;
    }

}
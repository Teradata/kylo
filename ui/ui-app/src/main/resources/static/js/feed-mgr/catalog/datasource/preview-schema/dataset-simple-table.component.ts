import {AfterViewInit, Component, ContentChildren, EventEmitter, Input, OnInit, Output, QueryList, ViewChild} from "@angular/core";
import {
    ITdDataTableSelectAllEvent,
    ITdDataTableSelectEvent,
    ITdDataTableSortChangeEvent,
    TdDataTableComponent,
    TdDataTableService,
    TdDataTableSortingOrder,
    TdDataTableTemplateDirective
} from "@covalent/core/data-table";
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";
import {TableColumn} from "./model/table-view-model";
import {CloneUtil} from "../../../../common/utils/clone-util";

export class DatasetSimpleTableOptions {
    selectable:boolean = false;
    sortable:boolean = true;
    // do we initially mark all as selected?
    initiallySelected:boolean = false;

    constructor() {

    }


}

class SelectionRow {
    constructor(public row:any, public selected:boolean){}
}

export class SelectionSummary {
    public rows:SelectionRow[];
    public selectedCount:number;
    public rowMap:Map<any,SelectionRow> = new Map<any, SelectionRow>();
    constructor(){

    }

    initialize(rows:any[],selected:boolean) {
        this.rowMap = new Map<any, SelectionRow>();
        this.rows = rows.map(row => {
            let selectionRow = new SelectionRow(row,selected);
            this.rowMap.set(row,selectionRow);
            return selectionRow;
        });

        if(selected) {
            this.selectedCount = this.rows.length;
        }
        else {
            this.selectedCount = 0;
        }
    }

    selectAll(){
        this.rows.filter(row => !row.selected).forEach(row => row.selected = true);
        this.selectedCount = this.rows.length;
    }
    selectNone(){
        this.rows.filter(row => row.selected).forEach(row => row.selected = false);
        this.selectedCount = 0;
    }

    deselect(row:any,index:number) {
        let selectionRow = this.rowMap.get(row);
        if(selectionRow) {
            selectionRow.selected = false;
        }
        this.selectedCount-=1;
    }
    select(row:any,index:number) {
        let selectionRow = this.rowMap.get(row);
        if(selectionRow) {
            selectionRow.selected = true;
        }
        this.selectedCount+=1;
    }

    getSelectedRows() :any[]{
        return this.rows.filter(row => row.selected).map(row => row.row);
    }

}

@Component({
    selector: 'dataset-simple-table',
    styleUrls:["./dataset-simple-table.component.scss"],
    template:`
      <td-data-table #datatable
          [data]="filteredData"
          [columns]="columns"
          [selectable]="options.selectable"
          [sortable]="options.sortable"
          [sortBy]="sortBy"
          [sortOrder]="sortOrder"
          (sortChange)="sort($event)"
          (rowSelect)="rowSelect($event)"
          (selectAll)="selectAll($event)"
          [style.height.px]="325" class="dataset-simple-table">
        <ng-content></ng-content>
      </td-data-table>   
    <div  *ngIf="filteredData.length == 0" fxLayout="row" fxLayoutAlign="center center">
      <h3>No results to display.</h3>
    </div>`
})
export class DatasetSimpleTableComponent  implements OnInit, AfterViewInit{

    @ContentChildren(TdDataTableTemplateDirective) templates: QueryList<TdDataTableTemplateDirective>;

    @Input()
    rows:any[];

    @Input()
    columns:TableColumn[] = [];

    @Input()
    options?:DatasetSimpleTableOptions = new DatasetSimpleTableOptions();


    @Output("rowSelect")
    public rowSelected:EventEmitter<ITdDataTableSelectEvent> = new EventEmitter<ITdDataTableSelectEvent>();


    @Output("selectAll")
    public selectedAll:EventEmitter<ITdDataTableSelectAllEvent> = new EventEmitter<ITdDataTableSelectAllEvent>();

    @Output("selectionChange")
    public selectionChange:EventEmitter<SelectionSummary> = new EventEmitter<SelectionSummary>();
    @ViewChild("datatable")
    datatable: TdDataTableComponent;

    selectionSummary:SelectionSummary = new SelectionSummary();

    constructor(  private _dataTableService: TdDataTableService){
    }


    /**
     * All the data
     * @type {any[]}
     */
    data:any[] = [];

    /**
     * sorted/filtered data displayed in the ui
     * @type {any[]}
     */
    filteredData:any[] = [];

    /**
     * Calc the column width based upon data?
     */
    @Input()
    calcColumnWidth:boolean = true;


    sortBy: string = '';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;


    rowSelect($event:ITdDataTableSelectEvent){
        if($event.selected) {
            this.selectionSummary.select($event.row,$event.index)
        }
        else {
            this.selectionSummary.deselect($event.row,$event.index)
        }
        this.rowSelected.emit($event)
        this.selectionChange.emit(this.selectionSummary)
    }

    selectAll($event:ITdDataTableSelectAllEvent){
        if($event.selected) {
            this.selectionSummary.selectAll()
        }
        else {
            this.selectionSummary.selectNone();
        }
        this.selectedAll.emit($event)
        this.selectionChange.emit(this.selectionSummary)
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }

    filter(){
        let newData:any[] = this.data;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        this.filteredData = newData;
    }



    ngOnInit(){
        this.initTable();
        this.selectionSummary.initialize(this.rows,this.options.initiallySelected)
    }

    ngAfterViewInit(): void {
            // Templates won't be seen by TdDataTable; need to manually link them
            this.datatable._templates = this.templates;
            this.datatable.ngAfterContentInit();

        if(this.options.initiallySelected){
            this.datatable.selectAll(true);
        }
    }

    ngOnChanges(changes: SimpleChanges) {

        if(changes && (!changes.rows.firstChange || !changes.columns.firstChange)){
            this.initTable();
            this.selectionSummary.initialize(this.rows,false)
            if(this.options.initiallySelected){
             setTimeout(() => this.datatable.selectAll(true),50);
            }
        }

    }


    initTable(){
        if(this.columns) {
            this.sortBy = this.columns[0].name;
        }
        else {
            this.columns = [];
        }
        //determine width from data
        if(this.calcColumnWidth && this.columns.length >3) {
            let colWidths = this.calculateMaxColumnWidth();
            this.columns.forEach(col => {
                let width = colWidths[col.name]
                let pixels = 0;
                if (width == 0) {
                    pixels = 100;
                }
                else {
                    pixels = width * 11;
                }
                if (pixels > 300) {
                    pixels = 300;
                }
                if (pixels < 150) {
                    pixels = 150;
                }
                col.width = pixels;
            });
        }

        // Add table data
        this.data = this.rows;
        this.filter();
    }

    /**
     * Calc the max width based upon data
     * @return {any}
     */
    calculateMaxColumnWidth():any{
        let widthMap = {};
        this.rows.map(row => {
            this.columns.forEach(col => {
                let columnValue = row[col.name];
                let max = widthMap[col.name];
                let currWidth = 0
                if (max == undefined) {
                    max = 0;
                    widthMap[col.name] = 0;
                }
                if (columnValue != undefined && columnValue != null) {
                    currWidth = (columnValue+"").length;
                }
                if (currWidth > max) {
                    widthMap[col.name] = currWidth;
                }
            });
        });
           return widthMap;

    }
}
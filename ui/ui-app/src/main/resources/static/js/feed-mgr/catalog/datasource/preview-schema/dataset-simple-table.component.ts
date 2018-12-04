import {Component, Input} from "@angular/core";
import {ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";
import {TableColumn} from "./model/table-view-model";

@Component({
    selector: 'dataset-simple-table',
    styleUrls:["./dataset-simple-table.component.scss"],
    template:`
      <td-data-table
          [data]="filteredData"
          [columns]="columns"
          [selectable]="false"
          [sortable]="true"
          [sortBy]="sortBy"
          [sortOrder]="sortOrder"
          (sortChange)="sort($event)"
          [style.height.px]="325" class="dataset-simple-table">      
      </td-data-table>   
    <div  *ngIf="filteredData.length == 0" fxLayout="row" fxLayoutAlign="center center">
      <h3>No results to display.</h3>
    </div>`
})
export class DatasetSimpleTableComponent {

    @Input()
    rows:any[];

    @Input()
    columns:TableColumn[] = [];


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
    }

    ngOnChanges(changes: SimpleChanges) {

        if(changes && (!changes.rows.firstChange || !changes.columns.firstChange)){
            this.initTable();
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
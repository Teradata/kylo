import {Component, Input} from "@angular/core";
import {ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {SimpleChanges} from "@angular/core/src/metadata/lifecycle_hooks";
import {TableColumn} from "./model/table-view-model";

@Component({
    selector: 'dataset-simple-table',
    styleUrls:["js/feed-mgr/catalog/datasource/preview-schema/dataset-simple-table.component.scss"],
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
     <!--  <div class="dataset-simple-table">
    <table td-data-table >
      <thead>
      <tr td-data-table-column-row>
        <th td-data-table-column
            *ngFor="let column of columns"
            [name]="column.name"
            [sortable]="false"
            [numeric]="column.numeric"
            (sortChange)="sort($event)"
            [sortOrder]="sortOrder">
          {{column.label}} <br/>
          ({{column.dataType}})
        </th>
      </tr>
      </thead>
      <tbody>
      <tr td-data-table-row *ngFor="let row of filteredData">
        <td td-data-table-cell *ngFor="let column of columns"
            [numeric]="column.numeric">
          {{row[column.name]}}
        </td>
      </tr>
      </tbody>
    </table>
   </div>
-->
    <!--
          <td-data-table
          [data]="filteredData"
          [columns]="columns"
          [selectable]="false"
          [clickable]="true"
          [multiple]="multiple"
          [sortable]="false"
          [sortBy]="sortBy"
          [(ngModel)]="selected"
          [sortOrder]="sortOrder"
          (rowClick)="rowSelected($event)"
          (sortChange)="sort($event)"
          [style.height.px]="325" class="dataset-simple-table">
      </td-data-table>     
      -->
    
    
    
    <div  *ngIf="!filteredData.length ===0" fxLayout="row" fxLayoutAlign="center center">
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


    sortBy: string = '';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;



    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order === TdDataTableSortingOrder.Descending ? TdDataTableSortingOrder.Ascending : TdDataTableSortingOrder.Descending;
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

        // Add table data
        this.data = this.rows;
        this.filter();
    }





}
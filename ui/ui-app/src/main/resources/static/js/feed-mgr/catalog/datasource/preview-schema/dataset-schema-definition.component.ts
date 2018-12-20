import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {TableColumn} from "./model/table-view-model";
import {DatasetSimpleTableOptions, SelectionSummary} from "./dataset-simple-table.component";
import {ITdDataTableSelectAllEvent, ITdDataTableSelectEvent} from "@covalent/core";

@Component({
    selector:'dataset-schema-definition',
    template:`
        <dataset-simple-table [calcColumnWidth]="false" (selectionChange)="selectionChange($event)" (rowSelect)="rowSelect($event)" (selectAll)="selectAll($event)"  [options]="options" [class.small]="smallView" [rows]="columns" [columns]="schemaColumns"></dataset-simple-table>    
    `

})
export class DatasetSchemaDefinitionComponent  implements OnInit {

    @Input()
    columns:TableColumn[]

    @Input()
    options?:DatasetSimpleTableOptions = new DatasetSimpleTableOptions();

    @Input()
    smallView:boolean = true;


    @Output("rowSelect")
    public rowSelected:EventEmitter<ITdDataTableSelectEvent> = new EventEmitter<ITdDataTableSelectEvent>();

    @Output("selectAll")
    public selectedAll:EventEmitter<ITdDataTableSelectAllEvent> = new EventEmitter<ITdDataTableSelectAllEvent>();

    @Output("selectionChange")
    public selectionChanged:EventEmitter<SelectionSummary> = new EventEmitter<SelectionSummary>();

    schemaColumns:TableColumn[]

    constructor() {

    }

    private toDataTable(){
        let schemaColumns :TableColumn[] = [];
        schemaColumns.push({"name":"name","label":"Column Name","dataType":"string","sortable":true});
        schemaColumns.push({"name":"dataType","label":"Data Type","dataType":"string","sortable":true});
        schemaColumns.push({"name":"description","label":"Description","dataType":"string","sortable":true});
        this.schemaColumns = schemaColumns;
    }

    ngOnInit(){
        if(this.columns == undefined){
            this.columns = [];
        }
        this.toDataTable();
    }

    rowSelect($event:ITdDataTableSelectEvent){
        this.rowSelected.emit($event)
    }

    selectAll($event:ITdDataTableSelectAllEvent){
        this.selectedAll.emit($event)
    }

    selectionChange($event:SelectionSummary){
        this.selectionChanged.emit($event);
    }
}

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {TableColumn} from "./model/table-view-model";
import {DescriptionChangeEvent} from './dataset-simple-table.component';

@Component({
    selector:'dataset-schema-definition',
    template:`
        <dataset-simple-table [calcColumnWidth]="false" [class.small]="smallView" 
                              [rows]="columns" 
                              [columns]="schemaColumns" 
                              (descriptionChange)="onDescriptionChange($event)"></dataset-simple-table>    
    `

})
export class DatasetSchemaDefinitionComponent  implements OnInit {

    @Input()
    columns:TableColumn[]

    @Input()
    smallView:boolean = true;

    schemaColumns:TableColumn[];

    @Output()
    descriptionChange = new EventEmitter<DescriptionChangeEvent>();


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

    onDescriptionChange(event: DescriptionChangeEvent) {
        this.descriptionChange.emit(event)
    }
}

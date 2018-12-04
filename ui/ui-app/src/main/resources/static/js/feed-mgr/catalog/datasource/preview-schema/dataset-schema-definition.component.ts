import {Component, Input, OnInit} from "@angular/core";
import {TableColumn} from "./model/table-view-model";

@Component({
    selector:'dataset-schema-definition',
    template:`
        <dataset-simple-table [calcColumnWidth]="false" [class.small]="smallView" [rows]="columns" [columns]="schemaColumns"></dataset-simple-table>    
    `

})
export class DatasetSchemaDefinitionComponent  implements OnInit {

    @Input()
    columns:TableColumn[]

    @Input()
    smallView:boolean = true;

    schemaColumns:TableColumn[]

    constructor() {

    }

    private toDataTable(){
        let schemaColumns :TableColumn[] = []
        schemaColumns.push({"name":"name","label":"Column Name","dataType":"string","sortable":true})
        schemaColumns.push({"name":"dataType","label":"Data Type","dataType":"string","sortable":true})
        this.schemaColumns = schemaColumns;
    }

    ngOnInit(){
        if(this.columns == undefined){
            this.columns = [];
        }
        this.toDataTable();
    }

}

import {DomainType} from "../../../services/DomainTypesService";
import {Component, Inject} from "@angular/core";
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent, ITdDataTableColumn } from '@covalent/core/data-table';
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DomainTypeConflictDialogResponse} from "../domain-type-conflict/domain-type-conflict-dialog.component";
import {ITdDataTableSelectEvent} from "@covalent/core";


/**
 * Local data for {@link ApplyTableDomainTypesDialog}.
 */
export interface ApplyDomainTypesData {

    /**
     * Detected domain type corresponding to each field.
     */
    domainTypes: DomainType[];

    /**
     * Column definitions.
     */
    fields: TableColumnDefinition[];
}

export enum ApplyDomainTypesResponseStatus {
    APPLY=1,REJECT=2
}
export class ApplyDomainTypesResponse {
    appliedRows :ApplyDomainTypesRow[] = [];

    constructor(public status:ApplyDomainTypesResponseStatus){

    }
    static rejectedResponse(){
        return new ApplyDomainTypesResponse(ApplyDomainTypesResponseStatus.REJECT);
    }

    static appliedResponse(appliedRows :ApplyDomainTypesRow[]) {
        let response = new ApplyDomainTypesResponse(ApplyDomainTypesResponseStatus.APPLY);
        response.appliedRows = appliedRows;
        return response;
    }
}

export interface ApplyDomainTypesRow {
    name:string;
    domainType:DomainType;
    newName:string;
    dataType:string;
    newType:string;
}

/**
 * Dialog for resolving conflicts between detected data type of columns and detected domain type.
 */
@Component({
    selector:"apply-domain-types-dialog",
    templateUrl: "./apply-domain-types-dialog.component.html"
})
export class ApplyDomainTypesDialogComponent {

    /**
     * Table options.
     */
    columns: ITdDataTableColumn[] = [
            {name: "name", label: "Column", sortable:true},
            {name: "domainType", label: "Domain Type", sortable:true},
            {name: "newName", label: "New Name", sortable:true},
            {name: "dataType", label: "Original Type", sortable:true},
            {name: "newType", label: "New Type", sortable:true}
        ];

    /**
     * All the data
     * @type {any[]}
     */
    data:ApplyDomainTypesRow[] = [];

    /**
     * sorted/filtered data displayed in the ui
     * @type {any[]}
     */
    filteredData:ApplyDomainTypesRow[] = [];

    /**
     * Rows selected
     * @type {any[]}
     */
    selectedRows:ApplyDomainTypesRow[] = [];

    sortBy: string = 'name';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;



    constructor(
        private _dataTableService: TdDataTableService,
        public dialogRef: MatDialogRef<ApplyDomainTypesDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public dialogData: ApplyDomainTypesData) {
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }

    filter(){
        let newData:ApplyDomainTypesRow[] = this.data;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        this.filteredData = newData;
    }

    ngOnInit(){
        // Add table data
        this.data = this.dialogData.fields.map((field, index) => {
            const domainType = this.dialogData.domainTypes[index];
            return {
                name: field.name,
                dataType: field.derivedDataType,
                domainType: domainType,
                newName: domainType.field.name ? domainType.field.name : field.name,
                newType: domainType.field.derivedDataType ? domainType.field.derivedDataType : field.derivedDataType
            };
        });
        this.filter();
    }

    onRowSelected(event:ITdDataTableSelectEvent){
        /*
        if(event.selected){
            this.selectedRows.push(event.row);
        }
        else {
            //remove
            let idx = this.selectedRows.indexOf(event.row);
            if(idx >=0){
                this.selectedRows.splice(idx,1);
            }
        }
        */
    }


    /**
     * Close the dialog and reject promise. 
     */
    cancel() {
        this.dialogRef.close(ApplyDomainTypesResponse.rejectedResponse());
    }

    /**
     * Close the dialog and resolve promise.
     */
    hide() {

        if(this.selectedRows.length>0){
            this.dialogRef.close(ApplyDomainTypesResponse.appliedResponse(this.selectedRows));
        }
        else {
            this.dialogRef.close(ApplyDomainTypesResponse.rejectedResponse());
        }
    }
}

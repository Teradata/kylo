import * as _ from "underscore"
import {ITdDataTableColumnWidth} from "@covalent/core/data-table/data-table.component";
export interface TableColumn {
    name:string;
    label:string;
    dataType:string;
    uuid?:string;
    numeric?:boolean;
    sortable?:boolean;
    width?:ITdDataTableColumnWidth | number
}





export class TableViewModel{
    /**
     * is there an error when viewing the table
     */
    error:boolean;
    /**
     * Error message
     */
    errorMessage:string;
    /**
     * Array of column objects
     */
    columns:TableColumn[];
    /**
     * Array of Rows
     */
    rows:any[];

    public static numberTypes: string[] = ["int","bigint","integer","float","double","decimal","long"];

    public static isNumeric(dataType:string){
        return TableViewModel.numberTypes.indexOf(dataType) >=0;
    }

    public constructor(init?:Partial<TableViewModel>) {
        this.error = false;
        this.errorMessage = "";
        Object.assign(this, init);

    }

    hasColumns(){
        return this.columns != undefined;
    }

    updateError(msg:string){
        this.error = true;
        this.errorMessage = msg;
    }


    clearError(){
        this.error = false;
        this.errorMessage = null;
    }



    columnData(colName:string) :string[]{
        if(this.columns && this.columns.filter(col => col.name == colName).length >0) {
            return this.rows.map(row => {
                let columnValue = row[colName];
                if(_.isUndefined(columnValue) || columnValue == null){
                    return "";
                }
                else if(_.isObject(columnValue) || _.isArray(columnValue)){
                    return JSON.stringify(columnValue);
                }
                else {
                    return columnValue.toString();
                }
            });
        }
        else {
            return [];
        }
    }
}
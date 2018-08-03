
export interface TableColumn {
    name:string;
    label:string;
    dataType:string;
    uuid?:string
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

    public constructor(init?:Partial<TableViewModel>) {
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
            return this.rows.map(row => row[colName]);
        }
        else {
            return [];
        }
    }
}
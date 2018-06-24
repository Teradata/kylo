
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
    columns:any;
    /**
     * Array of Rows
     */
    rows:any;

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
}
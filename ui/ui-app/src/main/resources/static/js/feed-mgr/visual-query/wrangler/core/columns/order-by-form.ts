import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";



export class OrderByForm extends ColumnForm {


    constructor(column:any, grid:any,controller:ColumnController, private callback?:Function){
        super(column,grid,controller)
    }

    buildForm() {
        const DEFAULT = "(default)";
        return new DynamicFormBuilder().setTitle("Ordering (optional)")
            .column()
            .select().setKey("orderBy1")
            .setPlaceholder("Order field")
            .setValue(DEFAULT)
            .setRequired(false)
            .setOptions(this.getColumnNames().map(col => {
                return {label:col,value:col};
            })).done()
            .checkbox().setKey("asc1").setPlaceholder("Asc?")
            .setValue(true).done()
            .columnComplete()
            .onApply((values:any)=> {
                let orderByClause :string[] = [];
                this.buildOrderBy(orderByClause, values.orderBy1, values.asc1, DEFAULT);
                let orderBy = (orderByClause.length == 0 ? '1' : orderByClause.join(","));
                if(this.callback){
                    this.callback(orderBy);
                }
            })
            .build();
    }

    private buildOrderBy(clauseCollection : string[], orderBy:string, asc:boolean, defaultIfNotSpecified:string) : void {
        if (!(orderBy == null || orderBy == defaultIfNotSpecified)) {
            let stmt = (asc ? `${orderBy}` : `desc("${orderBy}")`);
            clauseCollection.push(stmt);
        }
    }


}

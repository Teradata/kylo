import {ColumnUtil} from "../column-util";
import {DataCategory,DataType} from "../../column-delegate";
import {FormConfig} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {QueryResultColumn} from "../../model/query-result-column";

export abstract class ColumnForm {

    fieldName:string;
    dataType :string;
    dataCategory:DataCategory;
    formConfig:FormConfig;


    protected constructor(protected column:any,protected grid:any, protected controller:ColumnController,protected value?:string){
        this.fieldName = ColumnUtil.getColumnFieldName(column);
        this.dataType = column.dataType;
        this.dataCategory = ColumnUtil.fromDataType(this.dataType);
        this.initializeParameters();
        this.formConfig =this.buildForm();
    }

    abstract buildForm():FormConfig;

    /**
     * initialize any parameters here that you need to reference in the formConfig
     */
    initializeParameters():void{

    }

    getColumnNames() : string[] {
        return (<any>this).controller.engine.getCols().map( (f:QueryResultColumn)=> { return f.field });
    }

    // Executes the regex formula
    protected executeRegex(regex:string, group:number) {
        const script = `regexp_extract(${this.fieldName}, "${regex}", ${group}).as("${this.fieldName}")`;
        const formula = ColumnUtil.toFormula(script, this.column, this.grid);
        this.controller.addFunction(formula, {
            formula: formula, icon: "content_cut",
            name: `Extract regex from ${this.fieldName}`
        });
    }


}

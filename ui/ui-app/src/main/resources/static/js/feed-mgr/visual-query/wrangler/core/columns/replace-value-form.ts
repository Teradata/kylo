import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {ColumnUtil} from "../column-util";
import {DataCategory, DataType} from "../../column-delegate";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";

export class ReplaceValueForm extends ColumnForm{
    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }

    buildForm(){

        // Build validator based on the type of field
        let pattern : string = ".*"
        switch (this.column.dataType) {
            case DataType.TINYINT:
            case DataType.SMALLINT:
            case DataType.INT:
            case DataType.BIGINT:
                pattern = "^-?\\d*";
                break;
            case DataType.FLOAT:
            case DataType.DOUBLE:
            case DataType.DECIMAL:
                pattern = "^-?\\d*\\.{0,1}\\d+";
                break;
            case DataType.DATE:
                pattern = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$"
                break;
            default:
                break;
        }

        let displayValue = (this.value == null || this.value == '' ? '(empty)' : this.value);

        return new DynamicFormBuilder()
            .setTitle("Replace value:")
            .setMessage(`Replaces ${displayValue} with a new value.`)
            .column()
            .text().setKey("replaceValue").setType(InputType.text).setPattern(pattern).setRequired(true).setPlaceholder("Replacement value:")
            .done()
            .columnComplete()
            .onApply((values:any) => {
                let formula = '';
                let replaceValue=values.replaceValue;
                if (replaceValue == null || replaceValue == '') {
                    replaceValue = `''`
                }
                if (this.dataCategory == DataCategory.NUMERIC) {
                    let check = (this.value == null || this.value == '' ? `isnull(${this.fieldName})` : `${this.fieldName}==${this.value}`);
                    formula = ColumnUtil.toFormula(`when( ${check}, ${replaceValue}).otherwise(${this.fieldName}).as("${this.fieldName}")`, this.column, this.grid);
                } else {
                    let check = (this.value == null || this.value == '' ? `isnull(${this.fieldName})` : `${this.fieldName}=='${this.value}'`);
                    formula = ColumnUtil.toFormula(`when( ${check}, '${replaceValue}').otherwise(${this.fieldName}).as("${this.fieldName}")`, this.column, this.grid);
                }
                this.controller.addFunction(formula, {formula: formula, icon: "find_replace", name: `Replace ${displayValue} with ${replaceValue}`});
            })
            .build()
    }

}

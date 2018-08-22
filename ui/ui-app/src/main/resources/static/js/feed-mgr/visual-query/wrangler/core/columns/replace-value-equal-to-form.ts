import {DynamicFormBuilder, FormConfig} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {InputType} from "../../../../shared/dynamic-form/model/InputText";
import {ColumnForm} from "./column-form";
import {WranglerFormField} from "../../WranglerFormBuilder";
import {ColumnController} from "../../column-controller";
import {DataCategory} from "../../column-delegate";
import {ColumnUtil} from "../column-util";

export class ReplaceValueEqualToForm extends ColumnForm{
    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }

    buildForm(){
        return new DynamicFormBuilder()
            .setTitle("Replace value:")
            .setMessage("Replace '"+this.value+"' with a new value.")
            .column()
                .text()
                    .setKey("replaceValue")
                    .setPlaceholder("Replace value")
                .done()
            .columnComplete()
            .onApply((values:any) => {
                let formula = '';
                let replaceValue=values.replaceValue
                if (this.dataCategory == DataCategory.NUMERIC) {
                    if (replaceValue == null || replaceValue == '') {
                        replaceValue = `''`
                    }
                    formula = ColumnUtil.toFormula(`when(${this.fieldName}==${this.value}, ${replaceValue}).otherwise(${this.fieldName}).as("${this.fieldName}")`, this.column, this.grid);
                } else {
                    formula = ColumnUtil.toFormula(`when(${this.fieldName}=='${this.value}', '${replaceValue}').otherwise(${this.fieldName}).as("${this.fieldName}")`, this.column, this.grid);
                }
                this.controller.addFunction(formula, {formula: formula, icon: "find_replace", name: `Replace ${this.value} with ${replaceValue}`});
            }).build();
    }



}
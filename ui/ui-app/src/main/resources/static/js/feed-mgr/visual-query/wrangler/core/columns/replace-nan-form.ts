import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {ColumnUtil} from "../column-util";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";

export class ReplaceNanForm extends ColumnForm{
    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }

    buildForm(){

        return new DynamicFormBuilder()
            .setTitle("Replace value:")
            .setMessage(`Replaces empty and NAN with a new value.`)
            .column()
            .text().setKey("replaceValue").setType(InputType.text).setValue(-1).setPlaceholder("Replacement value")
            .done()
            .columnComplete()
            .onApply((values:any) => {
                let fieldName = this.fieldName;
                let replaceValue=values.replaceValue;
                let script = `when((isnan(${fieldName}.cast('double')) || ${fieldName}.cast('double') == "" || isnull(${fieldName}.cast('double'))),'${replaceValue}').otherwise(${fieldName}).as("${fieldName}")`;
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {
                    formula: formula, icon: "find_replace",
                    name: `Replace NaN with ${replaceValue}`
                });
            })
            .build()
    }

}

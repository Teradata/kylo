import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {ColumnUtil} from "../column-util";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {FormGroup, Validators} from "@angular/forms";

export class RoundNumberForm extends ColumnForm{
    constructor(column:any, grid:any,controller:ColumnController){
        super(column,grid,controller)
    }

    buildForm(){

        let fieldName = this.fieldName;

        return new DynamicFormBuilder()
            .setTitle("Round Number")
            .setMessage("Rounds a number to the specified digits (scale).")
            .column()
            .text().setKey("scale").setType(InputType.number).setRequired(true).setValue(2).setPlaceholder("Scale")
            .setValidators([Validators.min(0)])
            .setErrorMessageLookup((type:string,validationResponse:any,form:FormGroup) => {
                switch (type) {
                    case "min":
                        return "A positive integer required ";
                }
            })
            .done()
            .columnComplete()
            .onApply((values:any) => {
                let scale = values.scale;
                const script = `round(${fieldName},${scale}).as("${fieldName}")`;
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {
                    formula: formula, icon: "exposure_zero",
                    name: `Round ${fieldName} to ${scale} digits`
                });

            })
            .build()
    }

}

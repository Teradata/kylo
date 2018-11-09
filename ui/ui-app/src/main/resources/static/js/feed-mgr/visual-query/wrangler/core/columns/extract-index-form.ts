import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnUtil} from "../column-util";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {ColumnDelegate} from "../../api";
import {Validators} from "@angular/forms";

export class ExtractIndexForm extends ColumnForm{
    constructor(column:any, grid:any,controller:any){
        super(column,grid,controller)
    }

    buildForm(){

        let fieldName = this.fieldName;

        return new DynamicFormBuilder()
            .setTitle("Extract Array Element")
            .setMessage("Extracts a single element at provided index from an array into a new column.")
            .column()
            .text().setKey("colIndex").setType(InputType.number).setRequired(true).setValue(0).setPlaceholder("Array index").setValidators([Validators.min(0)]).done()
            .text().setKey("colName").setType(InputType.text).setRequired(true).setValue(`${this.fieldName}_item`).setPattern("^[a-zA-Z_][a-zA-Z0-9_]*$").setPlaceholder("New column").done()
            .columnComplete()
            .onApply((values:any) => {
                let colIndex=values.colIndex;
                let colName=values.colName;

                let formula = ColumnUtil.createAppendColumnFormula(`getItem(${fieldName},${colIndex}).as("${colName}")`, this.column, this.grid, colName);
                this.controller.addFunction(formula, {formula: formula, icon: "remove", name: `Extract item ${colIndex}`});

            })
            .build()
    }

}

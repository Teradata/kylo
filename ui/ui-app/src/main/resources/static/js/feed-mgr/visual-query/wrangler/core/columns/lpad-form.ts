import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {ColumnUtil} from "../column-util";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {FormGroup} from "@angular/forms";

export class LpadForm extends ColumnForm {
    constructor(column: any, grid: any, controller: ColumnController, length?: string) {
        super(column, grid, controller)
    }

    buildForm() {

        const pattern = ".";
        return new DynamicFormBuilder()
            .setTitle("Left Pad")
            .setMessage(`Left pad (right justify) value to specified length with a character.`)
            .column()
            .text().setKey("length").setType(InputType.number).setRequired(true).setValue(length).setPlaceholder("Fixed length of text").done()
            .text().setKey("padChar").setType(InputType.text).setRequired(true).setValue(' ').setPattern(pattern).setPlaceholder("Padding character")
            .setErrorMessageLookup((type: string, validationResponse: any, form: FormGroup) => {
                return "Single character required.";
            }).done()
            .columnComplete()
            .onApply((values: any) => {
                let length = values.length;
                let padChar = values.padChar;
                let fieldName = this.fieldName;

                const script = `lpad(${fieldName}, ${length}, "${padChar}").as("${fieldName}")`
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {formula: formula, icon: 'format_align_right', name: `Left pad ${fieldName}`});
            })
            .build()
    }


}

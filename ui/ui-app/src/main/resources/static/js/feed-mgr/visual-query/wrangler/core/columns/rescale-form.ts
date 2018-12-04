import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {AbstractControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {ColumnUtil} from "../column-util";
import {ColumnProfile} from "../../api/column-profile";
import {FormValidators} from "../../../../../../lib/dynamic-form/form-validators/form-validators";


export class RescaleForm extends ColumnForm {

    static minScaleKey = "minScale";

    static maxScaleKey = "maxScale";

    static  minScaleLabel = "Minimum range";

    static maxScaleLabel ="Maximum range";

    constructor(column: any, grid: any, controller: ColumnController, value?: string) {
        super(column, grid, controller, value)
    }




    minMaxValidator(){
        return FormValidators.minMaxFormFieldValidator(RescaleForm.minScaleKey,RescaleForm.maxScaleKey,RescaleForm.minScaleLabel,RescaleForm.maxScaleLabel);
    }

    initializeParameters(){

    }


    buildForm() {
        return new DynamicFormBuilder().setTitle("Rescale Min/Max").setMessage("Rescale feature to a given range.")
            .column()
            .text().setKey(RescaleForm.minScaleKey).setPlaceholder(RescaleForm.minScaleLabel).setType(InputType.number).setPattern("\\d*\\.{0,1}\\d+").setValue(0).setRequired(true)
            .setValidators([ this.minMaxValidator()])
            .done()
            .text().setKey(RescaleForm.maxScaleKey).setPlaceholder(RescaleForm.maxScaleLabel).setType(InputType.number).setPattern("^-?\\d*\\.{0,1}\\d+").setValue(1).setRequired(true)
            .setValidators([this.minMaxValidator()])
            .done()
            .columnComplete()
            .onApply((values: any) => {
                let minScale: number = values.minScale;
                let maxScale: number = values.maxScale;
                let fieldName = this.fieldName;

                this.controller.extractColumnStatistics(fieldName).then((profileData: ColumnProfile) => {
                    let min = profileData.min;
                    let max = profileData.max;
                    let algo: string;
                    if (min === max) {
                        algo = `(0.5*((${minScale})+(${maxScale})))`
                    } else {
                        algo = `(((${fieldName}-(${min}))/((${max})-(${min})))*((${maxScale})-(${minScale})+(${minScale})))`
                    }
                    let script = `when(${algo}>${maxScale},${maxScale}).when(${algo}<${minScale},${minScale}).otherwise(${algo}).as("${fieldName}")`

                    const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                    this.controller.addFunction(formula, {
                        formula: formula, icon: "functions",
                        name: "Rescale " + ColumnUtil.getColumnDisplayName(this.column)
                    });
                })
            })
            .build()
    }
}

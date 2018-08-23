import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {AbstractControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {InputType} from "../../../../shared/dynamic-form/model/InputText";
import {ColumnUtil} from "../column-util";
import {ProfileHelper} from "../../api/profile-helper";


export class RescaleForm extends ColumnForm {

    private form: FormGroup;

    constructor(column: any, grid: any, controller: ColumnController, value?: string) {
        super(column, grid, controller, value)
        this.form = new FormGroup({});
    }

    minMaxValidator(form:FormGroup, minKey:string, maxKey:string) :ValidatorFn {
        return (control: AbstractControl): {[key: string]: any} | null => {
            let minValue = form.get(minKey).value;
            let maxValue = form.get(maxKey).value;
            if(minValue >= maxValue){
                return {"minMaxError":true}
            }
            else {
                return null;
            }
        }
    }

    buildForm() {
        return new DynamicFormBuilder().setTitle("Rescale Min/Max").setForm(this.form).setMessage("Rescale feature to a given range.")
            .column()
            .text().setKey("minScale").setPlaceholder("Minimum range").setType(InputType.text).setPattern("\\d*\\.{0,1}\\d+").setValue(0).setRequired(true)
            .setValidators([ this.minMaxValidator(this.form, "minScale", "maxScale") ])
            .setErrorMessageLookup((type:string,validationResponse:any,form:FormGroup) => {
                switch (type) {
                    case "minMaxError":
                        return "Min must be less than max.";

                }
            }).done()
            .text().setKey("maxScale").setPlaceholder("Maximum range").setType(InputType.text).setPattern("^-?\\d*\\.{0,1}\\d+").setValue(1).setRequired(true)
            .setErrorMessageLookup((type:string,validationResponse:any,form:FormGroup) => {
                switch (type) {
                    case "minMaxError":
                        return "Min must be less than max.";

                }
            }).done()
            .columnComplete()
            .onApply((values: any) => {
                let minScale: number = values.minScale;
                let maxScale: number = values.maxScale;
                let fieldName = this.fieldName;

                this.controller.extractColumnStatistics(fieldName).then((profileData: ProfileHelper) => {
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
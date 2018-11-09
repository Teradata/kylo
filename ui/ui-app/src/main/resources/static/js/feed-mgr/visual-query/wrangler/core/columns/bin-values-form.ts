import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {FormGroup, Validators} from "@angular/forms";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {ColumnUtil} from "../column-util";

export class BinValuesForm extends ColumnForm {

    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }


    buildForm() {
        return new DynamicFormBuilder().setTitle("Bin Values")
            .column()
              .text()
                .setKey("bin")
                .setType(InputType.number)
                .setPlaceholder("# of bins")
                .setValue(4)
                .setRequired(true)
                .setValidators([Validators.min(2)])
                .setErrorMessageLookup((type:string,validationResponse:any,form:FormGroup) => {
                    switch (type) {
                        case "required":
                            return "A positive integer greater than or equal to 2 is required ";
                        case "min":
                            return "A positive integer greater than or equal to "+validationResponse.min+" is required ";;

                    }
                })
            .done()
            .text()
                .setKey("sample")
                .setPlaceholder("# of rows to sample")
                .setType(InputType.number)
                .setValue(10000)
                .setValidators([Validators.min(1)])
                .setErrorMessageLookup((type:string,validationResponse:any,form:FormGroup) => {
                switch (type) {
                    case "min":
                        return "A positive integer greater than or equal to "+validationResponse.min+" is required ";;

                }})
                .setRequired(true)
            .done()
            .columnComplete()
            .onApply((values:any)=> {
                let bins:number =values.bin
                let sample:number = values.sample;

                let binSize = 1 / bins;
                let arr = []
                for (let i = 1; i < bins; i++) {
                    arr.push(i * binSize)
                }
                let quantileStats = ColumnUtil.approxQuantileFormula(this.fieldName, bins);
                this.controller.extractFormulaResult(quantileStats, sample)
                    .then((value: any) =>{
                        let formulaArray = [];
                        for (let i = 1; i < bins; i++) {
                            let val = value[i-1];
                            formulaArray.push(`when(${this.fieldName}<${val},${i})`);
                        }
                        formulaArray.push(`otherwise(${bins}).as("${this.fieldName}")`);
                        let script = formulaArray.join(".");
                        const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                        this.controller.addFunction(formula, {
                            formula: formula, icon: "functions",
                            name: "Bin " + ColumnUtil.getColumnDisplayName(this.column)
                        });
                    })
            })
            .build()
     

    }
}

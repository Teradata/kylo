import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {Validators} from "@angular/forms";
import {InputType} from "../../../../shared/dynamic-form/model/InputText";
import {ColumnUtil} from "../column-util";


/**
 *   dialog.withTitle(`Bin Values`)
 .inputbox("bin").withLabel("# of bins:").minIntValidator(2).default(4).build()
 .inputbox("sample").withLabel("# of rows to sample:").intNumeric().default(10000).build()
 .showDialog((fields:Map<String,WranglerFormField>)=> {
                    let bins=fields['bins'].getValueAsNumber();
                    let sample=fields['sample'].getValueAsNumber();

                    let binSize = 1 / bins;
                    let arr = []
                    for (let i = 1; i < bins; i++) {
                        arr.push(i * binSize)
                    }
                    let quantileStats = self.approxQuantileFormula(fieldName, bins);
                    self.controller.extractFormulaResult(quantileStats, sample)
                        .then(function (value: any) {
                            let formulaArray = [];
                            for (let i = 1; i < bins; i++) {
                                let val = value[i-1];
                                formulaArray.push(`when(${fieldName}<${val},${i})`);
                            }
                            formulaArray.push(`otherwise(${bins}).as("${fieldName}")`);
                            let script = formulaArray.join(".");
                            const formula = self.toFormula(script, column, grid);
                            self.controller.addFunction(formula, {
                                formula: formula, icon: "functions",
                                name: "Bin " + self.getColumnDisplayName(column)
                            });
                        })
                });
 */

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
                .setPlaceholder("# of bins:")
                .setValue(4)
                .setValidators([Validators.min(2)])
            .done()
            .text()
                .setKey("sample")
                .setPlaceholder("# of rows to sample:")
                .setType(InputType.number)
                .setValue(10000)
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
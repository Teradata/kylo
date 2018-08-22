import {DynamicFormBuilder, FormConfig} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {InputType} from "../../../../shared/dynamic-form/model/InputText";
import {ColumnForm} from "./column-form";
import {WranglerFormField} from "../../WranglerFormBuilder";
import {ColumnController} from "../../column-controller";
import {ChainedOperation, DataCategory} from "../../column-delegate";
import {ColumnUtil} from "../column-util";
import {QueryResultColumn} from "../../model/query-result-column";

export class CrossTabForm extends ColumnForm{
    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }

    buildForm(){
        return new DynamicFormBuilder()
            .setTitle("Crosstab")
            .column()
                .select()
                    .setKey("crossColumn")
                    .setPlaceholder("Crosstab column:")
                    .setOptions(this.getColumnNames().map(col => {
                        return {label:col,value:col};
                     }))
                .done()
            .columnComplete()
            .onApply((values:any) => {
                let crossColumn=values.crossColumn;

                let crossColumnTemp = (crossColumn == this.fieldName ? crossColumn + "_0" : crossColumn);
                let clean = ColumnUtil.createCleanFieldFormula(crossColumn, crossColumnTemp);
                const cleanFormula = `select(${this.fieldName}, ${clean})`;
                let chainedOp: ChainedOperation = new ChainedOperation(2);
                let crossColumnName = crossColumn;
                this.controller.setChainedQuery(chainedOp);
                this.controller.pushFormula(cleanFormula, {formula: cleanFormula, icon: 'spellcheck', name: `Clean ${this.fieldName} and ${crossColumn}`}, true, false).then( () => {
                    chainedOp.nextStep();
                    const formula = `crosstab("${this.fieldName}","${crossColumnTemp}")`;
                    this.controller.addFunction(formula, {formula: formula, icon: 'poll', name: `Crosstab ${this.fieldName} and ${crossColumnName}`});
                });
            }).build();
    }





}
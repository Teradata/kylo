import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {ColumnForm} from "./column-form";
import {ColumnUtil} from "../column-util";


export class ImputeMissingValuesForm extends ColumnForm {

    fields:string[] = []

    constructor(column: any, grid: any, controller: ColumnController, value?: string) {
        super(column, grid, controller, value)
    }

    buildForm() {
        this.fields = ColumnUtil.toColumnArray(this.grid.columns, this.fieldName)
        return new DynamicFormBuilder()
            .setTitle("Impute missing values")
            .setMessage("Provide windowing options for sourcing fill-values")
            .column()
            .select().setKey("groupBy").setPlaceholder("Grouping field").setOptionsArray(this.fields).setRequired(true).done()
            .select().setKey("orderBy").setPlaceholder("Ordering field").setOptionsArray(this.fields).setRequired(true).done()
            .columnComplete()
            .onApply((values: any) => {
                let script = `coalesce(${this.fieldName}, last(${this.fieldName}, true).over(partitionBy(${values.groupBy}).orderBy(${values.orderBy}))).as("${this.fieldName}")`;
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {formula: formula, icon: "functions", name: `Impute missing values ${this.fieldName}`});
            })
            .build();

    }
}

import {ColumnUtil} from "../column-util";
import {ColumnForm} from "./column-form";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnController} from "../../column-controller";
import {FormGroup} from "@angular/forms";

export class FillForwardForm extends ColumnForm {

    private form: FormGroup;

    constructor(column: any, grid: any, controller: ColumnController, value?: string) {
        super(column, grid, controller, value)
        this.form = new FormGroup({});
    }

    buildForm() {
        let fieldName = this.fieldName;
        let columns = ColumnUtil.toColumnArray(this.grid.columns, this.fieldName)
        return new DynamicFormBuilder().setTitle("Fill forward").setMessage("Specify windowing options to source fill-forward values:")
            .column()
            .select().setKey("groupBy").setPlaceholder("Group By").setOptionsArray(columns).setRequired(true).done()
            .select().setKey("orderBy").setPlaceholder("Order By").setOptionsArray(columns).setRequired(true).done()
            .columnComplete()
            .onApply((values: any) => {
                let groupBy: string = values.groupBy;
                let orderBy: string = values.orderBy;

                const script = `coalesce(${fieldName}, last(${fieldName}, true).over(partitionBy(${groupBy}).orderBy(${orderBy}))).as("${fieldName}")`;
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {formula: formula, icon: "functions", name: `Fill-forward missing values ${fieldName}`});
                })
            .build()
            }

}

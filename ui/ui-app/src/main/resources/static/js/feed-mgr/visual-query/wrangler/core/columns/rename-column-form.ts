import {ColumnController} from "../../column-controller";
import {ColumnUtil} from "../column-util";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {StringUtils} from "../../../../../common/utils/StringUtils";

/**
 * Rename a column form
 */
export class RenameColumnForm extends ColumnForm{

    constructor(column:any, grid:any,controller:ColumnController, value?:string){
        super(column,grid,controller,value)
    }

    buildForm(){
        // @formatter:off
        return new DynamicFormBuilder()
            .setTitle("Rename Column")
            .setMessage("Enter a new name for the " + ColumnUtil.getColumnDisplayName(this.column) + " column:")
            .column()
                .text()
                    .setKey("columnName")
                    .setPlaceholder("Column name")
                    .setRequired(true)
                .done()
            .columnComplete()
            // @formatter:on
            .onApply((values:any)=> {

                let newName = values.columnName;
                // Update field policy
                if (this.column.index < this.controller.fieldPolicies.length) {
                    const name = ColumnUtil.getColumnFieldName(this.column);
                    const policy = this.controller.fieldPolicies[this.column.index];
                    policy.name = name;
                    policy.fieldName = name;
                    policy.feedFieldName = name;
                }

                // Add rename function
                const script = ColumnUtil.getColumnFieldName(this.column) + ".as(\"" + StringUtils.singleQuote(newName) + "\")";
                const formula = ColumnUtil.toFormula(script, this.column, this.grid);
                this.controller.addFunction(formula, {
                    formula: formula, icon: "mode_edit",
                    name: "Rename " + ColumnUtil.getColumnDisplayName(this.column) + " to " + newName
                });

            }).build()
    }
}

import {ColumnDelegate, DataCategory} from "../column-delegate";

/**
 * Handles operations on columns from Teradata.
 */
export class TeradataColumnDelegate extends ColumnDelegate {

    /**
     * Hides the specified column.
     *
     * @param column - the column to be hidden
     * @param grid - the grid with the column
     */
    hideColumn(column: any, grid: any): any {
        column.visible = false;

        let formula = "";
        grid.columns.forEach(function (item: any) {
            if (item.visible) {
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += (item.field === column.field) ? "" : item.field;
            }
        });
        formula += ")";
        this.controller.pushFormula(formula, {formula: formula, icon: "remove_circle", name: "Hide " + column.displayName});

        grid.onColumnsChange();
        grid.refresh();
    }

    /**
     * Displays a dialog prompt to rename the specified column.
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    renameColumn(column: any, grid: any) {
        const self = this;
        const prompt = (this.$mdDialog as any).prompt({
            title: "Rename Column",
            textContent: "Enter a new name for the " + column.displayName + " column:",
            placeholder: "Column name",
            ok: "OK",
            cancel: "Cancel"
        });
        this.$mdDialog.show(prompt).then(function (name) {
            const script = column.displayName + ".as(\"" + StringUtils.quote(name) + "\")";
            const formula = self.toFormula(script, column, grid);
            self.controller.pushFormula(formula, {
                formula: formula, icon: "mode_edit",
                name: "Rename " + column.field + " to " + name
            });

            column.displayName = name;
        });
    }

    /**
     * Gets the transformations for a column based on category.
     *
     * @param dataCategory - the category for the column
     * @returns the transformations for the column
     */
    protected getTransforms(dataCategory: DataCategory) {
        const transforms = [];

        if (dataCategory === DataCategory.DATETIME) {
            transforms.push({description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'td_day_of_month'},
                {description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'td_day_of_year'},
                {description: 'Month of', icon: 'today', name: 'Month', operation: 'td_month_of_year'},
                {description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'td_quarter_of_year'},
                {description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'td_week_of_year'},
                {description: 'Year of', icon: 'today', name: 'Year', operation: 'td_year_of_calendar'});
        }
        if (dataCategory === DataCategory.NUMERIC) {
            transforms.push({description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceiling'},
                {description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor'},
                {icon: 'swap_vert', name: 'Round', operation: 'round'},
                {descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'degrees'},
                {descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'radians'});
        }
        if (dataCategory === DataCategory.STRING) {
            transforms.push({description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower'},
                {description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper'});
        }

        return transforms;
    }
}

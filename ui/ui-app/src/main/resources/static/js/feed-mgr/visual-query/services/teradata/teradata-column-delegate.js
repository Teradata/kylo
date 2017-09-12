var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "../column-delegate"], function (require, exports, column_delegate_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Handles operations on columns from Teradata.
     */
    var TeradataColumnDelegate = (function (_super) {
        __extends(TeradataColumnDelegate, _super);
        function TeradataColumnDelegate() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        /**
         * Hides the specified column.
         *
         * @param column - the column to be hidden
         * @param grid - the grid with the column
         */
        TeradataColumnDelegate.prototype.hideColumn = function (column, grid) {
            column.visible = false;
            var formula = "";
            grid.columns.forEach(function (item) {
                if (item.visible) {
                    formula += (formula.length == 0) ? "select(" : ", ";
                    formula += (item.field === column.field) ? "" : item.field;
                }
            });
            formula += ")";
            this.controller.pushFormula(formula, { formula: formula, icon: "remove_circle", name: "Hide " + column.displayName });
            grid.onColumnsChange();
            grid.refresh();
        };
        /**
         * Displays a dialog prompt to rename the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be renamed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        TeradataColumnDelegate.prototype.renameColumn = function (column, grid) {
            var self = this;
            var prompt = this.$mdDialog.prompt({
                title: "Rename Column",
                textContent: "Enter a new name for the " + column.displayName + " column:",
                placeholder: "Column name",
                ok: "OK",
                cancel: "Cancel"
            });
            this.$mdDialog.show(prompt).then(function (name) {
                var script = column.displayName + ".as(\"" + StringUtils.quote(name) + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.pushFormula(formula, {
                    formula: formula, icon: "mode_edit",
                    name: "Rename " + column.field + " to " + name
                });
                column.displayName = name;
            });
        };
        /**
         * Gets the transformations for a column based on category.
         *
         * @param dataCategory - the category for the column
         * @returns the transformations for the column
         */
        TeradataColumnDelegate.prototype.getTransforms = function (dataCategory) {
            var transforms = [];
            if (dataCategory === column_delegate_1.DataCategory.DATETIME) {
                transforms.push({ description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'td_day_of_month' }, { description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'td_day_of_year' }, { description: 'Month of', icon: 'today', name: 'Month', operation: 'td_month_of_year' }, { description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'td_quarter_of_year' }, { description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'td_week_of_year' }, { description: 'Year of', icon: 'today', name: 'Year', operation: 'td_year_of_calendar' });
            }
            if (dataCategory === column_delegate_1.DataCategory.NUMERIC) {
                transforms.push({ description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceiling' }, { description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor' }, { icon: 'swap_vert', name: 'Round', operation: 'round' }, { descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'degrees' }, { descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'radians' });
            }
            if (dataCategory === column_delegate_1.DataCategory.STRING) {
                transforms.push({ description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower' }, { description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper' });
            }
            return transforms;
        };
        return TeradataColumnDelegate;
    }(column_delegate_1.ColumnDelegate));
    exports.TeradataColumnDelegate = TeradataColumnDelegate;
});
//# sourceMappingURL=teradata-column-delegate.js.map
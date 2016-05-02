angular.module(MODULE_FEED_MGR).factory("VisualQueryColumnDelegate", function($mdDialog, uiGridConstants) {

    /**
     * Handles operations on columns.
     *
     * @constructor
     * @param {VisualQueryTransformController} controller the visual query transform controller
     */
    function VisualQueryColumnDelegate(controller) {
        /**
         * Visual query transform controller.
         *
         * @type {VisualQueryTransformController}
         */
        this.controller = controller;

        /**
         * List of column filters.
         *
         * @type {Array.<{condition: number}>}
         */
        this.filters = [
            {condition: uiGridConstants.filter.LESS_THAN},
            {condition: uiGridConstants.filter.GREATER_THAN},
            {condition: uiGridConstants.filter.EXACT}
        ];
    }

    angular.extend(VisualQueryColumnDelegate.prototype, {
        /**
         * Hides the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        hideColumn: function(column, grid) {
            column.colDef.visible = false;
            column.visible = false;

            this.controller.addFunction("drop(\"" + StringUtils.quote(column.field) + "\")");

            grid.queueGridRefresh();
            grid.api.core.notifyDataChange("column");
            grid.api.core.raise.columnVisibilityChanged(column);
        },

        /**
         * Unsorts the specified column.
         *
         * @param {ui.grid.GridColumn} column the column
         * @param {ui.grid.Grid} grid the grid with the column
         */
        removeSort: function(column, grid) {
            column.unsort();
            grid.refresh();
        },

        /**
         * Displays a dialog prompt to rename the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be renamed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        renameColumn: function(column, grid) {
            var self = this;
            var prompt = $mdDialog.prompt({
                title: "Rename Column",
                textContent: "Enter a new name for the " + column.displayName + " column:",
                placeholder: "Column name",
                ok: "OK",
                cancel: "Cancel"
            });
            $mdDialog.show(prompt).then(function (name) {
                column.displayName = name;

                var script = column.field + ".as(\"" + StringUtils.quote(name) + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.pushFormula(formula);
            });
        },

        /**
         * Sorts the specified column.
         *
         * @param {string} direction "ASC" to sort ascending, or "DESC" to sort descending
         * @param {ui.grid.GridColumn} column the column to be sorted
         * @param {ui.grid.Grid} grid the grid with the column
         */
        sortColumn: function(direction, column, grid) {
            grid.sortColumn(column, direction, true);
            grid.refresh();
        },

        /**
         * Executes the specified operation on the column.
         *
         * @param {string} operation the operation to be executed
         * @param {ui.grid.GridColumn} column the column to be transformed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        transformColumn: function(operation, column, grid) {
            var script = operation + "(" + column.field + ").as(\"" + StringUtils.quote(column.field) + "\")";
            var formula = this.toFormula(script, column, grid);
            this.controller.addFunction(formula);
        },

        /**
         * Creates a formula that replaces the specified column with the specified script.
         *
         * @private
         * @param {string} script the expression for the column
         * @param {ui.grid.GridColumn} column the column to be replaced
         * @param {ui.grid.Grid} grid the grid with the column
         * @returns {string} a formula that replaces the column
         */
        toFormula: function(script, column, grid) {
            var formula = "";

            angular.forEach(grid.columns, function(item) {
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += (item.field === column.field) ? script : item.field;
            });

            formula += ")";
            return formula;
        }
    });

    return VisualQueryColumnDelegate;
});

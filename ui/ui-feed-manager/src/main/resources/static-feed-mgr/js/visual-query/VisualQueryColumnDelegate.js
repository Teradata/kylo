angular.module(MODULE_FEED_MGR).factory("VisualQueryColumnDelegate", function(uiGridConstants) {

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
            var formula = "";

            angular.forEach(grid.columns, function(item) {
                formula += (formula.length == 0) ? "select(" : ", ";
                if (item.field == column.field) {
                    formula += operation + "(" + item.field + ").as(\"" + StringUtils.quote(item.field) + "\")";
                }
                else {
                    formula += item.field;
                }
            });

            formula += ")";
            this.controller.addFunction(formula);
        }
    });

    return VisualQueryColumnDelegate;
});

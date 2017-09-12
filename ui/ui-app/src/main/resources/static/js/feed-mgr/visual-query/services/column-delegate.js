define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Categories for data types.
     */
    var DataCategory;
    (function (DataCategory) {
        DataCategory[DataCategory["ARRAY"] = 0] = "ARRAY";
        DataCategory[DataCategory["BINARY"] = 1] = "BINARY";
        DataCategory[DataCategory["BOOLEAN"] = 2] = "BOOLEAN";
        DataCategory[DataCategory["DATETIME"] = 3] = "DATETIME";
        DataCategory[DataCategory["MAP"] = 4] = "MAP";
        DataCategory[DataCategory["NUMERIC"] = 5] = "NUMERIC";
        DataCategory[DataCategory["STRING"] = 6] = "STRING";
        DataCategory[DataCategory["STRUCT"] = 7] = "STRUCT";
        DataCategory[DataCategory["UNION"] = 8] = "UNION";
        DataCategory[DataCategory["OTHER"] = 9] = "OTHER";
    })(DataCategory = exports.DataCategory || (exports.DataCategory = {}));
    /**
     * Hive data types.
     *
     * @readonly
     * @enum {string}
     */
    var DataType = {
        // Numeric types
        TINYINT: 'tinyint',
        SMALLINT: 'smallint',
        INT: 'int',
        BIGINT: 'bigint',
        FLOAT: 'float',
        DOUBLE: 'double',
        DECIMAL: 'decimal',
        // Date/time types
        TIMESTAMP: 'timestamp',
        DATE: 'date',
        // String types
        STRING: 'string',
        VARCHAR: 'varchar',
        CHAR: 'char',
        // Misc types
        BOOLEAN: 'boolean',
        BINARY: 'binary',
        // Complex types
        ARRAY: 'array',
        MAP: 'map',
        STRUCT: 'struct',
        UNION: 'uniontype'
    };
    /**
     * Handles operations on columns.
     */
    var ColumnDelegate = (function () {
        /**
         * Constructs a column delegate.
         */
        function ColumnDelegate(dataType, controller, $mdDialog, uiGridConstants) {
            this.dataType = dataType;
            this.controller = controller;
            this.$mdDialog = $mdDialog;
            this.uiGridConstants = uiGridConstants;
            this.dataCategory = this.fromDataType(dataType);
            this.filters = this.getFilters(this.dataCategory);
            this.transforms = this.getTransforms(this.dataCategory);
        }
        /**
         * Hides the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.hideColumn = function (column, grid) {
            column.visible = false;
            var formula = "drop(\"" + StringUtils.quote(column.field) + "\")";
            this.controller.pushFormula(formula, { formula: formula, icon: "remove_circle", name: "Hide " + column.displayName });
            grid.onColumnsChange();
            grid.refresh();
        };
        /**
         * Unsorts the specified column.
         *
         * @param {ui.grid.GridColumn} column the column
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.removeSort = function (column, grid) {
            column.unsort();
            grid.refresh();
        };
        /**
         * Displays a dialog prompt to rename the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be renamed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.renameColumn = function (column, grid) {
            var self = this;
            var prompt = this.$mdDialog.prompt({
                title: "Rename Column",
                textContent: "Enter a new name for the " + column.displayName + " column:",
                placeholder: "Column name",
                ok: "OK",
                cancel: "Cancel"
            });
            this.$mdDialog.show(prompt).then(function (name) {
                var script = column.field + ".as(\"" + StringUtils.quote(name) + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.pushFormula(formula, {
                    formula: formula, icon: "mode_edit",
                    name: "Rename " + column.displayName + " to " + name
                });
                column.displayName = name;
            });
        };
        /**
         * Sorts the specified column.
         *
         * @param {string} direction "ASC" to sort ascending, or "DESC" to sort descending
         * @param {ui.grid.GridColumn} column the column to be sorted
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.sortColumn = function (direction, column, grid) {
            grid.sortColumn(column, direction, true);
            grid.refresh();
        };
        /**
         * Executes the specified operation on the column.
         *
         * @param {Object} transform the transformation object from {@link VisualQueryColumnDelegate#getTransforms}
         * @param {ui.grid.GridColumn} column the column to be transformed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.transformColumn = function (transform, column, grid) {
            var script = transform.operation + "(" + column.field + ").as(\"" + StringUtils.quote(column.field) + "\")";
            var formula = this.toFormula(script, column, grid);
            var name = (transform.description ? transform.description : transform.name) + " " + column.displayName;
            this.controller.addFunction(formula, { formula: formula, icon: transform.icon, name: name });
        };
        /**
         * Validates the specified filter.
         *
         * @param {Object} filter the filter to be validated
         * @param {VisualQueryTable} table the visual query table
         */
        ColumnDelegate.prototype.validateFilter = function (filter, table) {
            if (filter.term == "") {
                filter.term = null;
            }
            else {
                delete filter.regex;
            }
            table.onRowsChange();
            table.refreshRows();
        };
        /**
         * Creates a formula that replaces the specified column with the specified script.
         *
         * @private
         * @param {string} script the expression for the column
         * @param {ui.grid.GridColumn} column the column to be replaced
         * @param {ui.grid.Grid} grid the grid with the column
         * @returns {string} a formula that replaces the column
         */
        ColumnDelegate.prototype.toFormula = function (script, column, grid) {
            var formula = "";
            angular.forEach(grid.columns, function (item) {
                if (item.visible) {
                    formula += (formula.length == 0) ? "select(" : ", ";
                    formula += (item.field === column.field) ? script : item.field;
                }
            });
            formula += ")";
            return formula;
        };
        /**
         * Converts from the specified data type to a category.
         *
         * @param dataType - the data type
         * @returns the data category
         */
        ColumnDelegate.prototype.fromDataType = function (dataType) {
            switch (dataType) {
                case DataType.TINYINT:
                case DataType.SMALLINT:
                case DataType.INT:
                case DataType.BIGINT:
                case DataType.FLOAT:
                case DataType.DOUBLE:
                case DataType.DECIMAL:
                    return DataCategory.NUMERIC;
                case DataType.TIMESTAMP:
                case DataType.DATE:
                    return DataCategory.DATETIME;
                case DataType.STRING:
                case DataType.VARCHAR:
                case DataType.CHAR:
                    return DataCategory.STRING;
                case DataType.BOOLEAN:
                    return DataCategory.BOOLEAN;
                case DataType.BINARY:
                    return DataCategory.BINARY;
                case DataType.ARRAY:
                    return DataCategory.ARRAY;
                case DataType.MAP:
                    return DataCategory.MAP;
                case DataType.STRUCT:
                    return DataCategory.STRUCT;
                case DataType.UNION:
                    return DataCategory.UNION;
            }
        };
        /**
         * Gets the filters for a column based on category.
         *
         * @param dataCategory - the category for the column
         * @returns the filters for the column
         */
        ColumnDelegate.prototype.getFilters = function (dataCategory) {
            var filters = [];
            switch (dataCategory) {
                case DataCategory.STRING:
                    filters.push({ condition: this.uiGridConstants.filter.CONTAINS, icon: 'search', label: 'Contains...' });
                // fall through
                case DataCategory.NUMERIC:
                    filters.push({
                        condition: this.uiGridConstants.filter.LESS_THAN, icon: 'keyboard_arrow_left',
                        label: 'Less than...'
                    }, {
                        condition: this.uiGridConstants.filter.GREATER_THAN, icon: 'keyboard_arrow_right',
                        label: 'Greater than...'
                    }, { condition: this.uiGridConstants.filter.EXACT, icon: '=', label: 'Equal to...' });
                    break;
                default:
            }
            return filters;
        };
        /**
         * Gets the transformations for a column based on category.
         *
         * @param dataCategory - the category for the column
         * @returns the transformations for the column
         */
        ColumnDelegate.prototype.getTransforms = function (dataCategory) {
            var transforms = [];
            if (dataCategory === DataCategory.ARRAY) {
                transforms.push({ icon: 'call_split', name: 'Explode', operation: 'explode' }, { description: 'Sort', icon: 'sort', name: 'Sort Array', operation: 'sort_array' });
            }
            if (dataCategory === DataCategory.BINARY) {
                transforms.push({ icon: '#', name: 'CRC32', operation: 'crc32' }, { icon: '#', name: 'MD5', operation: 'md5' }, { icon: '#', name: 'SHA1', operation: 'sha1' }, { icon: '#', name: 'SHA2', operation: 'sha2' });
            }
            if (dataCategory === DataCategory.DATETIME) {
                transforms.push({ description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'dayofmonth' }, { description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'dayofyear' }, { description: 'Hour of', icon: 'access_time', name: 'Hour', operation: 'hour' }, { description: 'Last day of month for', icon: 'today', name: 'Last Day of Month', operation: 'last_day' }, { description: 'Minute of', icon: 'access_time', name: 'Minute', operation: 'minute' }, { description: 'Month of', icon: 'today', name: 'Month', operation: 'month' }, { description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'quarter' }, { description: 'Second of', icon: 'access_time', name: 'Second', operation: 'second' }, { description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'weekofyear' }, { description: 'Year of', icon: 'today', name: 'Year', operation: 'year' });
            }
            if (dataCategory === DataCategory.MAP) {
                transforms.push({ icon: 'call_split', name: 'Explode', operation: 'explode' });
            }
            if (dataCategory === DataCategory.NUMERIC) {
                transforms.push({ description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil' }, { description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor' }, { icon: 'swap_vert', name: 'Round', operation: 'round' }, { descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees' }, { descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians' });
            }
            if (dataCategory === DataCategory.STRING) {
                transforms.push({ description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower' }, { description: 'Title case', icon: 'format_color_text', name: 'Title Case', operation: 'initcap' }, { icon: 'graphic_eq', name: 'Trim', operation: 'trim' }, { description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper' });
            }
            return transforms;
        };
        return ColumnDelegate;
    }());
    exports.ColumnDelegate = ColumnDelegate;
});
//# sourceMappingURL=column-delegate.js.map
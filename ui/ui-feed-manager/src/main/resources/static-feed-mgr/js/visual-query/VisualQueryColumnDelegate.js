angular.module(MODULE_FEED_MGR).factory("VisualQueryColumnDelegate", function($mdDialog, uiGridConstants) {

    /**
     * The UI context for a transformation.
     *
     * @typedef {Object} TransformContext
     * @property {string} formula the transformation formula
     * @property {string} icon the icon name
     * @property {string} name the human-readable display name for the transformation
     */

    /**
     * Categories for data types.
     *
     * @readonly
     * @enum {number}
     */
    var DataCategory = {
        ARRAY: 0,
        BINARY: 1,
        BOOLEAN: 2,
        DATETIME: 3,
        MAP: 4,
        NUMERIC: 5,
        STRING: 6,
        STRUCT: 7,
        UNION: 8,

        /**
         * Converts from the specified data type to a category.
         *
         * @param {string} dataType the data type
         * @returns {DataCategory} the data category
         */
        fromDataType: function(dataType) {
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
        }
    };

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
     *
     * @constructor
     * @param {string} dataType the type of data in the column
     * @param {VisualQueryTransformController} controller the visual query transform controller
     */
    function VisualQueryColumnDelegate(dataType, controller) {
        /**
         * Visual query transform controller.
         *
         * @type {VisualQueryTransformController}
         */
        this.controller = controller;

        /**
         * The category for the data in the column.
         *
         * @type {DataCategory}
         */
        this.dataCategory = DataCategory.fromDataType(dataType);

        /**
         * The type of data in the column.
         *
         * @type {DataType}
         */
        this.dataType = dataType;

        /**
         * List of column filters.
         *
         * @type {Object[]}
         */
        this.filters = VisualQueryColumnDelegate.getFilters(this.dataCategory);

        /**
         * List of column transformations.
         *
         * @type {Object[]}
         */
        this.transforms = VisualQueryColumnDelegate.getTransforms(this.dataCategory);
    }

    angular.extend(VisualQueryColumnDelegate, {
        /**
         * Gets the filters for a column based on category.
         *
         * @static
         * @param {DataCategory} dataCategory the category for the column
         * @returns {Object[]} the filters for the column
         */
        getFilters: function(dataCategory) {
            var filters = [];

            switch (dataCategory) {
                case DataCategory.STRING:
                    filters.push({condition: uiGridConstants.filter.CONTAINS, icon: 'search', label: 'Contains...'});
                    // fall through

                case DataCategory.NUMERIC:
                    filters.push({condition: uiGridConstants.filter.LESS_THAN, icon: 'keyboard_arrow_left',
                                label: 'Less than...'},
                            {condition: uiGridConstants.filter.GREATER_THAN, icon: 'keyboard_arrow_right',
                                label: 'Greater than...'},
                            {condition: uiGridConstants.filter.EXACT, icon: '=', label: 'Equal to...'});
                    break;

                default:
            }

            return filters;
        },

        /**
         * Gets the transformations for a column based on category.
         *
         * @static
         * @param {DataCategory} dataCategory the category for the column
         * @returns {Object[]} the transformations for the column
         */
        getTransforms: function(dataCategory) {
            var transforms = [];

            if (dataCategory === DataCategory.ARRAY) {
                transforms.push({icon: 'call_split', name: 'Explode', operation: 'explode'},
                        {description: 'Sort', icon: 'sort', name: 'Sort Array', operation: 'sort_array'});
            }
            if (dataCategory === DataCategory.BINARY) {
                transforms.push({icon: '#', name: 'CRC32', operation: 'crc32'},
                        {icon: '#', name: 'MD5', operation: 'md5'},
                        {icon: '#', name: 'SHA1', operation: 'sha1'},
                        {icon: '#', name: 'SHA2', operation: 'sha2'});
            }
            if (dataCategory === DataCategory.DATETIME) {
                transforms.push({description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'dayofmonth'},
                        {description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'dayofyear'},
                        {description: 'Hour of', icon: 'access_time', name: 'Hour', operation: 'hour'},
                        {description: 'Last day of month for', icon: 'today', name: 'Last Day of Month', operation: 'last_day'},
                        {description: 'Minute of', icon: 'access_time', name: 'Minute', operation: 'minute'},
                        {description: 'Month of', icon: 'today', name: 'Month', operation: 'month'},
                        {description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'quarter'},
                        {description: 'Second of', icon: 'access_time', name: 'Second', operation: 'second'},
                        {description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'weekofyear'},
                        {description: 'Year of', icon: 'today', name: 'Year', operation: 'year'});
            }
            if (dataCategory === DataCategory.MAP) {
                transforms.push({icon: 'call_split', name: 'Explode', operation: 'explode'});
            }
            if (dataCategory === DataCategory.NUMERIC) {
                transforms.push({description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil'},
                        {description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor'},
                        {icon: 'swap_vert', name: 'Round', operation: 'round'},
                        {descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees'},
                        {descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians'});
            }
            if (dataCategory === DataCategory.STRING) {
                transforms.push({description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower'},
                        {description: 'Title case', icon: 'format_color_text', name: 'Title Case', operation: 'initcap'},
                        {icon: 'graphic_eq', name: 'Trim', operation: 'trim'},
                        {description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper'});
            }

            return transforms;
        }
    });

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

            var formula = "drop(\"" + StringUtils.quote(column.field) + "\")";
            this.controller.pushFormula(formula, {formula: formula, icon: "remove_circle", name: "Hide " + column.displayName});

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
                var script = column.field + ".as(\"" + StringUtils.quote(name) + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.pushFormula(formula, {formula: formula, icon: "mode_edit",
                    name: "Rename " + column.displayName + " to " + name});

                column.displayName = name;
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
         * @param {Object} transform the transformation object from {@link VisualQueryColumnDelegate#getTransforms}
         * @param {ui.grid.GridColumn} column the column to be transformed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        transformColumn: function(transform, column, grid) {
            var script = transform.operation + "(" + column.field + ").as(\"" + StringUtils.quote(column.field) + "\")";
            var formula = this.toFormula(script, column, grid);
            var name = (transform.description ? transform.description : transform.name) + " " + column.displayName;
            this.controller.addFunction(formula, {formula: formula, icon: transform.icon, name: name});
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

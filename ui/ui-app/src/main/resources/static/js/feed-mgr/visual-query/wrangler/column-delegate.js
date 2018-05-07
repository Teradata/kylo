define(["require", "exports", "angular", "jquery"], function (require, exports, angular, $) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Categories for data types.
     */
    var DataCategory;
    (function (DataCategory) {
        DataCategory[DataCategory["ARRAY_DOUBLE"] = 0] = "ARRAY_DOUBLE";
        DataCategory[DataCategory["ARRAY"] = 1] = "ARRAY";
        DataCategory[DataCategory["BINARY"] = 2] = "BINARY";
        DataCategory[DataCategory["BOOLEAN"] = 3] = "BOOLEAN";
        DataCategory[DataCategory["DATETIME"] = 4] = "DATETIME";
        DataCategory[DataCategory["MAP"] = 5] = "MAP";
        DataCategory[DataCategory["NUMERIC"] = 6] = "NUMERIC";
        DataCategory[DataCategory["STRING"] = 7] = "STRING";
        DataCategory[DataCategory["STRUCT"] = 8] = "STRUCT";
        DataCategory[DataCategory["UNION"] = 9] = "UNION";
        DataCategory[DataCategory["OTHER"] = 10] = "OTHER";
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
        ARRAY_DOUBLE: 'array<double>',
        ARRAY: 'array',
        MAP: 'map',
        STRUCT: 'struct',
        UNION: 'uniontype'
    };
    /**
     * Represents a sequence of query operations
     */
    var ChainedOperation = /** @class */ (function () {
        function ChainedOperation(totalSteps) {
            if (totalSteps === void 0) { totalSteps = 1; }
            this.step = 1;
            this.totalSteps = totalSteps;
        }
        ChainedOperation.prototype.nextStep = function () {
            this.step += 1;
        };
        /**
         * Fractional of overall progress complete
         * @param {number} stepProgress the progress between 0 and 100
         * @returns {number} overall progress between 0 and 100
         */
        ChainedOperation.prototype.fracComplete = function (stepProgress) {
            var min = ((this.step - 1) / this.totalSteps);
            var max = this.step / this.totalSteps;
            return (Math.ceil((min * 100) + (max - min) * stepProgress));
        };
        ChainedOperation.prototype.isLastStep = function () {
            return this.step == this.totalSteps;
        };
        return ChainedOperation;
    }());
    exports.ChainedOperation = ChainedOperation;
    /**
     * Handles operations on columns.
     */
    var ColumnDelegate = /** @class */ (function () {
        /**
         * Constructs a column delegate.
         */
        function ColumnDelegate(dataType, controller, $mdDialog, uiGridConstants, dialog) {
            this.dataType = dataType;
            this.controller = controller;
            this.$mdDialog = $mdDialog;
            this.uiGridConstants = uiGridConstants;
            this.dialog = dialog;
            this.dataCategory = this.fromDataType(dataType);
            this.filters = this.getFilters(this.dataCategory);
            this.transforms = this.getTransforms(this.dataCategory);
        }
        /**
         * Casts this column to the specified type.
         */
        ColumnDelegate.prototype.castTo = function (dataType) {
            // not supported
        };
        ColumnDelegate.prototype.escapeRegExp = function (text) {
            return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\\\$&');
        };
        ColumnDelegate.prototype.stripValueContaining = function (value, column, grid) {
            var fieldName = this.getColumnFieldName(column);
            var regex = this.escapeRegExp(value);
            var formula = this.toFormula("regexp_replace(" + fieldName + ", \"" + regex + "\", \"\").as(\"" + fieldName + "\")", column, grid);
            this.controller.addFunction(formula, { formula: formula, icon: "content_cut", name: "Strip " + this.getColumnDisplayName(column) + " containing " + value });
        };
        ColumnDelegate.prototype.clearRowsEquals = function (value, column, grid) {
            var fieldName = this.getColumnFieldName(column);
            var formula = this.toFormula("when(equal(" + fieldName + ", '" + StringUtils.singleQuote(value) + "'),null).otherwise(" + fieldName + ").as(\"" + fieldName + "\")", column, grid);
            this.controller.addFunction(formula, { formula: formula, icon: "remove_circle", name: "Clear " + this.getColumnDisplayName(column) + " equals " + value });
        };
        /**
         * Filters for rows where the specified column is not null.
         */
        ColumnDelegate.prototype.deleteNullRows = function (column) {
            var formula = "filter(not(isnull(" + this.getColumnFieldName(column) + ")))";
            this.controller.addFunction(formula, { formula: formula, icon: "remove_circle_containing", name: "Delete " + this.getColumnDisplayName(column) + " if null" });
        };
        /**
         * Filters for rows where the specified column does not contain the specified value.
         *
         * @param value - the value to remove
         * @param column - the column
         */
        ColumnDelegate.prototype.deleteRowsContaining = function (value, column) {
            var formula = "filter(not(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.singleQuote(value) + "')))";
            this.controller.addFunction(formula, { formula: formula, icon: "search", name: "Delete " + this.getColumnDisplayName(column) + " containing " + value });
        };
        /**
         * Filters for rows where the specified column is not the specified value.
         *
         * @param value - the value to remove
         * @param column - the column
         */
        ColumnDelegate.prototype.deleteRowsEqualTo = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " != '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "≠", name: "Delete " + this.getColumnDisplayName(column) + " equal to " + value });
        };
        /**
         * Filters for rows where the specified column is less than or equal to the specified value.
         *
         * @param value - the maximum value (inclusive)
         * @param column - the column
         */
        ColumnDelegate.prototype.deleteRowsGreaterThan = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " <= '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "≯", name: "Delete " + this.getColumnDisplayName(column) + " greater than " + value });
        };
        /**
         * Filters for rows where the specified column is greater than or equal to the specified value.
         *
         * @param value - the minimum value (inclusive)
         * @param column - the column
         */
        ColumnDelegate.prototype.deleteRowsLessThan = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " >= '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "≮", name: "Delete " + this.getColumnDisplayName(column) + " less than " + value });
        };
        /**
         * Filters for rows where the specified column is null.
         */
        ColumnDelegate.prototype.findNullRows = function (column) {
            var formula = "filter(isnull(" + this.getColumnFieldName(column) + "))";
            this.controller.addFunction(formula, { formula: formula, icon: "=", name: "Find where " + this.getColumnDisplayName(column) + " is null" });
        };
        /**
         * Filters for rows where the specified column contains the specified value.
         *
         * @param value - the value to find
         * @param column - the column
         */
        ColumnDelegate.prototype.findRowsContaining = function (value, column) {
            var formula = "filter(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.singleQuote(value) + "'))";
            this.controller.addFunction(formula, { formula: formula, icon: "search", name: "Find " + this.getColumnDisplayName(column) + " containing " + value });
        };
        /**
         * Filters for rows where the specified column is the specified value.
         *
         * @param value - the value to find
         * @param column - the column
         */
        ColumnDelegate.prototype.findRowsEqualTo = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " == '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "=", name: "Find " + this.getColumnDisplayName(column) + " equal to " + value });
        };
        /**
         * Filters for rows where the specified column is greater than the specified value.
         *
         * @param value - the minimum value (exclusive)
         * @param column - the column
         */
        ColumnDelegate.prototype.findRowsGreaterThan = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " > '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "keyboard_arrow_right", name: "Find " + this.getColumnDisplayName(column) + " greater than " + value });
        };
        /**
         * Filters for rows where the specified column is less than the specified value.
         *
         * @param value - the maximum value (exclusive)
         * @param column - the column
         */
        ColumnDelegate.prototype.findRowsLessThan = function (value, column) {
            var formula = "filter(" + this.getColumnFieldName(column) + " < '" + StringUtils.singleQuote(value) + "')";
            this.controller.addFunction(formula, { formula: formula, icon: "keyboard_arrow_left", name: "Find " + this.getColumnDisplayName(column) + " less than " + value });
        };
        /**
         * Hides the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.hideColumn = function (column, grid) {
            column.visible = false;
            var formula = "drop(\"" + StringUtils.singleQuote(column.headerTooltip) + "\")";
            this.controller.pushFormula(formula, { formula: formula, icon: "remove_circle", name: "Hide " + this.getColumnDisplayName(column) });
            this.controller.fieldPolicies = this.controller.fieldPolicies.filter(function (value, index) { return index == column.index; });
        };
        /**
         * Display the analyze column view
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.showAnalyzeColumn = function (column, grid) {
            var fieldName = this.getColumnFieldName(column);
            this.controller.showAnalyzeColumn(fieldName);
        };
        /**
         * Clone the specified column.
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.cloneColumn = function (column, grid) {
            var fieldName = this.getColumnFieldName(column);
            var script = "clone(" + fieldName + ")";
            var formula = this.toAppendColumnFormula(script, column, grid);
            this.controller.addFunction(formula, { formula: formula, icon: 'content_copy', name: 'Clone ' + this.getColumnDisplayName(column) });
        };
        /**
         * Imputes the values using mean
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.imputeMeanColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var script = "when(or(isnull(" + fieldName + "),isnan(" + fieldName + ")),mean(" + fieldName + ").over(orderBy(1))).otherwise(" + fieldName + ").as(\"" + fieldName + "\")";
            var formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, { formula: formula, icon: 'functions', name: 'Impute mean ' + self.getColumnDisplayName(column) });
        };
        /**
         * Crosstab against another column
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.crosstabColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var cols = self.controller.engine.getCols();
            self.$mdDialog.show({
                clickOutsideToClose: true,
                controller: (_a = /** @class */ (function () {
                        function class_1($mdDialog) {
                            this.$mdDialog = $mdDialog;
                            this.columns = cols;
                            this.crossColumn = "";
                        }
                        class_1.prototype.valid = function () {
                            return (this.crossColumn != "");
                        };
                        class_1.prototype.cancel = function () {
                            this.$mdDialog.hide();
                        };
                        class_1.prototype.apply = function () {
                            this.$mdDialog.hide();
                            var crossColumnTemp = (this.crossColumn == fieldName ? this.crossColumn + "_0" : this.crossColumn);
                            var clean2 = self.createCleanFieldFormula(this.crossColumn, crossColumnTemp);
                            var cleanFormula = "select(" + fieldName + ", " + clean2 + ")";
                            var chainedOp = new ChainedOperation(2);
                            var crossColumnName = this.crossColumn;
                            self.controller.setChainedQuery(chainedOp);
                            self.controller.pushFormula(cleanFormula, { formula: cleanFormula, icon: 'spellcheck', name: "Clean " + fieldName + " and " + this.crossColumn }, true, false).then(function () {
                                chainedOp.nextStep();
                                var formula = "crosstab(\"" + fieldName + "\",\"" + crossColumnTemp + "\")";
                                self.controller.addFunction(formula, { formula: formula, icon: 'poll', name: "Crosstab " + fieldName + " and " + crossColumnName });
                            });
                        };
                        return class_1;
                    }()),
                    _a.$inject = ["$mdDialog"],
                    _a),
                controllerAs: "dialog",
                parent: angular.element("body"),
                template: "\n                  <md-dialog arial-label=\"error executing the query\" style=\"max-width: 640px;\">\n                    <md-dialog-content class=\"md-dialog-content\" role=\"document\" tabIndex=\"-1\">\n                      <h2 class=\"md-title\">Select crosstab field:</h2>\n\n                      <md-input-container>\n                        <label>Cross column:</label>\n                        <md-select ng-model=\"dialog.crossColumn\" >\n                            <md-option ng-repeat=\"x in dialog.columns\" value=\"{{x.field}}\">\n                                {{x.field}}\n                            </md-option>\n                        </md-select>\n                      </md-input-container>\n                    </md-dialog-content>\n                    <md-dialog-actions>\n                      <md-button ng-click=\"dialog.cancel()\" class=\"md-cancel-button\" md-autofocus=\"false\">Cancel</md-button>\n                      <md-button ng-click=\"dialog.apply()\" ng-disabled=\"!dialog.valid()\" class=\"md-primary md-confirm-button\" md-autofocus=\"true\">Ok</md-button>\n                    </md-dialog-actions>\n                  </md-dialog>\n                "
            });
            var _a;
        };
        /**
         * Generates a script to use a temp column with the desired result and replace the existing column and ordering for
         * which the temp column was derived. This is used by some of the machine
         * learning functions that don't return column types
         * @returns {string}
         */
        ColumnDelegate.prototype.generateRenameScript = function (fieldName, tempField, grid) {
            // Build select script to drop temp column we generated
            var self = this;
            var cols = [];
            angular.forEach(grid.columns, function (col) {
                var colName = self.getColumnFieldName(col);
                if (colName != tempField) {
                    colName = (colName == fieldName ? tempField + ".as(\"" + fieldName + "\")" : colName);
                    cols.push(colName);
                }
            });
            var selectCols = cols.join();
            var renameScript = "select(" + selectCols + ")";
            return renameScript;
        };
        /**
         * Generates a script to move the column B directly to the right of column A
         * @returns {string}
         */
        ColumnDelegate.prototype.generateMoveScript = function (fieldNameA, fieldNameB, columnSource) {
            var self = this;
            var cols = [];
            var sourceColumns = (columnSource.columns ? columnSource.columns : columnSource);
            angular.forEach(sourceColumns, function (col) {
                var colName = self.getColumnFieldName(col);
                if (colName == fieldNameA) {
                    cols.push(colName);
                    cols.push(fieldNameB);
                }
                else if (colName != fieldNameB) {
                    cols.push(colName);
                }
            });
            var selectCols = cols.join();
            return "select(" + selectCols + ")";
        };
        /**
         * Attempt to determine number of elements in array
         * @param {string} text
         * @returns {string}
         */
        ColumnDelegate.prototype.arrayItems = function (text) {
            return (text && text.length > 0 ? text.split(",").length : 1);
        };
        /**
         * Extract array items into columns
         * @param column
         * @param grid
         */
        ColumnDelegate.prototype.extractArrayItems = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var count = 0;
            // Sample rows determine how many array elements
            if (grid.rows != null && grid.rows.length > 0) {
                var idx_1 = 0;
                angular.forEach(grid.columns, function (col, key) {
                    if (col.name == fieldName)
                        idx_1 = key;
                });
                angular.forEach(grid.rows, function (row) {
                    count = (row[idx_1] != null && row[idx_1].length > count ? row[idx_1].length : count);
                });
            }
            var columns = self.toColumnArray(grid.columns, fieldName);
            //var i=0;
            for (var i = 0; i < count; i++) {
                var newFieldName = fieldName + "_" + i;
                columns.push("getItem(" + fieldName + ", " + i + ").as(\"" + newFieldName + "\")");
            }
            var formula = "select(" + columns.join(",");
            self.controller.pushFormula(formula, { formula: formula, icon: "functions", name: "Extract array" }, true, true);
        };
        /**
         * Adds string labels to indexes
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.indexColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var newFieldName = fieldName + "_indexed";
            var formula = "StringIndexer().setInputCol(\"" + fieldName + "\").setOutputCol(\"" + newFieldName + "\").run(select(" + fieldName + "))";
            var moveFormula = self.generateMoveScript(fieldName, newFieldName, grid);
            // Two part conversion
            var chainedOp = new ChainedOperation(2);
            self.controller.setChainedQuery(chainedOp);
            self.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'Index ' + self.getColumnDisplayName(column) }, true, false)
                .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(moveFormula, { formula: formula, icon: 'functions', name: 'Move new column next to ' + fieldName });
            });
        };
        /**
         * Vectorize a numeric column as a double array
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.vectorizeColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var tempField = self.createTempField();
            var formula = "vectorAssembler([\"" + fieldName + "\"], \"" + tempField + "\")";
            var renameScript = self.generateRenameScript(fieldName, tempField, grid);
            // Two part conversion
            var chainedOp = new ChainedOperation(2);
            self.controller.setChainedQuery(chainedOp);
            self.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'Vectorize ' + self.getColumnDisplayName(column) }, true, false)
                .then(function result() {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, { formula: formula, icon: 'functions', name: 'Remap temp vector column to ' + fieldName });
            });
        };
        /**
         * Rescale the vector column
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         * @param boolean use mean
         * @param boolean use stdDev (normally default)
         */
        ColumnDelegate.prototype.rescaleColumn = function (self, column, grid, mean, stdDev) {
            var fieldName = self.getColumnFieldName(column);
            var tempField = self.createTempField();
            var formula = "StandardScaler().setInputCol(\"" + fieldName + "\").setOutputCol(\"" + tempField + "\").setWithMean(" + mean + ").setWithStd(" + stdDev + ").run(select(" + fieldName + "))";
            var renameScript = self.generateRenameScript(fieldName, tempField, grid);
            // Two part conversion
            var chainedOp = new ChainedOperation(2);
            self.controller.setChainedQuery(chainedOp);
            self.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'Std Dev. rescale ' + self.getColumnDisplayName(column) }, true, false)
                .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, { formula: formula, icon: 'functions', name: 'Remap temp rescaled column to ' + fieldName });
            });
        };
        /**
         * Rescale the vector column between min/max
         * @param self
         * @param column
         * @param grid
         */
        ColumnDelegate.prototype.rescaleMinMax = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var tempField = self.createTempField();
            var formula = "MinMaxScaler().setInputCol(\"" + fieldName + "\").setOutputCol(\"" + tempField + "\").run(select(" + fieldName + "))";
            var renameScript = self.generateRenameScript(fieldName, tempField, grid);
            // Two part conversion
            var chainedOp = new ChainedOperation(2);
            self.controller.setChainedQuery(chainedOp);
            self.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'MinMax rescale ' + self.getColumnDisplayName(column) }, true, false)
                .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, { formula: formula, icon: 'functions', name: 'Remap temp rescaled column to ' + fieldName });
            });
        };
        /**
         * Rescale the vector column using the standard deviation
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.rescaleStdDevColumn = function (self, column, grid) {
            self.rescaleColumn(self, column, grid, false, true);
        };
        /**
         * Rescale the vector column using the mean
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.rescaleMeanColumn = function (self, column, grid) {
            self.rescaleColumn(self, column, grid, true, false);
        };
        /**
         * Rescale using mean and stdDev
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.rescaleBothMethodsColumn = function (self, column, grid) {
            self.rescaleColumn(self, column, grid, true, true);
        };
        ColumnDelegate.prototype.toColumnArray = function (columns, ommitColumn) {
            var self = this;
            var cols = [];
            angular.forEach(columns, function (column) {
                if (!ommitColumn || (ommitColumn && ommitColumn != column.name)) {
                    cols.push(self.getColumnFieldName(column));
                }
            });
            return cols;
        };
        ColumnDelegate.prototype.imputeMissingColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            self.dialog.openImputeMissing({
                message: 'Provide windowing options for sourcing fill-values:',
                fields: self.toColumnArray(grid.columns, fieldName)
            }).subscribe(function (response) {
                var script = "coalesce(" + fieldName + ", last(" + fieldName + ", true).over(partitionBy(" + response.groupBy + ").orderBy(" + response.orderBy + "))).as(\"" + fieldName + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, { formula: formula, icon: "functions", name: "Impute missing values " + fieldName });
            });
        };
        ColumnDelegate.prototype.flattenStructColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var formula = self.toFormula("col(\"" + fieldName + ".*\")", column, grid);
            self.controller.addFunction(formula, {
                formula: formula, icon: "functions",
                name: "Flatten " + fieldName
            });
        };
        /**
         * Generates a temporary fieldname
         * @returns {string} the fieldName
         */
        ColumnDelegate.prototype.createTempField = function () {
            return "c_" + (new Date()).getTime();
        };
        /**
         * Creates a formula for cleaning values as future fieldnames
         * @returns {string} a formula for cleaning row values as fieldnames
         */
        ColumnDelegate.prototype.createCleanFieldFormula = function (fieldName, tempField) {
            return "when(startsWith(regexp_replace(substring(" + fieldName + ",0,1),\"[0-9]\",\"***\"),\"***\"),concat(\"c_\",lower(regexp_replace(" + fieldName + ",\"[^a-zA-Z0-9_]+\",\"_\")))).otherwise(lower(regexp_replace(" + fieldName + ",\"[^a-zA-Z0-9_]+\",\"_\"))).as(\"" + tempField + "\")";
        };
        /**
         * Extract numerical values from string
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.extractNumeric = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var script = "regexp_replace(" + fieldName + ", \"[^0-9\\\\.]+\",\"\").as('" + fieldName + "')";
            var formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, {
                formula: formula, icon: "filter_2",
                name: "Extract numeric from " + self.getColumnDisplayName(column)
            });
        };
        /**
         * Negate a boolean
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.negateBoolean = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            var script = "not(" + fieldName + ").as(\"" + fieldName + "\")";
            var formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, {
                formula: formula, icon: "exposure",
                name: "Negate boolean from " + self.getColumnDisplayName(column)
            });
        };
        /**
         * One hot encode categorical values
         *
         * @param {ui.grid.GridColumn} column the column to be hidden
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.oneHotEncodeColumn = function (self, column, grid) {
            var fieldName = self.getColumnFieldName(column);
            // Chain three calls: 1) clean values as valid column names 2) execute pivot 3) replace null with empty (due to spark2 pivot behavior)
            var tempField = self.createTempField();
            var cleanFormula = self.createCleanFieldFormula(fieldName, tempField);
            // Generate group by and pivot formula from all the columns
            var cols = self.toColumnArray(grid.columns);
            var colString = cols.join();
            var formula = "groupBy(" + colString + ").pivot(\"" + tempField + "\").agg(when(count(" + tempField + ")>0,1).otherwise(0))";
            var chainedOp = new ChainedOperation(3);
            self.controller.setChainedQuery(chainedOp);
            self.controller.pushFormula(cleanFormula, { formula: cleanFormula, icon: 'functions', name: 'Clean one hot field ' + fieldName }, true, false)
                .then(function () {
                chainedOp.nextStep();
                self.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'One hot encode ' + fieldName }, true, false)
                    .then(function () {
                    // Now we need to fill in the null values with zero for our new cols
                    var allcols = self.toColumnArray(self.controller.engine.getCols());
                    var select = angular.copy(cols);
                    var idx = cols.length - 1;
                    angular.forEach(allcols, function (col, index) {
                        if (index > idx) {
                            select.push("coalesce(" + col + ",0).as(\"" + col + "\")");
                        }
                    });
                    var selectString = select.join();
                    var fillNAFormula = "select(" + selectString + ")";
                    chainedOp.nextStep();
                    self.controller.addFunction(fillNAFormula, { formula: fillNAFormula, icon: 'functions', name: 'Fill NA' });
                });
            });
        };
        /**
         * Gets the target data types supported for casting this column.
         */
        ColumnDelegate.prototype.getAvailableCasts = function () {
            // not supported
            return [];
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
                textContent: "Enter a new name for the " + this.getColumnDisplayName(column) + " column:",
                placeholder: "Column name",
                ok: "OK",
                cancel: "Cancel"
            });
            this.$mdDialog.show(prompt).then(function (name) {
                // Update field policy
                if (column.index < self.controller.fieldPolicies.length) {
                    var name_1 = self.getColumnFieldName(column);
                    var policy = self.controller.fieldPolicies[column.index];
                    policy.name = name_1;
                    policy.fieldName = name_1;
                    policy.feedFieldName = name_1;
                }
                // Add rename function
                var script = self.getColumnFieldName(column) + ".as(\"" + StringUtils.singleQuote(name) + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, {
                    formula: formula, icon: "mode_edit",
                    name: "Rename " + self.getColumnDisplayName(column) + " to " + name
                });
            });
        };
        /**
         * Sets the domain type for the specified column.
         */
        ColumnDelegate.prototype.setDomainType = function (column, domainTypeId) {
            var fieldName = this.getColumnFieldName(column);
            this.controller.setDomainType(column.index, domainTypeId);
            var formula = "withColumn(\"" + fieldName + "\", " + fieldName + ")";
            this.controller.pushFormula(formula, { formula: formula, icon: 'functions', name: 'Change domain type' });
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
         * Splits the specified column on the specified value.
         *
         * @param value - the value to split on
         * @param column - the column
         * @param grid - the table
         */
        ColumnDelegate.prototype.splitOn = function (value, column, grid) {
            var displayName = this.getColumnDisplayName(column);
            var fieldName = this.getColumnFieldName(column);
            var pattern = "[" + StringUtils.singleQuote(value).replace(/]/g, "\\]") + "]";
            var formula = this.toFormula("split(" + fieldName + ", '" + pattern + "').as(\"" + StringUtils.singleQuote(displayName) + "\")", column, grid);
            this.controller.addFunction(formula, { formula: formula, icon: "call_split", name: "Split " + this.getColumnDisplayName(column) + " on " + value });
        };
        /**
         * Executes the specified operation on the column.
         *
         * @param {Object} transform the transformation object from {@link VisualQueryColumnDelegate#getTransforms}
         * @param {ui.grid.GridColumn} column the column to be transformed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.transformColumn = function (transform, column, grid) {
            var fieldName = this.getColumnFieldName(column);
            var self = this;
            if ($.isFunction(transform.operation)) {
                transform.operation(self, column, grid);
            }
            else {
                var script = transform.operation + "(" + fieldName + ").as(\"" + StringUtils.singleQuote(fieldName) + "\")";
                var formula = this.toFormula(script, column, grid);
                var name_2 = (transform.description ? transform.description : transform.name) + " " + this.getColumnDisplayName(column);
                this.controller.addFunction(formula, { formula: formula, icon: transform.icon, name: name_2 });
            }
        };
        /**
         * Displays a dialog prompt to prompt for value to replace
         *
         * @param {ui.grid.GridColumn} column the column to be renamed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.replaceEmptyWithValue = function (self, column, grid) {
            var prompt = self.$mdDialog.prompt({
                title: "Replace Empty",
                textContent: "Enter replace value:",
                placeholder: "0",
                ok: "OK",
                cancel: "Cancel"
            });
            self.$mdDialog.show(prompt).then(function (value) {
                var fieldName = self.getColumnFieldName(column);
                var script = "when((" + fieldName + " == \"\" || isnull(" + fieldName + ") ),\"" + value + "\").otherwise(" + fieldName + ").as(\"" + fieldName + "\")";
                var formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, {
                    formula: formula, icon: "find_replace",
                    name: "Fill empty with " + value
                });
            });
        };
        /**
         * Round numeric to specified digits
         *
         * @param {ui.grid.GridColumn} column the column to be renamed
         * @param {ui.grid.Grid} grid the grid with the column
         */
        ColumnDelegate.prototype.roundNumeric = function (self, column, grid) {
            var prompt = self.$mdDialog.prompt({
                title: "Round Numeric",
                textContent: "Enter scale decimal:",
                placeholder: "0",
                initialValue: "0",
                ok: "OK",
                cancel: "Cancel"
            });
            self.$mdDialog.show(prompt).then(function (value) {
                if (value != null && !isNaN(value) && (parseInt(value) >= 0)) {
                    var fieldName = self.getColumnFieldName(column);
                    var script = "round(" + fieldName + ", " + value + ").as(\"" + fieldName + "\")";
                    var formula = self.toFormula(script, column, grid);
                    self.controller.addFunction(formula, {
                        formula: formula, icon: "exposure_zero",
                        name: "Round " + fieldName + " to " + value + " digits"
                    });
                    return;
                }
                else {
                    alert("Enter 0 or a positive numeric integer");
                    self.roundNumeric(self, column, grid);
                }
            });
        };
        /**
         * Validates the specified filter.
         *
         * @param {Object} the column to apply the filter to
         * @param {Object} filter the filter to be validated
         * @param {VisualQueryTable} table the visual query table
         */
        ColumnDelegate.prototype.validateFilter = function (header, filter, table) {
            if (filter.term == "") {
                filter.term = null;
            }
            else {
                delete filter.regex;
            }
        };
        ColumnDelegate.prototype.applyFilters = function (header, filters, table) {
            table.onRowsChange();
            table.refreshRows();
        };
        ColumnDelegate.prototype.applyFilter = function (header, filter, table) {
            table.onRowsChange();
            table.refreshRows();
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
                case DataType.ARRAY_DOUBLE:
                    return DataCategory.ARRAY_DOUBLE;
            }
            // Deal with complex types
            if (dataType.startsWith(DataType.ARRAY.toString())) {
                return DataCategory.ARRAY;
            }
            else if (dataType.startsWith(DataType.MAP.toString())) {
                return DataCategory.MAP;
            }
            else if (dataType.startsWith(DataType.STRUCT.toString())) {
                return DataCategory.STRUCT;
            }
            else if (dataType.startsWith(DataType.UNION.toString())) {
                return DataCategory.UNION;
            }
            return DataCategory.OTHER;
        };
        /**
         * Gets the human-readable name of the specified column.
         */
        ColumnDelegate.prototype.getColumnDisplayName = function (column) {
            return column.displayName;
        };
        /**
         * Gets the SQL identifier for the specified column.
         */
        ColumnDelegate.prototype.getColumnFieldName = function (column) {
            return column.field || column.name;
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
            var self = this;
            if (dataCategory === DataCategory.NUMERIC) {
                transforms.push({ description: 'Impute missing with mean', icon: 'functions', name: 'Impute', operation: self.imputeMeanColumn }, { description: 'Replace null/nan with a specified value', icon: 'find_replace', name: 'Replace null/nan...', operation: self.replaceEmptyWithValue }, { description: 'Convert to a numerical array for ML', icon: 'functions', name: 'Vectorize', operation: self.vectorizeColumn }, { description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil' }, { description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor' }, { icon: 'exposure_zero', name: 'Round...', operation: self.roundNumeric }, { descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees' }, { descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians' });
            }
            else if (dataCategory === DataCategory.STRING) {
                transforms.push({ description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower' }, { description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper' }, { description: 'Title case', icon: 'format_color_text', name: 'Title Case', operation: 'initcap' }, { description: 'Extract numeric', icon: 'filter_2', name: 'Extract numeric', operation: self.extractNumeric }, { icon: 'graphic_eq', name: 'Trim', operation: 'trim' }, { description: 'One hot encode (or pivot) categorical values', icon: 'functions', name: 'One hot encode', operation: self.oneHotEncodeColumn }, { description: 'Replace empty with a specified value', icon: 'find_replace', name: 'Replace empty...', operation: self.replaceEmptyWithValue }, { description: 'Impute missing values by fill-forward', icon: 'functions', name: 'Impute missing values...', operation: self.imputeMissingColumn }, { description: 'Index labels', icon: 'functions', name: 'Index labels', operation: self.indexColumn }, { description: 'Crosstab', icon: 'poll', name: 'Crosstab', operation: self.crosstabColumn });
            }
            else if (dataCategory == DataCategory.ARRAY_DOUBLE) {
                transforms.push({ description: 'Rescale using standard deviation', icon: 'functions', name: 'Rescale using std dev', operation: self.rescaleStdDevColumn }, { description: 'Rescale using mean', icon: 'functions', name: 'Rescale using mean', operation: self.rescaleMeanColumn }, { description: 'Rescale using mean', icon: 'functions', name: 'Rescale using mean and std dev', operation: self.rescaleBothMethodsColumn }, { description: 'Rescale min/max between 0-1', icon: 'functions', name: 'Rescale min/max between 0-1', operation: self.rescaleMinMax });
            }
            else if (dataCategory === DataCategory.ARRAY) {
                transforms.push({ icon: 'call_split', name: 'Explode', operation: 'explode' }, { description: 'Sort', icon: 'sort', name: 'Sort Array', operation: 'sort_array' }, { description: 'Extract to columns', icon: 'call_split', name: 'Extract to columns', operation: self.extractArrayItems });
            }
            else if (dataCategory === DataCategory.BINARY) {
                transforms.push({ icon: '#', name: 'CRC32', operation: 'crc32' }, { icon: '#', name: 'MD5', operation: 'md5' }, { icon: '#', name: 'SHA1', operation: 'sha1' }, { icon: '#', name: 'SHA2', operation: 'sha2' });
            }
            else if (dataCategory === DataCategory.DATETIME) {
                transforms.push({ description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'dayofmonth' }, { description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'dayofyear' }, { description: 'Hour of', icon: 'access_time', name: 'Hour', operation: 'hour' }, { description: 'Last day of month for', icon: 'today', name: 'Last Day of Month', operation: 'last_day' }, { description: 'Minute of', icon: 'access_time', name: 'Minute', operation: 'minute' }, { description: 'Month of', icon: 'today', name: 'Month', operation: 'month' }, { description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'quarter' }, { description: 'Second of', icon: 'access_time', name: 'Second', operation: 'second' }, { description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'weekofyear' }, { description: 'Year of', icon: 'today', name: 'Year', operation: 'year' });
            }
            else if (dataCategory == DataCategory.STRUCT) {
                transforms.push({ description: 'Flatten struct', icon: 'functions', name: 'Flatten struct', operation: self.flattenStructColumn });
            }
            else if (dataCategory === DataCategory.MAP) {
                transforms.push({ icon: 'call_split', name: 'Explode', operation: 'explode' });
            }
            else if (dataCategory === DataCategory.BOOLEAN) {
                transforms.push({ icon: 'exposure', name: 'Negate boolean', operation: self.negateBoolean });
            }
            return transforms;
        };
        /**
         * Creates a guaranteed unique field name
         * @param columns column list
         * @returns {string} a unique fieldname
         */
        ColumnDelegate.prototype.toAsUniqueColumnName = function (columns, columnFieldName) {
            var prefix = "new_";
            var idx = 0;
            var columnSet = new Set();
            var uniqueName = null;
            var self = this;
            columnSet.add(columnFieldName);
            angular.forEach(columns, function (item) {
                columnSet.add(self.getColumnFieldName(item));
            });
            while (uniqueName == null) {
                var name_3 = prefix + idx;
                uniqueName = (columnSet.has(name_3) ? null : name_3);
                idx++;
            }
            return ".as(\"" + uniqueName + "\")";
        };
        /**
         * Creates a formula that adds a new column with the specified script. It generates a unique column name.
         *
         * @param {string} script the expression for the column
         * @param {ui.grid.GridColumn} column the column to be replaced
         * @param {ui.grid.Grid} grid the grid with the column
         * @returns {string} a formula that replaces the column
         */
        ColumnDelegate.prototype.toAppendColumnFormula = function (script, column, grid) {
            var columnFieldName = this.getColumnFieldName(column);
            var formula = "";
            var self = this;
            angular.forEach(grid.columns, function (item, idx) {
                if (item.visible) {
                    var itemFieldName = self.getColumnFieldName(item);
                    formula += (formula.length == 0) ? "select(" : ", ";
                    formula += itemFieldName;
                    if (itemFieldName == columnFieldName) {
                        formula += "," + script + self.toAsUniqueColumnName(grid.columns, columnFieldName);
                    }
                }
            });
            formula += ")";
            return formula;
        };
        /**
         * Creates a formula that replaces the specified column with the specified script.
         *
         * @param {string} script the expression for the column
         * @param {ui.grid.GridColumn} column the column to be replaced
         * @param {ui.grid.Grid} grid the grid with the column
         * @returns {string} a formula that replaces the column
         */
        ColumnDelegate.prototype.toFormula = function (script, column, grid) {
            var columnFieldName = this.getColumnFieldName(column);
            var formula = "";
            var self = this;
            angular.forEach(grid.columns, function (item) {
                if (item.visible) {
                    var itemFieldName = self.getColumnFieldName(item);
                    formula += (formula.length == 0) ? "select(" : ", ";
                    formula += (itemFieldName === columnFieldName) ? script : itemFieldName;
                }
            });
            formula += ")";
            return formula;
        };
        return ColumnDelegate;
    }());
    exports.ColumnDelegate = ColumnDelegate;
});
//# sourceMappingURL=column-delegate.js.map
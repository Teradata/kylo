define(["require", "exports", "./column-delegate", "./query-engine-constants"], function (require, exports, column_delegate_1, query_engine_constants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Provides the ability to query and transform data.
     */
    var QueryEngine = /** @class */ (function () {
        /**
         * Construct a {@code QueryEngine}.
         */
        function QueryEngine($mdDialog, DatasourcesService, uiGridConstants) {
            this.$mdDialog = $mdDialog;
            this.DatasourcesService = DatasourcesService;
            this.uiGridConstants = uiGridConstants;
            /**
             * Transformation function definitions.
             */
            this.defs_ = {};
            /**
             * Number of rows to select in the initial query.
             */
            this.limit_ = 1000;
            /**
             * Indicates if limiting should be done before sampling.
             */
            this.limitBeforeSample_ = false;
            /**
             * List of states that can be redone.
             */
            this.redo_ = [];
            /**
             * Fraction of rows to include when sampling.
             */
            this.sample_ = 1.0;
            /**
             * List of states.
             */
            this.states_ = [this.newState()];
        }
        Object.defineProperty(QueryEngine.prototype, "allowMultipleDataSources", {
            /**
             * Indicates if multiple data sources are allowed in the same query.
             */
            get: function () {
                return false;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(QueryEngine.prototype, "useNativeDataType", {
            /**
             * Indicates that the native data type should be used instead of the Hive data type.
             */
            get: function () {
                return true;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Indicates if a previously undone transformation can be redone.
         *
         * @returns {@code true} if the transformation can be restored
         */
        QueryEngine.prototype.canRedo = function () {
            return (this.redo_.length !== 0);
        };
        /**
         * Indicates if the current transformation can be undone.
         *
         * @returns {@code true} if the current transformation can be undone
         */
        QueryEngine.prototype.canUndo = function () {
            return (this.states_.length > 1);
        };
        /**
         * Creates a column delegate of the specified data type.
         */
        QueryEngine.prototype.createColumnDelegate = function (dataType, controller) {
            return new column_delegate_1.ColumnDelegate(dataType, controller, this.$mdDialog, this.uiGridConstants);
        };
        /**
         * Gets the type definitions for the output columns of the current script. These definitions are only available after receiving a {@link #transform} response.
         *
         * @returns the column type definitions
         */
        QueryEngine.prototype.getColumnDefs = function () {
            // Set directives
            var defs = {
                "!name": "columns"
            };
            if (typeof this.defs_[query_engine_constants_1.QueryEngineConstants.DEFINE_DIRECTIVE] !== "undefined") {
                defs[query_engine_constants_1.QueryEngineConstants.DEFINE_DIRECTIVE] = this.defs_[query_engine_constants_1.QueryEngineConstants.DEFINE_DIRECTIVE];
            }
            // Add column names
            var columns = this.getState().columns;
            if (columns !== null) {
                columns.forEach(function (column) {
                    defs[column.field] = query_engine_constants_1.QueryEngineConstants.TERNJS_COLUMN_TYPE;
                });
            }
            return defs;
        };
        /**
         * Gets the Hive column label for the field with the specified name.
         *
         * @param fieldName - the field name
         * @returns the Hive column label if the column exists, or {@code null} otherwise
         */
        QueryEngine.prototype.getColumnLabel = function (fieldName) {
            for (var i = this.states_.length - 1; i >= 0; --i) {
                var columns = this.states_[i].columns;
                if (columns !== null) {
                    for (var _i = 0, columns_1 = columns; _i < columns_1.length; _i++) {
                        var column = columns_1[_i];
                        if (column.field === fieldName) {
                            return column.hiveColumnLabel;
                        }
                    }
                }
            }
            return null;
        };
        /**
         * Gets the columns after applying the current transformation.
         *
         * @returns the columns or {@code null} if the transformation has not been applied
         */
        QueryEngine.prototype.getColumns = function () {
            return this.getState().columns;
        };
        /**
         * Gets the Spark script without sampling for the feed.
         *
         * @returns the Spark script
         */
        QueryEngine.prototype.getFeedScript = function () {
            return this.getScript(null, null, false);
        };
        /**
         * Gets the field policies for the current transformation.
         */
        QueryEngine.prototype.getFieldPolicies = function () {
            return this.getState().fieldPolicies;
        };
        /**
         * Gets the schema fields for the the current transformation.
         *
         * @returns the schema fields or {@code null} if the transformation has not been applied
         */
        QueryEngine.prototype.getFields = function () {
            // Get list of columns
            var columns = this.getColumns();
            if (columns === null) {
                return null;
            }
            // Get field list
            return columns.map(function (col) {
                var dataType;
                //comment out decimal to double.  Decimals are supported ... will remove after testing
                if (col.dataType.startsWith("decimal")) {
                    dataType = "decimal";
                }
                else {
                    dataType = col.dataType;
                }
                var colDef = { name: col.hiveColumnLabel, description: col.comment, dataType: dataType, primaryKey: false, nullable: false, sampleValues: [] };
                if (col.precisionScale) {
                    colDef.precisionScale = col.precisionScale;
                }
                else if (dataType === 'decimal') {
                    //parse out the precisionScale
                    var precisionScale = '20,2';
                    if (col.dataType.indexOf("(") > 0) {
                        precisionScale = col.dataType.substring(col.dataType.indexOf("(") + 1, col.dataType.length - 1);
                    }
                    colDef.precisionScale = precisionScale;
                }
                colDef.derivedDataType = dataType;
                return colDef;
            });
        };
        /**
         * Gets the function definitions being used.
         *
         * @return the function definitions
         */
        QueryEngine.prototype.getFunctionDefs = function () {
            return this.defs_;
        };
        /**
         * Gets the list of contexts for the current transformations.
         *
         * @return the function history
         */
        QueryEngine.prototype.getHistory = function () {
            return this.states_.slice(1).map(function (state) {
                return state.context;
            });
        };
        /**
         * Returns the data sources that are supported natively by this engine.
         */
        QueryEngine.prototype.getNativeDataSources = function () {
            return new Promise(function (resolve) { return resolve([]); });
        };
        /**
         * Gets the column statistics for the current transformation.
         */
        QueryEngine.prototype.getProfile = function () {
            var profile = [];
            var state = this.getState();
            // Add total counts
            var hasInvalidCount = false;
            var hasTotalCount = false;
            var hasValidCount = false;
            if (state.profile) {
                state.profile.forEach(function (row) {
                    if (row.columnName === "(ALL)") {
                        hasInvalidCount = hasInvalidCount || (row.metricType === "INVALID_COUNT");
                        hasTotalCount = hasTotalCount || (row.metricType === "TOTAL_COUNT");
                        hasValidCount = hasValidCount || (row.metricType === "VALID_COUNT");
                    }
                });
            }
            if (!hasInvalidCount) {
                profile.push({ columnName: "(ALL)", metricType: "INVALID_COUNT", metricValue: 0 });
            }
            if (!hasTotalCount) {
                profile.push({ columnName: "(ALL)", metricType: "TOTAL_COUNT", metricValue: (state.rows) ? state.rows.length : 0 });
            }
            if (!hasValidCount) {
                profile.push({ columnName: "(ALL)", metricType: "VALID_COUNT", metricValue: 0 });
            }
            // Add state profile
            if (state.profile) {
                profile = profile.concat(state.profile);
            }
            return profile;
        };
        /**
         * Gets the rows after applying the current transformation.
         *
         * @returns the rows or {@code null} if the transformation has not been applied
         */
        QueryEngine.prototype.getRows = function () {
            return this.getState().rows;
        };
        /**
         * Lists the names of the supported data source types.
         *
         * Used in error messages to list the supported data source types.
         */
        QueryEngine.prototype.getSupportedDataSourceNames = function () {
            return [];
        };
        /**
         * Gets the schema for the specified table.
         *
         * @param schema - name of the database or schema
         * @param table - name of the table
         * @param datasourceId - id of the datasource
         * @returns the table schema
         */
        QueryEngine.prototype.getTableSchema = function (schema, table, datasourceId) {
            var self = this;
            return new Promise(function (resolve, reject) { return self.DatasourcesService.getTableSchema(datasourceId, table, schema).then(resolve, reject); });
        };
        /**
         * Gets the validation results from the current transformation.
         */
        QueryEngine.prototype.getValidationResults = function () {
            return this.getState().validationResults;
        };
        /**
         * The number of rows to select in the initial query.
         *
         * @param value - the new value
         * @returns the number of rows
         */
        QueryEngine.prototype.limit = function (value) {
            if (typeof value !== "undefined") {
                this.clearTableState();
                this.limit_ = value;
            }
            return this.limit_;
        };
        /**
         * Removes the last transformation from the stack. This action cannot be undone.
         *
         * @see #undo()
         */
        QueryEngine.prototype.pop = function () {
            if (this.states_.length > 1) {
                this.states_.pop();
            }
        };
        /**
         * Adds a transformation expression to the stack.
         *
         * @param tree - the abstract syntax tree for the expression
         * @param context - the UI context for the transformation
         */
        QueryEngine.prototype.push = function (tree, context) {
            // Add new state
            var state = this.newState();
            state.context = context;
            state.fieldPolicies = this.getState().fieldPolicies;
            state.script = this.parseAcornTree(tree);
            this.states_.push(state);
            // Clear redo states
            this.redo_ = [];
        };
        /**
         * Restores the last transformation that was undone.
         *
         * @see #undo()
         * @returns the UI context for the transformation
         * @throws {Error} if there are no transformations to redo
         */
        QueryEngine.prototype.redo = function () {
            if (this.redo_.length > 0) {
                var state = this.redo_.pop();
                this.states_.push(state);
                return state.context;
            }
            else {
                throw new Error("No states to redo");
            }
        };
        /**
         * The fraction of rows to include when sampling.
         *
         * @param value - the new value
         * @returns the fraction of rows
         */
        QueryEngine.prototype.sample = function (value) {
            if (typeof value !== "undefined") {
                this.clearTableState();
                this.sample_ = value;
            }
            return this.sample_;
        };
        /**
         * Returns an object for recreating this script.
         *
         * @return the saved state
         */
        QueryEngine.prototype.save = function () {
            return this.states_.slice(1).map(function (state) {
                return { context: state.context, script: state.script };
            });
        };
        /**
         * Searches for table names matching the specified query.
         *
         * @param query - search query
         * @param datasourceId - datasource to search
         * @returns the list of table references
         */
        QueryEngine.prototype.searchTableNames = function (query, datasourceId) {
            var tables = this.DatasourcesService.listTables(datasourceId, query);
            return new Promise(function (resolve, reject) { return tables.then(resolve, reject); });
        };
        /**
         * Sets the field policies to use for the current transformation.
         */
        QueryEngine.prototype.setFieldPolicies = function (policies) {
            this.getState().fieldPolicies = policies;
        };
        /**
         * Sets the function definitions to use.
         *
         * @param defs the function definitions
         */
        QueryEngine.prototype.setFunctionDefs = function (defs) {
            this.defs_ = defs;
        };
        /**
         * Loads the specified state for using an existing transformation.
         */
        QueryEngine.prototype.setState = function (state) {
            var _this = this;
            this.redo_ = [];
            state.forEach(function (src) {
                var state = _this.newState();
                state.context = src.context;
                state.script = src.script;
                _this.states_.push(state);
            });
        };
        /**
         * Sets the query and datasources.
         */
        QueryEngine.prototype.setQuery = function (query, datasources) {
            if (datasources === void 0) { datasources = []; }
            this.datasources_ = (datasources.length > 0) ? datasources : null;
            this.redo_ = [];
            this.source_ = this.parseQuery(query);
            this.states_ = [this.newState()];
        };
        /**
         * Indicates if the limiting should be done before sampling.
         *
         * @param value - the new value
         * @returns {@code true} if limiting should be done first, or {@code false} if sampling should be done first
         */
        QueryEngine.prototype.shouldLimitBeforeSample = function (value) {
            if (typeof value !== "undefined") {
                this.clearTableState();
                this.limitBeforeSample_ = value;
            }
            return this.limitBeforeSample_;
        };
        /**
         * Removes transformations from the current script.
         */
        QueryEngine.prototype.splice = function (start, deleteCount) {
            // Delete states
            this.states_.splice(start, deleteCount);
            this.clearTableState(start);
            // Clear redo states
            this.redo_ = [];
        };
        /**
         * Indicates if this engine supports the specified data source.
         *
         * @param dataSource - the data source to check
         * @returns true if the data source is support, or false otherwise
         */
        QueryEngine.prototype.supportsDataSource = function (dataSource) {
            return true;
        };
        /**
         * Reverts to the previous transformation. The current transformation is remembered and may be restored.
         *
         * @see #pop()
         * @see #redo()
         * @returns the UI context for the transformation
         * @throws {Error} if there are no transformations to undo
         */
        QueryEngine.prototype.undo = function () {
            if (this.states_.length > 1) {
                var state = this.states_.pop();
                this.redo_.push(state);
                return state.context;
            }
            else {
                throw new Error("No states to undo");
            }
        };
        /**
         * Clears table data from all states. This doesn't affect column information that doesn't change with the limit or sample
         * properties.
         */
        QueryEngine.prototype.clearTableState = function (index) {
            if (index === void 0) { index = 0; }
            for (var r = index; r < this.redo_.length; ++r) {
                this.redo_[r].rows = null;
                this.redo_[r].table = null;
            }
            for (var s = index; s < this.states_.length; ++s) {
                this.states_[s].rows = null;
                this.states_[s].table = null;
            }
        };
        /**
         * Gets the current state.
         */
        QueryEngine.prototype.getState = function () {
            return this.states_.length > 0 ? this.states_[this.states_.length - 1] : {};
        };
        /**
         * Creates a new script state.
         *
         * @returns a new script state
         */
        QueryEngine.prototype.newState = function () {
            return { columns: null, context: {}, fieldPolicies: null, profile: null, rows: null, script: null, table: null, validationResults: null };
        };
        return QueryEngine;
    }());
    exports.QueryEngine = QueryEngine;
});
//# sourceMappingURL=query-engine.js.map
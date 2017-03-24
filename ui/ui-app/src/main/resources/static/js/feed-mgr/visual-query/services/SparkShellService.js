/**
 * The result of a Spark transformation.
 *
 * @typedef {Object} TransformResponse
 * @property {string} message the error message if status is "error"
 * @property {QueryResult} results the results if status is "success"
 * @property {string} status "success" if the script executed successfully or "error" if an exception occurred
 * @property {string} table the Hive table containing the results if status is "success"
 */

/**
 * The result of a SQL query or Spark DataFrame.
 *
 * @typedef {Object} QueryResult
 * @property {Object.<String, QueryResultColumn>} columnDisplayNameMap maps column display names to column details
 * @property {Object.<String, QueryResultColumn>} columnFieldMap maps field names to column details
 * @property {Array.<QueryResultColumn>} columns list of column details
 * @property {string} query the Spark script that was sent in the request
 * @property {Array.<Object.<String, *>>} rows maps field names to values
 */

/**
 * A column in a QueryResult.
 *
 * @typedef {Object} QueryResultColumn
 * @property {string} databaseName name of the database containing the table
 * @property {string} dataType name of the data type for the column
 * @property {string} displayName a human-readable name for the column
 * @property {string} field name of the column in the table
 * @property {string} hiveColumnLabel suggested title for the column, usually specified by the AS clause
 * @property {number} index position of the column in the table
 * @property {string} tableName name of the source table
 */

/**
 * A field in a database table.
 *
 * @typedef {Object} SchemaField
 * @property {string} dataType name of the data type
 * @property {string} description a description for the field contents
 * @property {string} name field name
 * @property {boolean} nullable {@code true} if the field can contain {@code null} values
 * @property {boolean} primaryKey {@code true} if the field is a part of the primary key
 * @property {string[]} sampleValues sample values of the field
 */

/**
 * Maintains the state of a Spark script for a single transformation.
 *
 * @typedef {Object} ScriptState
 * @property {Array.<QueryResultColumn>|null} columns the columns as returned by the server
 * @property {Object} context the UI context for this script state
 * @property {Array.<Object.<string,*>>|null} rows the rows as returned by the server
 * @property {string} script the Spark script
 * @property {string|null} table the table containing the results
 */
define(["angular", "feed-mgr/visual-query/module-name"], function (angular, moduleName) {
angular.module(moduleName).factory("SparkShellService", ["$http", "$mdDialog", "$q", "$timeout", "RestUrlService", "SparkDatasourceService", "SparkParserService", "VisualQueryService",
                                                         function($http, $mdDialog, $q, $timeout, RestUrlService, SparkDatasourceService, SparkParserService, VisualQueryService) {
    // URL to the API server
    var API_URL = RestUrlService.SPARK_SHELL_SERVICE_URL;

    /**
     * Constructs a SparkShellService.
     *
     * @constructor
     * @param {string|VisualQueryModel} source the source SQL for transformations
     * @param {Object} [opt_load] the saved state to be loaded
     * @param {Array<string>} [opt_datasources] the list of required data source ids
     */
    var SparkShellService = function(source, opt_load, opt_datasources) {

        /**
         * List of required data source ids.
         *
         * @private
         * @type {Array<string>}
         */
        this.datasources_ = angular.copy(opt_datasources);

        /**
         * Transformation function definitions.
         *
         * @private
         * @type {Object}
         */
        this.defs_ = {};

        /**
         * Number of rows to select in the initial query.
         *
         * @private
         * @type {number}
         */
        this.limit_ = 1000;

        /**
         * Indicates if limiting should be done before sampling.
         *
         * @private
         * @type {boolean}
         */
        this.limitBeforeSample_ = false;

        /**
         * List of states that can be redone.
         *
         * @private
         * @type {Array.<ScriptState>}
         */
        this.redo_ = [];

        /**
         * Fraction of rows to include when sampling.
         *
         * @private
         * @type {number}
         */
        this.sample_ = 1.0;

        /**
         * The source SQL for transformations, escaped for Scala.
         *
         * @private
         * @type {string}
         */
        this.source_ = SparkDatasourceService.toScript(source, this.datasources_);

        /**
         * List of states.
         *
         * @private
         * @type {Array.<ScriptState>}
         */
        this.states_ = [this.newState()];

        if (angular.isArray(opt_load)) {
            var sparkShellService = this;
            angular.forEach(opt_load, function(src) {
                var state = sparkShellService.newState();
                state.context = src.context;
                state.script = src.script;
                sparkShellService.states_.push(state);
            });
        }
    };

    angular.extend(SparkShellService.prototype, {
        /**
         * Indicates if a previously undone transformation can be redone.
         *
         * @returns {boolean} {@code true} if the transformation can be restored
         */
        canRedo: function() {
            return (this.redo_.length !== 0);
        },

        /**
         * Indicates if the current transformation can be undone.
         *
         * @returns {boolean} {@code true} if the current transformation can be undone
         */
        canUndo: function() {
            return (this.states_.length > 1);
        },

        /**
         * Gets the type definitions for the output columns of the current script. These definitions are only available after
         * receiving a {@link SparkShellService#transform} response.
         *
         * @returns {Object} the column type definitions
         */
        getColumnDefs: function() {
            // Set directives
            var defs = {
                "!name": "columns"
            };

            if (typeof(this.defs_["!define"]) !== "undefined") {
                defs["!define"] = this.defs_["!define"];
            }

            // Add column names
            var columns = this.getState().columns;

            if (columns !== null) {
                angular.forEach(columns, function(column) {
                    defs[column.field] = SparkParserService.TERNJS_COLUMN_TYPE;
                });
            }

            return defs;
        },

        /**
         * Gets the Hive column label for the field with the specified name.
         *
         * @param {string} fieldName the field name
         * @returns {string|null} the Hive column label if the column exists, or {@code null} otherwise
         */
        getColumnLabel: function(fieldName) {
            for (var i=this.states_.length-1; i >= 0; --i) {
                var columns = this.states_[i].columns;
                if (columns !== null) {
                    for (var j = 0; j < columns.length; ++j) {
                        if (columns[j].field === fieldName) {
                            return columns[j].hiveColumnLabel;
                        }
                    }
                }
            }
            return null;
        },

        /**
         * Gets the columns after applying the current transformation.
         *
         * @returns {Array.<QueryResultColumn>|null} the columns or {@code null} if the transformation has not been applied
         */
        getColumns: function() {
            return this.getState().columns;
        },

        /**
         * Gets the Spark script without sampling for the feed.
         *
         * @returns {string} the Spark script
         */
        getFeedScript: function() {
            return this.getScript(null, null, false);
        },

        /**
         * Gets the schema fields for the the current transformation.
         *
         * @returns {Array.<SchemaField>|null} the schema fields or {@code null} if the transformation has not been applied
         */
        getFields: function() {
            // Get list of columns
            var columns = this.getColumns();
            if (columns === null) {
                return null;
            }

            // Get field list
            return _.map(columns, function(col) {
                var dataType;
                //comment out decimal to double.  Decimals are supported ... will remove after testing
                if (col.dataType.startsWith("decimal")) {
                    dataType = "decimal";
                }
                else if (col.dataType === "smallint") {
                    dataType = "int";
                } else {
                    dataType = col.dataType;
                }
                var colDef = {
                    name: col.hiveColumnLabel, description: "", dataType: dataType, primaryKey: false, nullable: false,
                    sampleValues: []};
                if (dataType === 'decimal') {
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
        },

        /**
         * Gets the function definitions being used.
         *
         * @return {Object} the function definitions
         */
        getFunctionDefs: function() {
            return this.defs_;
        },

        /**
         * Gets the list of contexts for the current transformations.
         *
         * @return {Object[]} the function history
         */
        getHistory: function() {
            return _.map(this.states_.slice(1), function(state) {
                return state.context;
            });
        },

        /**
         * Gets the rows after applying the current transformation.
         *
         * @returns {Array.<Object.<string,*>>|null} the rows or {@code null} if the transformation has not been applied
         */
        getRows: function() {
            return this.getState().rows;
        },

        /**
         * Gets the Spark script.
         *
         * @param {number|null} [opt_start] the index of the first transformation
         * @param {number|null} [opt_end] the index of the last transformation
         * @param {boolean|null} [opt_sample] {@code false} to disable sampling
         * @returns {string} the Spark script
         */
        getScript: function(opt_start, opt_end, opt_sample) {
            // Parse arguments
            var start = angular.isNumber(opt_start) ? opt_start : 0;
            var end = angular.isNumber(opt_end) ? opt_end + 1 : this.states_.length;
            var sample = (angular.isUndefined(opt_sample) || opt_sample === null || opt_sample);

            // Build script
            var sparkScript = "import org.apache.spark.sql._\n";

            if (start === 0) {
                sparkScript += this.source_;
                sparkScript += SparkParserService.DATA_FRAME_VARIABLE + " = " + SparkParserService.DATA_FRAME_VARIABLE;
                if (sample && this.limitBeforeSample_ && this.limit_ > 0) {
                    sparkScript += ".limit(" + this.limit_ + ")";
                }
                if (sample && this.sample_ > 0 && this.sample_ < 1) {
                    sparkScript += ".sample(false, " + this.sample_ + ")";
                }
                if (sample && !this.limitBeforeSample_ && this.limit_ > 0) {
                    sparkScript += ".limit(" + this.limit_ + ")";
                }
                sparkScript += "\n";
                ++start;
            } else {
                sparkScript += "var " + SparkParserService.DATA_FRAME_VARIABLE + " = parent\n";
            }

            for (var i = start; i < end; ++i) {
                sparkScript += SparkParserService.DATA_FRAME_VARIABLE + " = " + SparkParserService.DATA_FRAME_VARIABLE + this.states_[i].script + "\n";
            }

            sparkScript += SparkParserService.DATA_FRAME_VARIABLE + "\n";
            return sparkScript;
        },

        /**
         * The number of rows to select in the initial query.
         *
         * @param {number} [opt_value] the new value
         * @returns {number} the number of rows
         */
        limit: function(opt_value) {
            if (arguments.length !== 0) {
                this.clearTableState();
                this.limit_ = opt_value;
            }
            return this.limit_;
        },

        /**
         * Removes the last transformation from the stack. This action cannot be undone.
         *
         * @see #undo()
         */
        pop: function() {
            if (this.states_.length > 1) {
                this.states_.pop();
            }
        },

        /**
         * Adds a transformation expression to the stack.
         *
         * @param {acorn.Node} tree the abstract syntax tree for the expression
         * @param {Object} context the UI context for the transformation
         */
        push: function(tree, context) {
            // Add new state
            var state = this.newState();
            state.context = context;
            state.script = SparkParserService.toScript(tree, this);
            this.states_.push(state);

            // Clear redo states
            this.redo_ = [];
        },

        /**
         * Restores the last transformation that was undone.
         *
         * @see #undo()
         * @returns {Object} the UI context for the transformation
         * @throws {Error} if there are no transformations to redo
         */
        redo: function() {
            if (this.redo_.length > 0) {
                var state = this.redo_.pop();
                this.states_.push(state);
                return state.context;
            }
            else {
                throw new Error("No states to redo");
            }
        },

        /**
         * The fraction of rows to include when sampling.
         *
         * @param {number} [opt_value] the new value
         * @returns {number} the fraction of rows
         */
        sample: function(opt_value) {
            if (arguments.length !== 0) {
                this.clearTableState();
                this.sample_ = opt_value;
            }
            return this.sample_;
        },

        /**
         * Returns an object for recreating this script.
         *
         * @return {Object} the saved state
         */
        save: function() {
            return _.map(_.rest(this.states_), function(state) {
                return {context: state.context, script: state.script};
            });
        },

        /**
         * Sets the function definitions to use.
         *
         * @param {Object} defs the function definitions
         */
        setFunctionDefs: function(defs) {
            this.defs_ = defs;
        },

        /**
         * Indicates if the limiting should be done before sampling.
         *
         * @param {boolean} [opt_value] the new value
         * @returns {boolean} {@code true} if limiting should be done first, or {@code false} if sampling should be done first
         */
        shouldLimitBeforeSample: function(opt_value) {
            if (arguments.length !== 0) {
                this.clearTableState();
                this.limitBeforeSample_ = opt_value;
            }
            return this.limitBeforeSample_;
        },

        /**
         * Removes transformations from the current script.
         *
         * @param {number} start
         * @param {number} deleteCount
         */
        splice: function(start, deleteCount) {
            // Delete states
            this.states_.splice(start, deleteCount);
            this.clearTableState(start);

            // Clear redo states
            this.redo_ = [];
        },

        /**
         * Runs the current Spark script on the server.
         *
         * @return {Promise} a promise for the response
         */
        transform: function() {
            // Build the request body
            var body = {};
            var index = this.states_.length - 1;

            if (index > 0) {
                // Find last cached state
                var last = index - 1;
                while (last >= 0 && this.states_[last].table === null) {
                    --last;
                }

                // Add script to body
                body["script"] = this.getScript(last + 1, index);
                if (last >= 0) {
                    body["parent"] = {
                        table: this.states_[last].table,
                        script: this.getScript(0, last)
                    };
                }
            }
            else {
                body["script"] = this.getScript()
            }

            if (this.datasources_ !== null) {
                body["datasources"] = this.datasources_;
            }

            // Create the response handlers
            var self = this;
            var deferred = $q.defer();

            var successCallback = function(response) {
                // Check status
                if (response.data.status === "PENDING") {
                    deferred.notify(response.data.progress);

                    $timeout(function() {
                        $http({
                            method: "GET",
                            url: API_URL + "/transform/" + response.data.table,
                            headers: {"Content-Type": "application/json"},
                            responseType: "json"
                        }).then(successCallback, errorCallback);
                    }, 1000, false);
                    return;
                }
                if (response.data.status !== "SUCCESS") {
                    deferred.reject("Unexpected server status.");
                    return;
                }

                // Verify column names
                var invalid = _.find(response.data.results.columns, function(column) {
                    return (column.hiveColumnLabel.match(/[.`]/) !== null);  // Escaping backticks not supported until Spark 2.0
                });
                var reserved = _.find(response.data.results.columns, function(column) {
                    return (column.hiveColumnLabel === "processing_dttm");
                });

                var state = self.states_[index];
                if (angular.isDefined(invalid)) {
                    state.columns = [];
                    state.rows = [];
                    deferred.reject("Column name '" + invalid.hiveColumnLabel + "' is not supported. Please choose a different name.");
                } else if (angular.isDefined(reserved)) {
                    state.columns = [];
                    state.rows = [];
                    deferred.reject("Column name '" + reserved.hiveColumnLabel + "' is reserved. Please choose a different name.");
                } else {
                    state.columns = response.data.results.columns;
                    state.rows = response.data.results.rows;
                    state.table = response.data.table;
                    deferred.resolve(true);
                }
            };
            var errorCallback = function(response) {
                // Update state
                var state = self.states_[index];
                state.columns = [];
                state.rows = [];

                // Respond with error message
                var message;

                if (angular.isString(response.data.message)) {
                    message = (response.data.message.length <= 1024) ? response.data.message : response.data.message.substr(0, 1021) + "...";
                } else {
                    message = "An unknown error occurred.";
                }

                deferred.reject(message);
            };

            // Send the request
            $http({
                method: "POST",
                url: API_URL + "/transform",
                data: JSON.stringify(body),
                headers: {"Content-Type": "application/json"},
                responseType: "json"
            }).then(successCallback, errorCallback);
            return deferred.promise;
        },

        /**
         * Reverts to the previous transformation. The current transformation is remembered and may be restored.
         *
         * @see #pop()
         * @see #redo()
         * @returns {Object} the UI context for the transformation
         * @throws {Error} if there are no transformations to undo
         */
        undo: function() {
            if (this.states_.length > 1) {
                var state = this.states_.pop();
                this.redo_.push(state);
                return state.context;
            }
            else {
                throw new Error("No states to undo");
            }
        },

        /**
         * Clears table data from all states. This doesn't affect column information that doesn't change with the limit or sample
         * properties.
         */
        clearTableState: function(opt_index) {
            var index = (typeof(opt_index) !== "undefined") ? opt_index : 0;

            for (var r=index; r < this.redo_.length; ++r) {
                this.redo_[r].rows = null;
                this.redo_[r].table = null;
            }
            for (var s=index; s < this.states_.length; ++s) {
                this.states_[s].rows = null;
                this.states_[s].table = null;
            }
        },

        /**
         * Gets the current state.
         *
         * @private
         * @returns {ScriptState} the current state
         */
        getState: function() {
            return this.states_.length > 0 ? this.states_[this.states_.length - 1] : {};
        },

        /**
         * Creates a new script state.
         *
         * @private
         * @returns {ScriptState} a new script state
         */
        newState: function() {
            return {columns: null, context: {}, rows: null, script: "", table: null};
        }
    });

    return SparkShellService;
}]);
});

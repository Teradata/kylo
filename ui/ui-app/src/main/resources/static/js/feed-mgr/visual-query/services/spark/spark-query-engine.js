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
define(["require", "exports", "angular", "rxjs/Observable", "rxjs/Subject", "underscore", "../../../services/VisualQueryService", "../../wrangler/api/rest-model", "../../wrangler/query-engine", "../../wrangler/query-engine-factory.service", "./spark-constants", "./spark-query-parser", "./spark-script-builder", "rxjs/add/observable/empty", "rxjs/add/observable/fromPromise", "rxjs/add/observable/interval", "rxjs/add/operator/catch", "rxjs/add/operator/expand", "rxjs/add/operator/map", "rxjs/add/operator/mergeMap", "rxjs/add/operator/take"], function (require, exports, angular, Observable_1, Subject_1, _, VisualQueryService_1, rest_model_1, query_engine_1, query_engine_factory_service_1, spark_constants_1, spark_query_parser_1, spark_script_builder_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Generates a Scala script to be executed by Kylo Spark Shell.
     */
    var SparkQueryEngine = /** @class */ (function (_super) {
        __extends(SparkQueryEngine, _super);
        /**
         * Constructs a {@code SparkQueryEngine}.
         */
        function SparkQueryEngine($http, $mdDialog, $timeout, DatasourcesService, HiveService, RestUrlService, uiGridConstants, VisualQueryService) {
            var _this = _super.call(this, $mdDialog, DatasourcesService, uiGridConstants) || this;
            _this.$http = $http;
            _this.$timeout = $timeout;
            _this.HiveService = HiveService;
            _this.RestUrlService = RestUrlService;
            _this.VisualQueryService = VisualQueryService;
            // Initialize properties
            _this.apiUrl = RestUrlService.SPARK_SHELL_SERVICE_URL;
            // Ensure Kylo Spark Shell is running
            $http.post(RestUrlService.SPARK_SHELL_SERVICE_URL + "/start", null);
            return _this;
        }
        Object.defineProperty(SparkQueryEngine.prototype, "allowLimitWithSample", {
            /**
             * Indicates if both limit and sample can be applied at the same time.
             */
            get: function () {
                return true;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(SparkQueryEngine.prototype, "allowMultipleDataSources", {
            /**
             * Indicates if multiple data sources are allowed in the same query.
             */
            get: function () {
                return true;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(SparkQueryEngine.prototype, "sampleFormulas", {
            /**
             * Gets the sample formulas.
             */
            get: function () {
                return [
                    { name: "Aggregate", formula: "groupBy(COLUMN).agg(count(COLUMN), sum(COLUMN))" },
                    { name: "Conditional", formula: "when(CONDITION, VALUE).when(CONDITION, VALUE).otherwise(VALUE)" },
                    { name: "Pivot", formula: "groupBy(COLUMN).pivot(&quot;COLUMN&quot;).agg(count(COLUMN))" },
                    { name: "Window", formula: "sum(COLUMN).over(orderBy(COLUMN))" }
                ];
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(SparkQueryEngine.prototype, "sqlDialect", {
            /**
             * Gets the SQL dialect used by this engine.
             */
            get: function () {
                return VisualQueryService_1.SqlDialect.HIVE;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(SparkQueryEngine.prototype, "useNativeDataType", {
            /**
             * Indicates that the Hive data type should be used.
             */
            get: function () {
                return false;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Gets the field name for the specified column.
         */
        SparkQueryEngine.prototype.getColumnName = function (column) {
            return column.displayName;
        };
        /**
         * Gets the schema fields for the the current transformation.
         *
         * @returns the schema fields or {@code null} if the transformation has not been applied
         */
        SparkQueryEngine.prototype.getFields = function () {
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
                else if (col.dataType === "smallint") {
                    dataType = "int";
                }
                else {
                    dataType = col.dataType;
                }
                var colDef = { name: col.hiveColumnLabel, description: col.comment, dataType: dataType, primaryKey: false, nullable: false, sampleValues: [] };
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
        };
        /**
         * Returns the data sources that are supported natively by this engine.
         */
        SparkQueryEngine.prototype.getNativeDataSources = function () {
            return new Promise(function (resolve) { return resolve([{ id: spark_constants_1.SparkConstants.HIVE_DATASOURCE, name: "Hive" }]); });
        };
        /**
         * Gets the Spark script.
         *
         * @param start - the index of the first transformation
         * @param end - the index of the last transformation
         * @param sample - {@code false} to disable sampling
         * @returns the Spark script
         */
        SparkQueryEngine.prototype.getScript = function (start, end, sample) {
            if (start === void 0) { start = null; }
            if (end === void 0) { end = null; }
            if (sample === void 0) { sample = true; }
            // Parse arguments
            start = (start !== null) ? start : 0;
            end = (end !== null) ? end + 1 : this.states_.length;
            // Build script
            var sparkScript = "import org.apache.spark.sql._\n";
            if (start === 0) {
                sparkScript += this.source_;
                sparkScript += spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE;
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
            }
            else {
                sparkScript += "var " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = parent\n";
            }
            for (var i = start; i < end; ++i) {
                sparkScript += spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + this.states_[i].script + "\n";
            }
            sparkScript += spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + "\n";
            return sparkScript;
        };
        /**
         * Gets the schema for the specified table.
         *
         * @param schema - name of the database or schema
         * @param table - name of the table
         * @param datasourceId - id of the datasource
         * @returns the table schema
         */
        SparkQueryEngine.prototype.getTableSchema = function (schema, table, datasourceId) {
            if (datasourceId === spark_constants_1.SparkConstants.HIVE_DATASOURCE) {
                var self_1 = this;
                return new Promise(function (resolve, reject) {
                    self_1.$http.get(self_1.RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table)
                        .then(function (response) {
                        resolve(response.data);
                    }, function (response) {
                        reject(response.data);
                    });
                });
            }
            else {
                return _super.prototype.getTableSchema.call(this, schema, table, datasourceId);
            }
        };
        /**
         * Fetches the Ternjs definitions for this query engine.
         */
        SparkQueryEngine.prototype.getTernjsDefinitions = function () {
            var _this = this;
            return new Promise(function (resolve, reject) {
                _this.$http.get(_this.RestUrlService.UI_BASE_URL + "/spark-functions")
                    .then(function (response) {
                    resolve(response.data);
                }, function (err) {
                    reject(err);
                });
            });
        };
        /**
         * Saves the results to the specified destination.
         *
         * @param request - save target
         * @returns an observable tracking the save status
         */
        SparkQueryEngine.prototype.saveResults = function (request) {
            var _this = this;
            // Build the request body
            var body = {
                async: true,
                datasources: (this.datasources_ !== null) ? this.datasources_.filter(function (datasource) { return datasource.id !== spark_constants_1.SparkConstants.HIVE_DATASOURCE; }) : null,
                script: this.getFeedScript()
            };
            if (request.jdbc && request.jdbc.id === spark_constants_1.SparkConstants.HIVE_DATASOURCE) {
                request.jdbc = null;
            }
            // Send the request
            var transformId;
            return Observable_1.Observable
                .fromPromise(this.$http({
                method: "POST",
                url: this.apiUrl + "/transform",
                data: JSON.stringify(body),
                headers: { "Content-Type": "application/json" },
                responseType: "json"
            }))
                .mergeMap(function (response) {
                transformId = response.data.table;
                return _this.$http({
                    method: "POST",
                    url: _this.apiUrl + "/transform/" + transformId + "/save",
                    data: JSON.stringify(request),
                    headers: { "Content-Type": "application/json" },
                    responseType: "json"
                });
            })
                .expand(function (response) {
                if (response.data.status === rest_model_1.SaveResponseStatus.PENDING) {
                    return Observable_1.Observable.interval(1000)
                        .take(1)
                        .mergeMap(function () { return _this.$http({
                        method: "GET",
                        url: _this.apiUrl + "/transform/" + transformId + "/save/" + response.data.id,
                        responseType: "json"
                    }); });
                }
                else if (response.data.status === rest_model_1.SaveResponseStatus.SUCCESS) {
                    return Observable_1.Observable.empty();
                }
                else {
                    throw response;
                }
            })
                .map(function (response) {
                var save = response.data;
                if (save.location !== null && save.location.startsWith("./")) {
                    save.location = _this.apiUrl + "/transform/" + transformId + "/save/" + save.id + save.location.substr(1);
                }
                return save;
            })
                .catch(function (response) {
                throw response.data;
            });
        };
        /**
         * Searches for table names matching the specified query.
         *
         * @param query - search query
         * @param datasourceId - datasource to search
         * @returns the list of table references
         */
        SparkQueryEngine.prototype.searchTableNames = function (query, datasourceId) {
            if (datasourceId === spark_constants_1.SparkConstants.HIVE_DATASOURCE) {
                var tables_1 = this.HiveService.queryTablesSearch(query);
                if (tables_1.then) {
                    return new Promise(function (resolve, reject) { return tables_1.then(resolve, reject); });
                }
                else {
                    return tables_1;
                }
            }
            else {
                return _super.prototype.searchTableNames.call(this, query, datasourceId);
            }
        };
        /**
         * Runs the current Spark script on the server.
         *
         * @return an observable for the response progress
         */
        SparkQueryEngine.prototype.transform = function () {
            // Build the request body
            var body = {
                "policies": this.getState().fieldPolicies
            };
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
                body["script"] = this.getScript();
            }
            if (this.datasources_ !== null) {
                body["datasources"] = this.datasources_.filter(function (datasource) { return datasource.id !== spark_constants_1.SparkConstants.HIVE_DATASOURCE; });
            }
            // Create the response handlers
            var self = this;
            var deferred = new Subject_1.Subject();
            var successCallback = function (response) {
                var state = self.states_[index];
                // Check status
                if (response.data.status === "PENDING") {
                    if (state.columns === null && response.data.results && response.data.results.columns) {
                        state.columns = response.data.results.columns;
                        state.rows = [];
                        state.table = response.data.table;
                        self.updateFieldPolicies(state);
                    }
                    deferred.next(response.data.progress);
                    self.$timeout(function () {
                        self.$http({
                            method: "GET",
                            url: self.apiUrl + "/transform/" + response.data.table,
                            headers: { "Content-Type": "application/json" },
                            responseType: "json"
                        }).then(successCallback, errorCallback);
                    }, 1000, false);
                    return;
                }
                if (response.data.status !== "SUCCESS") {
                    deferred.error("Unexpected server status.");
                    return;
                }
                // Verify column names
                var invalid = _.find(response.data.results.columns, function (column) {
                    return (column.hiveColumnLabel.match(/[.`]/) !== null); // Escaping backticks not supported until Spark 2.0
                });
                var reserved = _.find(response.data.results.columns, function (column) {
                    return (column.hiveColumnLabel === "processing_dttm");
                });
                if (angular.isDefined(invalid)) {
                    state.columns = [];
                    state.rows = [];
                    deferred.error("Column name '" + invalid.hiveColumnLabel + "' is not supported. Please choose a different name.");
                }
                else if (angular.isDefined(reserved)) {
                    state.columns = [];
                    state.rows = [];
                    deferred.error("Column name '" + reserved.hiveColumnLabel + "' is reserved. Please choose a different name.");
                }
                else {
                    // Update state
                    state.columns = response.data.results.columns;
                    state.profile = response.data.profile;
                    state.rows = response.data.results.rows;
                    state.table = response.data.table;
                    state.validationResults = response.data.results.validationResults;
                    self.updateFieldPolicies(state);
                    // Indicate observable is complete
                    deferred.complete();
                }
            };
            var errorCallback = function (response) {
                // Update state
                var state = self.states_[index];
                state.columns = [];
                state.rows = [];
                // Respond with error message
                var message;
                if (angular.isString(response.data.message)) {
                    message = (response.data.message.length <= 1024) ? response.data.message : response.data.message.substr(0, 1021) + "...";
                }
                else {
                    message = "An unknown error occurred.";
                }
                deferred.error(message);
            };
            // Send the request
            self.$http({
                method: "POST",
                url: this.apiUrl + "/transform",
                data: JSON.stringify(body),
                headers: { "Content-Type": "application/json" },
                responseType: "json"
            }).then(successCallback, errorCallback);
            return deferred;
        };
        /**
         * Parses the specified tree into a script for the current state.
         */
        SparkQueryEngine.prototype.parseAcornTree = function (tree) {
            return new spark_script_builder_1.SparkScriptBuilder(this.defs_, this).toScript(tree);
        };
        /**
         * Parses the specified source into a script for the initial state.
         */
        SparkQueryEngine.prototype.parseQuery = function (source) {
            return new spark_query_parser_1.SparkQueryParser(this.VisualQueryService).toScript(source, this.datasources_);
        };
        /**
         * Updates the field policies of the specified state to match the column order.
         * @param {ScriptState<string>} state
         */
        SparkQueryEngine.prototype.updateFieldPolicies = function (state) {
            if (state.fieldPolicies != null && state.fieldPolicies.length > 0) {
                var policyMap_1 = {};
                state.fieldPolicies.forEach(function (policy) {
                    policyMap_1[policy.name] = policy;
                });
                state.fieldPolicies = state.columns.map(function (column) {
                    if (policyMap_1[column.hiveColumnLabel]) {
                        return policyMap_1[column.hiveColumnLabel];
                    }
                    else {
                        return {
                            name: column.hiveColumnLabel,
                            fieldName: column.hiveColumnLabel,
                            feedFieldName: column.hiveColumnLabel,
                            domainTypeId: null,
                            partition: null,
                            profile: true,
                            standardization: null,
                            validation: null
                        };
                    }
                });
            }
        };
        return SparkQueryEngine;
    }(query_engine_1.QueryEngine));
    exports.SparkQueryEngine = SparkQueryEngine;
    query_engine_factory_service_1.registerQueryEngine("spark", ["$http", "$mdDialog", "$timeout", "DatasourcesService", "HiveService", "RestUrlService", "uiGridConstants", "VisualQueryService", SparkQueryEngine]);
});
//# sourceMappingURL=spark-query-engine.js.map
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
define(["require", "exports", "../query-engine", "rxjs/Subject", "./teradata-script-builder", "./teradata-query-parser", "../../../services/VisualQueryService", "./teradata-column-delegate", "../query-engine-constants"], function (require, exports, query_engine_1, Subject_1, teradata_script_builder_1, teradata_query_parser_1, VisualQueryService_1, teradata_column_delegate_1, query_engine_constants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Generates a SQL query to be executed by a Teradata database.
     */
    var TeradataQueryEngine = (function (_super) {
        __extends(TeradataQueryEngine, _super);
        /**
         * Constructs a {@code TeradataQueryEngine}.
         */
        function TeradataQueryEngine($http, $mdDialog, DatasourcesService, RestUrlService, uiGridConstants, VisualQueryService) {
            var _this = _super.call(this, $mdDialog, DatasourcesService, uiGridConstants) || this;
            _this.$http = $http;
            _this.VisualQueryService = VisualQueryService;
            // Initialize properties
            _this.functionsUrl = RestUrlService.UI_BASE_URL + "/teradata-functions";
            _this.transformUrl = RestUrlService.CONTROLLER_SERVICES_BASE_URL;
            return _this;
        }
        Object.defineProperty(TeradataQueryEngine.prototype, "allowLimitWithSample", {
            /**
             * Indicates if both limit and sample can be applied at the same time.
             */
            get: function () {
                return false;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(TeradataQueryEngine.prototype, "sampleFormulas", {
            /**
             * Gets the sample formulas.
             */
            get: function () {
                return [
                    { name: "Group By", formula: "select(COLUMNS).groupBy(COLUMNS)" }
                ];
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(TeradataQueryEngine.prototype, "sqlDialect", {
            /**
             * Gets the SQL dialect used by this engine.
             */
            get: function () {
                return VisualQueryService_1.SqlDialect.TERADATA;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Creates a column delegate of the specified data type.
         */
        TeradataQueryEngine.prototype.createColumnDelegate = function (dataType, controller) {
            return new teradata_column_delegate_1.TeradataColumnDelegate(dataType, controller, this.$mdDialog, this.uiGridConstants);
        };
        /**
         * Gets the type definitions for the output columns of the current script. These definitions are only available after receiving a {@link #transform} response.
         *
         * @returns the column type definitions
         */
        TeradataQueryEngine.prototype.getColumnDefs = function () {
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
                    defs[column.displayName] = query_engine_constants_1.QueryEngineConstants.TERNJS_COLUMN_TYPE;
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
        TeradataQueryEngine.prototype.getColumnLabel = function (fieldName) {
            for (var i = this.states_.length - 1; i >= 0; --i) {
                var columns = this.states_[i].columns;
                if (columns !== null) {
                    for (var _i = 0, columns_1 = columns; _i < columns_1.length; _i++) {
                        var column = columns_1[_i];
                        if (column.displayName === fieldName) {
                            return column.field;
                        }
                    }
                }
            }
            return null;
        };
        /**
         * Gets the field name of the specified column.
         */
        TeradataQueryEngine.prototype.getColumnName = function (column) {
            return column.field;
        };
        /**
         * Gets the Teradata SQL script.
         *
         * @param start - index of the first transformation
         * @param end - index of the last transformation
         * @param sample - false to disable sampling
         * @returns the Spark script
         */
        TeradataQueryEngine.prototype.getScript = function (start, end, sample) {
            if (start === void 0) { start = null; }
            if (end === void 0) { end = null; }
            if (sample === void 0) { sample = true; }
            // Parse arguments
            end = (end !== null) ? end + 1 : this.states_.length;
            // Build script
            var sql = this.source_;
            if (sample) {
                if (this.limit_ > 0) {
                    sql = "SELECT TOP " + this.limit_ + " " + sql.substr(7);
                }
                if (this.sample_ > 0 && this.sample_ < 1) {
                    sql += " SAMPLE " + this.sample_;
                }
            }
            for (var i = 1; i < end; ++i) {
                var script = this.states_[i].script;
                var table = "query" + i;
                sql = "SELECT " + (script.keywordList !== null ? script.keywordList + " " : "") + (script.selectList !== null ? script.selectList.replace(/^\*/, table + ".*") + " " : table + ".*")
                    + "FROM (" + sql + ") " + table
                    + (script.where !== null ? " WHERE " + script.where : "")
                    + (script.groupBy !== null ? " GROUP BY " + script.groupBy : "")
                    + (script.having !== null ? " HAVING " + script.having : "");
            }
            return sql;
        };
        /**
         * Lists the name of the supported data source type.
         */
        TeradataQueryEngine.prototype.getSupportedDataSourceNames = function () {
            return ["Teradata JDBC"];
        };
        /**
         * Fetches the Ternjs definitions for this query engine.
         */
        TeradataQueryEngine.prototype.getTernjsDefinitions = function () {
            var _this = this;
            return new Promise(function (resolve, reject) {
                _this.$http.get(_this.functionsUrl)
                    .then(function (response) {
                    resolve(response.data);
                }, function (err) {
                    reject(err);
                });
            });
        };
        /**
         * The number of rows to select in the initial query.
         *
         * @param value - the new value
         * @returns the number of rows
         */
        TeradataQueryEngine.prototype.limit = function (value) {
            if (typeof value !== "undefined") {
                this.sample_ = 1;
            }
            return _super.prototype.limit.call(this, value);
        };
        /**
         * The fraction of rows to include when sampling.
         *
         * @param value - the new value
         * @returns the fraction of rows
         */
        TeradataQueryEngine.prototype.sample = function (value) {
            if (typeof value !== "undefined") {
                this.limit_ = 0;
            }
            return _super.prototype.sample.call(this, value);
        };
        /**
         * Indicates if this engine supports the specified data source.
         */
        TeradataQueryEngine.prototype.supportsDataSource = function (dataSource) {
            return (typeof dataSource.type === "string" && dataSource.type.toLowerCase() === "teradata");
        };
        /**
         * Runs the current script on the server.
         *
         * @return an observable for the response progress
         */
        TeradataQueryEngine.prototype.transform = function () {
            // Build the request body
            var index = this.states_.length - 1;
            // Create the response handlers
            var self = this;
            var deferred = new Subject_1.Subject();
            var successCallback = function (response) {
                // Verify column names
                var invalid = _.find(response.data.columns, function (column) {
                    return (column.hiveColumnLabel.match(/[.`]/) !== null); // Escaping backticks not supported until Spark 2.0
                });
                var reserved = _.find(response.data.columns, function (column) {
                    return (column.hiveColumnLabel === "processing_dttm");
                });
                var state = self.states_[index];
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
                    state.columns = response.data.columns.map(function (column) {
                        if (!column.displayName.match(/^[A-Za-z0-9]+$/)) {
                            var index_1 = 0;
                            var newName = "";
                            while (newName.length === 0 || response.data.columnDisplayNameMap[newName]) {
                                newName = "col" + (index_1++);
                            }
                            response.data.columnDisplayNameMap[newName] = response.data.columnDisplayNameMap[column.displayName];
                            delete response.data.columnDisplayNameMap[column.displayName];
                            return {
                                comment: column.comment,
                                databaseName: column.databaseName,
                                dataType: column.dataType,
                                displayName: newName,
                                field: column.displayName,
                                hiveColumnLabel: column.displayName,
                                index: column.index,
                                tableName: column.tableName
                            };
                        }
                        else {
                            return {
                                comment: column.comment,
                                databaseName: column.databaseName,
                                dataType: column.dataType,
                                displayName: column.displayName,
                                field: column.displayName,
                                hiveColumnLabel: column.displayName,
                                index: column.index,
                                tableName: column.tableName
                            };
                        }
                    });
                    state.profile = self.generateProfile(response.data.columns, response.data.rows);
                    state.rows = response.data.rows;
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
                data: this.getScript(),
                method: "POST",
                url: this.transformUrl + "/" + this.datasources_[0].controllerServiceId + "/query",
                responseType: "json"
            }).then(successCallback, errorCallback);
            return deferred;
        };
        /**
         * Parses the specified tree into a script for the current state.
         */
        TeradataQueryEngine.prototype.parseAcornTree = function (tree) {
            return new teradata_script_builder_1.TeradataScriptBuilder(this.defs_, this).toScript(tree);
        };
        /**
         * Parses the specified source into a script for the initial state.
         */
        TeradataQueryEngine.prototype.parseQuery = function (source) {
            return new teradata_query_parser_1.TeradataQueryParser(this.VisualQueryService).toScript(source, this.datasources_);
        };
        /**
         * Generates the profile statistics for the specified data.
         */
        TeradataQueryEngine.prototype.generateProfile = function (columns, rows) {
            var profile = [];
            var _loop_1 = function (i) {
                var field = columns[i].field;
                var nulls = 0;
                var values = new Set();
                rows.map(function (row) { return row[field]; }).forEach(function (value) {
                    if (value === null) {
                        ++nulls;
                    }
                    values.add(value);
                });
                profile.push({ columnName: field, metricType: "NULL_COUNT", metricValue: nulls });
                profile.push({ columnName: field, metricType: "TOTAL_COUNT", metricValue: rows.length });
                profile.push({ columnName: field, metricType: "UNIQUE_COUNT", metricValue: values.size });
                profile.push({ columnName: field, metricType: "PERC_NULL_VALUES", metricValue: nulls / rows.length * 100 });
                profile.push({ columnName: field, metricType: "PERC_UNIQUE_VALUES", metricValue: values.size / rows.length * 100 });
                profile.push({ columnName: field, metricType: "PERC_DUPLICATE_VALUES", metricValue: 100 - (values.size / rows.length * 100) });
            };
            for (var i = 0; i < columns.length; ++i) {
                _loop_1(i);
            }
            return profile;
        };
        return TeradataQueryEngine;
    }(query_engine_1.QueryEngine));
    exports.TeradataQueryEngine = TeradataQueryEngine;
});
//# sourceMappingURL=teradata-query-engine.js.map
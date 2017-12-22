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
define(["require", "exports", "underscore", "../../wrangler/query-parser", "./spark-constants"], function (require, exports, _, query_parser_1, spark_constants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /** Name of the DatasourceProvider variable */
    var DATASOURCE_PROVIDER = "datasourceProvider";
    /**
     * Handles transformations from a visual query model to Spark.
     */
    var SparkQueryParser = /** @class */ (function (_super) {
        __extends(SparkQueryParser, _super);
        /**
         * Constructs a {@code SparkQueryParser}.
         */
        function SparkQueryParser(VisualQueryService) {
            return _super.call(this, VisualQueryService) || this;
        }
        /**
         * Generates a Spark script for the specified SQL query and optional data source.
         *
         * @param  sql - the SQL query
         * @param datasources - the data source
         * @returns the Spark script
         * @throws {Error} if there are too many data sources
         */
        SparkQueryParser.prototype.fromSql = function (sql, datasources) {
            if (datasources != null && datasources.length !== 1) {
                throw new Error("Not valid datasources: " + datasources);
            }
            else if (datasources == null || datasources.length === 0 || datasources[0].id === spark_constants_1.SparkConstants.HIVE_DATASOURCE) {
                return "var " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
            }
            else {
                var subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
                return "var " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                    + datasources[0].id + "\", sqlContext)\n";
            }
        };
        /**
         * Generates a Spark script for the specified visual query model and data sources.
         *
         * @param visualQueryModel - the visual query model
         */
        SparkQueryParser.prototype.fromVisualQueryModel = function (visualQueryModel) {
            var self = this;
            var tree = this.VisualQueryService.sqlBuilder(visualQueryModel).buildTree();
            // Group targets by table
            var targetsByTableAlias = {};
            tree.targetList.forEach(function (target) {
                if (typeof targetsByTableAlias[target.val.fields[0]] === "undefined") {
                    targetsByTableAlias[target.val.fields[0]] = [];
                }
                targetsByTableAlias[target.val.fields[0]].push(target);
            });
            // Build join script
            var joinScript = "";
            var tablesByAlias = {};
            for (var _i = 0, _a = tree.fromClause; _i < _a.length; _i++) {
                var expr = _a[_i];
                if (joinScript.length === 0) {
                    joinScript += "var " + spark_constants_1.SparkConstants.DATA_FRAME_VARIABLE + " = " + self.getJoinScript(expr, tablesByAlias, true);
                }
                else {
                    joinScript += self.getJoinScript(expr, tablesByAlias, false);
                }
            }
            // Build table script
            var script = "";
            _.keys(tablesByAlias).sort().forEach(function (alias) {
                var table = tablesByAlias[alias];
                script += "val " + alias + " = ";
                if (typeof table.datasourceId === "string" && table.datasourceId !== spark_constants_1.SparkConstants.HIVE_DATASOURCE) {
                    script += DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\", \"" + table.datasourceId
                        + "\", sqlContext)";
                }
                else {
                    script += "sqlContext.table(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\")";
                }
                script += ".alias(\"" + alias + "\")\n";
            });
            var firstTarget = true;
            script += joinScript + ".select(";
            tree.targetList.forEach(function (target) {
                if (firstTarget) {
                    firstTarget = false;
                }
                else {
                    script += ", ";
                }
                script += target.val.fields[0] + ".col(\"" + StringUtils.escapeScala(target.val.fields[1]) + "\")";
                if (target.name !== null || target.description !== null) {
                    script += ".as(\"" + StringUtils.escapeScala((target.name !== null) ? target.name : target.val.fields[1]) + "\"";
                    if (target.description !== null) {
                        script += ", new org.apache.spark.sql.types.MetadataBuilder().putString(\"comment\", \"" + StringUtils.escapeScala(target.description) + "\").build()";
                    }
                    script += ")";
                }
            });
            script += ")\n";
            return script;
        };
        /**
         * Generates a Spark script for the specified join expression.
         *
         * @private
         * @param expr - the join expression
         * @param tablesByAlias - map of table alias to range var
         * @param first - true if this is the first table in the script, or false otherwise
         * @returns the Spark script
         * @throws {Error} if the join expression is not valid
         */
        SparkQueryParser.prototype.getJoinScript = function (expr, tablesByAlias, first) {
            if (expr.type === this.VisualQueryService.NodeTag.RangeVar) {
                var rangeVar = expr;
                tablesByAlias[rangeVar.aliasName] = rangeVar;
                if (first) {
                    return rangeVar.aliasName;
                }
                else {
                    return ".join(" + rangeVar.aliasName + ")";
                }
            }
            else if (expr.type === this.VisualQueryService.NodeTag.JoinExpr) {
                var joinExpr = expr;
                tablesByAlias[joinExpr.rarg.aliasName] = joinExpr.rarg;
                var script = this.getJoinScript(joinExpr.larg, tablesByAlias, first);
                script += ".join(" + joinExpr.rarg.aliasName;
                if (joinExpr.jointype !== this.VisualQueryService.JoinType.JOIN) {
                    script += ", ";
                    if (joinExpr.quals !== null) {
                        script += this.getQualifierScript(joinExpr.quals);
                    }
                    else {
                        script += "functions.lit(1)";
                    }
                    script += ", ";
                    if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_INNER) {
                        script += "\"inner\"";
                    }
                    else if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_LEFT) {
                        script += "\"left_outer\"";
                    }
                    else if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_RIGHT) {
                        script += "\"right_outer\"";
                    }
                    else {
                        throw new Error("Not a supported join type: " + joinExpr.jointype);
                    }
                }
                script += ")";
                return script;
            }
            else {
                throw new Error("Unsupported type: " + expr.type);
            }
        };
        /**
         * Generates a Spark script for the specified qualifier expression.
         *
         * @param qualifier - the qualifier expression
         * @returns the Spark script
         * @throws {Error} if the qualifier expression is not valid
         */
        SparkQueryParser.prototype.getQualifierScript = function (qualifier) {
            if (qualifier.type === this.VisualQueryService.NodeTag.A_Expr) {
                var aExpr = qualifier;
                return aExpr.lexpr.fields[0] + ".col(\"" + StringUtils.escapeScala(aExpr.lexpr.fields[1]) + "\").equalTo(" + aExpr.rexpr.fields[0] + ".col(\""
                    + StringUtils.escapeScala(aExpr.rexpr.fields[1]) + "\"))";
            }
            else if (qualifier.type === this.VisualQueryService.NodeTag.BoolExpr) {
                var boolExpr = qualifier;
                return this.getQualifierScript(boolExpr.args[0]) + ".and(" + this.getQualifierScript(boolExpr.args[1]) + ")";
            }
            else {
                throw new Error("Unsupported type: " + qualifier.type);
            }
        };
        return SparkQueryParser;
    }(query_parser_1.QueryParser));
    exports.SparkQueryParser = SparkQueryParser;
});
//# sourceMappingURL=spark-query-parser.js.map
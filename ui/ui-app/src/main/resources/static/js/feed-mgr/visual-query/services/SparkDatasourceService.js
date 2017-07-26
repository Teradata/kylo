define(["angular", "feed-mgr/visual-query/module-name", "feed-mgr/visual-query/module"], function (angular, moduleName) {
    angular.module(moduleName).factory("SparkDatasourceService", ["SparkParserService", "VisualQueryService", function (SparkParserService, VisualQueryService) {

        /** Name of the DatasourceProvider variable */
        var DATASOURCE_PROVIDER = "datasourceProvider";

        /**
         * Handles transformations from a visual query model to Spark.
         *
         * @type {Object}
         */
        var SparkDatasourceService = {
            /**
             * Generates a Spark script for the specified SQL query and optional data source.
             *
             * @private
             * @param {string} sql the SQL query
             * @param {Array.<Datasource>} datasources the data source
             * @returns {string} the Spark script
             * @throws {Error} if there are too many data sources
             */
            fromSql: function (sql, datasources) {
                if (datasources != null && datasources.length !== 1) {
                    throw new Error("Not valid datasources: " + datasources);
                } else if (datasources == null || datasources.length === 0 || datasources[0].id === VisualQueryService.HIVE_DATASOURCE) {
                    return "var " + SparkParserService.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
                } else {
                    var subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
                    return "var " + SparkParserService.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                           + datasources[0].id + "\", sqlContext)\n";
                }
            },

            /**
             * Generates a Spark script for the specified visual query model and data sources.
             *
             * @private
             * @param {VisualQueryModel} visualQueryModel the visual query model
             */
            fromVisualQueryModel: function (visualQueryModel) {
                var self = this;
                var tree = VisualQueryService.sqlBuilder(visualQueryModel).buildTree();

                // Group targets by table
                var targetsByTableAlias = {};

                angular.forEach(tree.targetList, function (target) {
                    if (angular.isUndefined(targetsByTableAlias[target.val.fields[0]])) {
                        targetsByTableAlias[target.val.fields[0]] = [];
                    }
                    targetsByTableAlias[target.val.fields[0]].push(target);
                });

                // Build join script
                var joinScript = "";
                var tablesByAlias = {};

                angular.forEach(tree.fromClause, function (expr) {
                    if (joinScript.length === 0) {
                        joinScript += "var " + SparkParserService.DATA_FRAME_VARIABLE + " = " + self.getJoinScript(expr, tablesByAlias, true);
                    } else {
                        joinScript += self.getJoinScript(expr, tablesByAlias, false);
                    }
                });

                // Build table script
                var script = "";

                _.keys(tablesByAlias).sort().forEach(function (alias) {
                    var table = tablesByAlias[alias];

                    script += "val " + alias + " = ";
                    if (angular.isString(table.datasourceId) && table.datasourceId !== VisualQueryService.HIVE_DATASOURCE) {
                        script += DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\", \"" + table.datasourceId
                                  + "\", sqlContext)";
                    } else {
                        script += "sqlContext.table(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\")"
                    }
                    script += ".alias(\"" + alias + "\")\n";
                });

                var firstTarget = true;
                script += joinScript + ".select(";
                angular.forEach(tree.targetList, function (target) {
                    if (firstTarget) {
                        firstTarget = false;
                    } else {
                        script += ", ";
                    }
                    script += target.val.fields[0] + ".col(\"" + StringUtils.escapeScala(target.val.fields[1]) + "\")";
                    if (target.name !== null || target.description !== null) {
                        script += ".as(\"" + StringUtils.escapeScala((target.name !== null) ? target.name : target.val.fields[1]) + "\"";
                        if (target.description !== null) {
                            script += ", new org.apache.spark.sql.types.MetadataBuilder().putString(\"comment\", \"" + StringUtils.escapeScala(target.description) + "\").build()";
                        }
                        script += ")"
                    }
                });
                script += ")\n";

                return script;
            },

            /**
             * Generates a Spark script for the specified join expression.
             *
             * @private
             * @param {(JoinExpr|RangeVar)} expr the join expression
             * @param {Object.<string, RangeVar>} tablesByAlias map of table alias to range var
             * @param {boolean} first true if this is the first table in the script, or false otherwise
             * @returns {string} the Spark script
             * @throws {Error} if the join expression is not valid
             */
            getJoinScript: function (expr, tablesByAlias, first) {
                if (expr.type === VisualQueryService.NodeTag.RangeVar) {
                    tablesByAlias[expr.aliasName] = expr;
                    if (first) {
                        return expr.aliasName;
                    } else {
                        return ".join(" + expr.aliasName + ")";
                    }
                } else if (expr.type === VisualQueryService.NodeTag.JoinExpr) {
                    tablesByAlias[expr.rarg.aliasName] = expr.rarg;

                    var script = this.getJoinScript(expr.larg, tablesByAlias, first);
                    script += ".join(" + expr.rarg.aliasName;

                    if (expr.jointype !== VisualQueryService.JoinType.JOIN) {
                        script += ", ";
                        if (expr.quals !== null) {
                            script += this.getQualifierScript(expr.quals);
                        } else {
                            script += "functions.lit(1)";
                        }

                        script += ", ";
                        if (expr.jointype === VisualQueryService.JoinType.JOIN_INNER) {
                            script += "\"inner\"";
                        } else if (expr.jointype === VisualQueryService.JoinType.JOIN_LEFT) {
                            script += "\"left_outer\"";
                        } else if (expr.jointype === VisualQueryService.JoinType.JOIN_RIGHT) {
                            script += "\"right_outer\"";
                        } else {
                            throw new Error("Not a supported join type: " + expr.jointype);
                        }
                    }

                    script += ")";
                    return script;
                } else {
                    throw new Error("Unsupported type: " + expr.type);
                }
            },

            /**
             * Generates a Spark script for the specified qualifier expression.
             *
             * @param {(A_Expr|BoolExpr)} qualifier the qualifier expression
             * @returns {string} the Spark script
             * @throws {Error} if the qualifier expression is not valid
             */
            getQualifierScript: function (qualifier) {
                if (qualifier.type === VisualQueryService.NodeTag.A_Expr) {
                    return qualifier.lexpr.fields[0] + ".col(\"" + StringUtils.escapeScala(qualifier.lexpr.fields[1]) + "\").equalTo(" + qualifier.rexpr.fields[0] + ".col(\""
                           + StringUtils.escapeScala(qualifier.rexpr.fields[1]) + "\"))";
                } else if (qualifier.type === VisualQueryService.NodeTag.BoolExpr) {
                    return this.getQualifierScript(qualifier.args[0]) + ".and(" + this.getQualifierScript(qualifier.args[1]) + ")";
                } else {
                    throw new Error("Unsupported type: " + qualifier.type);
                }
            },

            /**
             * Generates a Spark script for the specified visual query model and data sources.
             *
             * @param {string|VisualQueryModel} source the SQL query or visual query model
             * @param {Array.<Datasource>} datasources the list of datasources used
             * @returns {string} the Spark script
             * @throws {Error} if there are too many data sources for the source
             */
            toScript: function (source, datasources) {
                if (angular.isString(source)) {
                    return SparkDatasourceService.fromSql(source, datasources);
                } else if (angular.isObject(source)) {
                    return SparkDatasourceService.fromVisualQueryModel(source);
                }
            }
        };

        return {
            toScript: SparkDatasourceService.toScript
        }
    }]);
});

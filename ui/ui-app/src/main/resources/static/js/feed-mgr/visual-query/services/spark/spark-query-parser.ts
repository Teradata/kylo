import * as _ from "underscore";

import {UserDatasource} from "../../../model/user-datasource";
import {A_Expr, BoolExpr, JoinExpr, RangeVar, ResTarget, VisualQueryModel} from "../../../services/VisualQueryService";
import {QueryParser} from "../../wrangler/query-parser";
import {SparkConstants} from "./spark-constants";

/** Name of the DatasourceProvider variable */
const DATASOURCE_PROVIDER = "datasourceProvider";

/**
 * Handles transformations from a visual query model to Spark.
 */
export class SparkQueryParser extends QueryParser {

    /**
     * Constructs a {@code SparkQueryParser}.
     */
    constructor(VisualQueryService: any) {
        super(VisualQueryService);
    }

    /**
     * Generates a Spark script for the specified SQL query and optional data source.
     *
     * @param  sql - the SQL query
     * @param datasources - the data source
     * @returns the Spark script
     * @throws {Error} if there are too many data sources
     */
    protected fromSql(sql: string, datasources: UserDatasource[]): string {
        if (datasources != null && datasources.length !== 1) {
            throw new Error("Not valid datasources: " + datasources);
        } else if (datasources == null || datasources.length === 0 || datasources[0].id === SparkConstants.HIVE_DATASOURCE) {
            return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
        } else {
            let subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
            return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                + datasources[0].id + "\", sqlContext)\n";
        }
    }

    /**
     * Generates a Spark script for the specified visual query model and data sources.
     *
     * @param visualQueryModel - the visual query model
     */
    protected fromVisualQueryModel(visualQueryModel: VisualQueryModel): string {
        let self = this;
        let tree = this.VisualQueryService.sqlBuilder(visualQueryModel).buildTree();

        // Group targets by table
        let targetsByTableAlias = {};

        tree.targetList.forEach(function (target: ResTarget) {
            if (typeof targetsByTableAlias[target.val.fields[0]] === "undefined") {
                targetsByTableAlias[target.val.fields[0]] = [];
            }
            targetsByTableAlias[target.val.fields[0]].push(target);
        });

        // Build join script
        let joinScript = "";
        let tablesByAlias: { [s: string]: RangeVar } = {};

        for (const expr of tree.fromClause) {
            if (joinScript.length === 0) {
                joinScript += "var " + SparkConstants.DATA_FRAME_VARIABLE + " = " + self.getJoinScript(expr, tablesByAlias, true);
            } else {
                joinScript += self.getJoinScript(expr, tablesByAlias, false);
            }
        }

        // Build table script
        let script = "";

        _.keys(tablesByAlias).sort().forEach(function (alias) {
            let table = tablesByAlias[alias];

            script += "val " + alias + " = ";
            if (typeof table.datasourceId === "string" && table.datasourceId !== SparkConstants.HIVE_DATASOURCE) {
                script += DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\", \"" + table.datasourceId
                    + "\", sqlContext)";
            } else {
                script += "sqlContext.table(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\")"
            }
            script += ".alias(\"" + alias + "\")\n";
        });

        let firstTarget = true;
        script += joinScript + ".select(";
        tree.targetList.forEach(function (target: ResTarget) {
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
    }

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
    private getJoinScript(expr: JoinExpr | RangeVar, tablesByAlias: { [s: string]: RangeVar }, first: boolean): string {
        if (expr.type === this.VisualQueryService.NodeTag.RangeVar) {
            let rangeVar = expr as RangeVar;
            tablesByAlias[rangeVar.aliasName] = rangeVar;
            if (first) {
                return rangeVar.aliasName;
            } else {
                return ".join(" + rangeVar.aliasName + ")";
            }
        } else if (expr.type === this.VisualQueryService.NodeTag.JoinExpr) {
            let joinExpr = expr as JoinExpr;
            tablesByAlias[joinExpr.rarg.aliasName] = joinExpr.rarg;

            let script = this.getJoinScript(joinExpr.larg, tablesByAlias, first);
            script += ".join(" + joinExpr.rarg.aliasName;

            if (joinExpr.jointype !== this.VisualQueryService.JoinType.JOIN) {
                script += ", ";
                if (joinExpr.quals !== null) {
                    script += this.getQualifierScript(joinExpr.quals);
                } else {
                    script += "functions.lit(1)";
                }

                script += ", ";
                if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_INNER) {
                    script += "\"inner\"";
                } else if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_LEFT) {
                    script += "\"left_outer\"";
                } else if (joinExpr.jointype === this.VisualQueryService.JoinType.JOIN_RIGHT) {
                    script += "\"right_outer\"";
                } else {
                    throw new Error("Not a supported join type: " + joinExpr.jointype);
                }
            }

            script += ")";
            return script;
        } else {
            throw new Error("Unsupported type: " + expr.type);
        }
    }

    /**
     * Generates a Spark script for the specified qualifier expression.
     *
     * @param qualifier - the qualifier expression
     * @returns the Spark script
     * @throws {Error} if the qualifier expression is not valid
     */
    private getQualifierScript(qualifier: A_Expr | BoolExpr): string {
        if (qualifier.type === this.VisualQueryService.NodeTag.A_Expr) {
            let aExpr = qualifier as A_Expr;
            return aExpr.lexpr.fields[0] + ".col(\"" + StringUtils.escapeScala(aExpr.lexpr.fields[1]) + "\").equalTo(" + aExpr.rexpr.fields[0] + ".col(\""
                + StringUtils.escapeScala(aExpr.rexpr.fields[1]) + "\"))";
        } else if (qualifier.type === this.VisualQueryService.NodeTag.BoolExpr) {
            let boolExpr = qualifier as BoolExpr;
            return this.getQualifierScript(boolExpr.args[0]) + ".and(" + this.getQualifierScript(boolExpr.args[1]) + ")";
        } else {
            throw new Error("Unsupported type: " + qualifier.type);
        }
    }
}

import * as _ from "underscore";

import {UserDatasource} from "../../../model/user-datasource";
import {A_Expr, BoolExpr, JoinExpr, JoinType, RangeVar, ResTarget, SqlDialect, VisualQueryModel, VisualQueryService} from "../../../services/VisualQueryService";
import {QueryParser} from "../../wrangler/query-parser";
import {SparkConstants} from "./spark-constants";
import {StringUtils} from "../../../../common/utils/StringUtils";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import {DataSource} from "../../../catalog/api/models/datasource";

/** Name of the DatasourceProvider variable */
export const DATASOURCE_PROVIDER = "datasourceProvider";

export const DATASET_PROVIDER = "catalogDataSetProvider";

/**
 * Handles transformations from a visual query model to Spark.
 */
export class SparkQueryParser extends QueryParser {

    /**
     * Constructs a {@code SparkQueryParser}.
     */
    constructor(visualQueryService: VisualQueryService) {
        super(visualQueryService);
    }

    /**
     * Generates a Spark script for the specified SQL query and optional data source.
     *
     * @param  sql - the SQL query
     * @param datasources - the data source
     * @returns the Spark script
     * @throws {Error} if there are too many data sources
     */
    protected fromSql(sql: string, datasources: UserDatasource[], catalogDataSources?:DataSource[]): string {
            if(catalogDataSources){
                return this.fromCatalogDataSourceSql(sql,catalogDataSources);
            }
            else {
                return this.fromUserDatasourceSql(sql,datasources);
            }
    }

    protected fromUserDatasourceSql(sql: string, datasources: UserDatasource[]): string {

                if (datasources != null && datasources.length !== 1) {
                    throw new Error("Not valid datasources: " + datasources);
                } else if (datasources != null && datasources.length > 0 && datasources[0].id === SparkConstants.USER_FILE_DATASOURCE) {
                    return "";
                } else if (datasources == null || datasources.length === 0 || datasources[0].id === SparkConstants.HIVE_DATASOURCE) {
                    return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
                } else {
                    let subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
                    return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                        + datasources[0].id + "\", sqlContext)\n";
                }
        }


    private fromCatalogDataSourceSql(sql: string, catalogDataSources?: DataSource[]): string {
        let hiveDataSource: DataSource = null;
        if(catalogDataSources) {
            hiveDataSource = catalogDataSources.find(ds => ds.connector.pluginId == "hive");
        }
        if (catalogDataSources != null && catalogDataSources.length !== 1) {
            //only allowed if using 1 datasource
            throw new Error("Not valid datasources: " + catalogDataSources);
        } else if (catalogDataSources == null || (hiveDataSource != null && hiveDataSource != undefined && catalogDataSources[0].id === hiveDataSource.id)) {
            return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
        } else {
            let subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
            return "var " + SparkConstants.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromCatalogDataSource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                + catalogDataSources[0].id + "\", sqlContext)\n";
        }
    }

    /**
     * Generates a Spark script for the specified visual query model and data sources.
     *
     * @param visualQueryModel - the visual query model
     */
    protected fromVisualQueryModel(visualQueryModel: VisualQueryModel): string {
        let self = this;
        let tree = VisualQueryService.sqlBuilder(visualQueryModel, SqlDialect.HIVE).buildTree();

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
            //TODO for A2A release change this logic to use table.dataset first
            //This check here will use hive sqlContext instead of the kyloCatalog for Hive data sources
            if (table.dataset != undefined || (typeof table.datasourceId === "string" && table.datasourceId.toLowerCase() !== SparkConstants.HIVE_DATASOURCE.toLowerCase() )) {
                if(table.dataset != undefined && !table.datasetMatchesUserDataSource ) {
                    script += DATASET_PROVIDER +".read(\""+table.dataset.id+"\")";
                }else {
                    script += DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\", \"" + table.datasourceId
                        + "\", sqlContext)";
                }
            } else {
                script += "sqlContext.table(\"" + StringUtils.escapeScala(table.schemaname + "." + table.relname) + "\")"
            }
            script += ".alias(\"" + alias + "\")\n";
        });


        script += joinScript+this.joinSelect(tree.targetList);


        return script;
    }

    public joinSelect(targetList:ResTarget[]){
        let firstTarget = true;
        let script = ".select(";
        targetList.forEach(function (target: ResTarget) {
            if (firstTarget) {
                firstTarget = false;
            } else {
                script += ", ";
            }
            script += target.val.fields[0] + ".col(\"" + StringUtils.escapeScala(target.val.fields[1]) + "\")";
            if (target.name !== null || target.description !== null) {
                script += ".as(\"" + StringUtils.escapeScala((target.name !== null) ? target.name : target.val.fields[1]) + "\"";
                if (target.description !== null && target.description != undefined) {
                    script += ", new org.apache.spark.sql.types.MetadataBuilder().putString(\"comment\", \"" + StringUtils.escapeScala(target.description) + "\").build()";
                }
                script += ")"
            }
        });
        script += ")\n";
        return script;
    }

    public parseJoinType(joinType:JoinType) {
        let join = "";
        if (joinType === VisualQueryService.JoinType.JOIN_INNER) {
            join = "inner";
        } else if (joinType=== VisualQueryService.JoinType.JOIN_LEFT) {
            join =  "leftouter";
        } else if (joinType === VisualQueryService.JoinType.JOIN_RIGHT) {
            join = "rightouter"
        } else if (joinType === VisualQueryService.JoinType.FULL_JOIN) {
          join = "fullouter"
        }   else {
            throw new Error("Not a supported join type: " + joinType);
        }
        return join;
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
        if (expr.type === VisualQueryService.NodeTag.RangeVar) {
            let rangeVar = expr as RangeVar;
            tablesByAlias[rangeVar.aliasName] = rangeVar;
            if (first) {
                return rangeVar.aliasName;
            } else {
                return ".join(" + rangeVar.aliasName + ")";
            }
        } else if (expr.type === VisualQueryService.NodeTag.JoinExpr) {
            let joinExpr = expr as JoinExpr;
            tablesByAlias[joinExpr.rarg.aliasName] = joinExpr.rarg;

            let script = this.getJoinScript(joinExpr.larg, tablesByAlias, first);
            script += ".join(" + joinExpr.rarg.aliasName;

            if (joinExpr.jointype !== VisualQueryService.JoinType.JOIN) {
                script += ", ";
                if (joinExpr.quals !== null) {
                    script += this.getQualifierScript(joinExpr.quals);
                } else {
                    script += "functions.lit(1)";
                }

                script += ", ";
                script +="\""+this.parseJoinType(joinExpr.jointype)+"\"";
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
        if (qualifier.type === VisualQueryService.NodeTag.A_Expr) {
            let aExpr = qualifier as A_Expr;
            return aExpr.lexpr.fields[0] + ".col(\"" + StringUtils.escapeScala(aExpr.lexpr.fields[1]) + "\").equalTo(" + aExpr.rexpr.fields[0] + ".col(\""
                + StringUtils.escapeScala(aExpr.rexpr.fields[1]) + "\"))";
        } else if (qualifier.type === VisualQueryService.NodeTag.BoolExpr) {
            let boolExpr = qualifier as BoolExpr;
            return this.getQualifierScript(boolExpr.args[0]) + ".and(" + this.getQualifierScript(boolExpr.args[1]) + ")";
        } else {
            throw new Error("Unsupported type: " + qualifier.type);
        }
    }
}

import {QueryEngine} from "../query-engine";
import {UserDatasource} from "../../../model/user-datasource";
import {Observable} from "rxjs/Observable";
import {DatasourcesServiceStatic} from "../../../services/DatasourcesService.typings";
import {Subject} from "rxjs/Subject";
import {UnderscoreStatic} from "underscore";
import {TeradataScript} from "./teradata-script";
import {TeradataScriptBuilder} from "./teradata-script-builder";
import {Program} from "@types/estree";
import {JdbcDatasource} from "../../../model/jdbc-datasource";
import {TeradataQueryParser} from "./teradata-query-parser";
import {SqlDialect} from "../../../services/VisualQueryService";
import {ProfileOutputRow} from "../../../model/profile-output-row";
import {QueryResultColumn} from "../../../model/query-result-column";
import {TransformDataComponent} from "../../transform-data.component";
import {ColumnDelegate} from "../column-delegate";
import {TeradataColumnDelegate} from "./teradata-column-delegate";
import {QueryEngineConstants} from "../query-engine-constants";

declare const _: UnderscoreStatic;
declare const angular: angular.IAngularStatic;

/**
 * Generates a SQL query to be executed by a Teradata database.
 */
export class TeradataQueryEngine extends QueryEngine<TeradataScript> {

    /**
     * URL to the functions endpoint
     */
    private functionsUrl: string;

    /**
     * URL to the transform endpoint
     */
    private transformUrl: string;

    /**
     * Constructs a {@code TeradataQueryEngine}.
     */
    constructor(private $http: angular.IHttpService, $mdDialog: angular.material.IDialogService, DatasourcesService: DatasourcesServiceStatic.DatasourcesService, RestUrlService: any,
                uiGridConstants: any, private VisualQueryService: any) {
        super($mdDialog, DatasourcesService, uiGridConstants);

        // Initialize properties
        this.functionsUrl = RestUrlService.UI_BASE_URL + "/teradata-functions";
        this.transformUrl = RestUrlService.CONTROLLER_SERVICES_BASE_URL;
    }

    /**
     * Indicates if both limit and sample can be applied at the same time.
     */
    get allowLimitWithSample(): boolean {
        return false;
    }

    /**
     * Gets the sample formulas.
     */
    get sampleFormulas(): { name: string; formula: string }[] {
        return [
            {name: "Group By", formula: "select(COLUMNS).groupBy(COLUMNS)"}
        ];
    }

    /**
     * Gets the SQL dialect used by this engine.
     */
    get sqlDialect(): SqlDialect {
        return SqlDialect.TERADATA;
    }

    /**
     * Creates a column delegate of the specified data type.
     */
    createColumnDelegate(dataType: string, controller: TransformDataComponent): ColumnDelegate {
        return new TeradataColumnDelegate(dataType, controller, this.$mdDialog, this.uiGridConstants);
    }

    /**
     * Gets the type definitions for the output columns of the current script. These definitions are only available after receiving a {@link #transform} response.
     *
     * @returns the column type definitions
     */
    getColumnDefs(): any {
        // Set directives
        const defs = {
            "!name": "columns"
        };

        if (typeof this.defs_[QueryEngineConstants.DEFINE_DIRECTIVE] !== "undefined") {
            defs[QueryEngineConstants.DEFINE_DIRECTIVE] = this.defs_[QueryEngineConstants.DEFINE_DIRECTIVE];
        }

        // Add column names
        const columns = this.getState().columns;

        if (columns !== null) {
            columns.forEach(function (column) {
                defs[column.displayName] = QueryEngineConstants.TERNJS_COLUMN_TYPE;
            });
        }

        return defs;
    }

    /**
     * Gets the Hive column label for the field with the specified name.
     *
     * @param fieldName - the field name
     * @returns the Hive column label if the column exists, or {@code null} otherwise
     */
    getColumnLabel(fieldName: string): string | null {
        for (let i = this.states_.length - 1; i >= 0; --i) {
            let columns = this.states_[i].columns;
            if (columns !== null) {
                for (let column of columns) {
                    if (column.displayName === fieldName) {
                        return column.field;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the field name of the specified column.
     */
    getColumnName(column: QueryResultColumn): string {
        return column.field;
    }

    /**
     * Gets the Teradata SQL script.
     *
     * @param start - index of the first transformation
     * @param end - index of the last transformation
     * @param sample - false to disable sampling
     * @returns the Spark script
     */
    getScript(start: number = null, end: number = null, sample: boolean = true): string {
        // Parse arguments
        end = (end !== null) ? end + 1 : this.states_.length;

        // Build script
        let sql = this.source_;

        if (sample) {
            if (this.limit_ > 0) {
                sql = "SELECT TOP " + this.limit_ + " " + sql.substr(7);
            }
            if (this.sample_ > 0 && this.sample_ < 1) {
                sql += " SAMPLE " + this.sample_;
            }
        }

        for (let i = 1; i < end; ++i) {
            const script = this.states_[i].script;
            const table = "query" + i;
            sql = "SELECT " + (script.keywordList !== null ? script.keywordList + " " : "") + (script.selectList !== null ? script.selectList.replace(/^\*/, table + ".*") + " " : table + ".*")
                + "FROM (" + sql + ") " + table
                + (script.where !== null ? " WHERE " + script.where : "")
                + (script.groupBy !== null ? " GROUP BY " + script.groupBy : "")
                + (script.having !== null ? " HAVING " + script.having : "");
        }

        return sql;
    }

    /**
     * Lists the name of the supported data source type.
     */
    getSupportedDataSourceNames(): string[] {
        return ["Teradata JDBC"];
    }

    /**
     * Fetches the Ternjs definitions for this query engine.
     */
    getTernjsDefinitions(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.$http.get(this.functionsUrl)
                .then(function (response: any) {
                    resolve(response.data);
                }, function (err: string) {
                    reject(err);
                });
        });
    }

    /**
     * The number of rows to select in the initial query.
     *
     * @param value - the new value
     * @returns the number of rows
     */
    limit(value?: number): number {
        if (typeof value !== "undefined") {
            this.sample_ = 1;
        }
        return super.limit(value);
    }

    /**
     * The fraction of rows to include when sampling.
     *
     * @param value - the new value
     * @returns the fraction of rows
     */
    sample(value?: number): number {
        if (typeof value !== "undefined") {
            this.limit_ = 0;
        }
        return super.sample(value);
    }

    /**
     * Indicates if this engine supports the specified data source.
     */
    supportsDataSource(dataSource: UserDatasource): boolean {
        return (typeof dataSource.type === "string" && dataSource.type.toLowerCase() === "teradata");
    }

    /**
     * Runs the current script on the server.
     *
     * @return an observable for the response progress
     */
    transform(): Observable<any> {
        // Build the request body
        let index = this.states_.length - 1;

        // Create the response handlers
        let self = this;
        let deferred = new Subject();

        let successCallback = function (response: any) {
            // Verify column names
            let invalid = _.find(response.data.columns, function (column: any) {
                return (column.hiveColumnLabel.match(/[.`]/) !== null);  // Escaping backticks not supported until Spark 2.0
            });
            let reserved = _.find(response.data.columns, function (column: any) {
                return (column.hiveColumnLabel === "processing_dttm");
            });

            let state = self.states_[index];
            if (angular.isDefined(invalid)) {
                state.columns = [];
                state.rows = [];
                deferred.error("Column name '" + invalid.hiveColumnLabel + "' is not supported. Please choose a different name.");
            } else if (angular.isDefined(reserved)) {
                state.columns = [];
                state.rows = [];
                deferred.error("Column name '" + reserved.hiveColumnLabel + "' is reserved. Please choose a different name.");
            } else {
                state.columns = response.data.columns.map((column: QueryResultColumn): QueryResultColumn => {
                    if (!column.displayName.match(/^[A-Za-z0-9]+$/)) {
                        let index = 0;
                        let newName = "";
                        while (newName.length === 0 || response.data.columnDisplayNameMap[newName]) {
                            newName = "col" + (index++);
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
                    } else {
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
        let errorCallback = function (response: any) {
            // Update state
            let state = self.states_[index];
            state.columns = [];
            state.rows = [];

            // Respond with error message
            let message;

            if (angular.isString(response.data.message)) {
                message = (response.data.message.length <= 1024) ? response.data.message : response.data.message.substr(0, 1021) + "...";
            } else {
                message = "An unknown error occurred.";
            }

            deferred.error(message);
        };

        // Send the request
        self.$http({
            data: this.getScript(),
            method: "POST",
            url: this.transformUrl + "/" + (this.datasources_[0] as JdbcDatasource).controllerServiceId + "/query",
            responseType: "json"
        }).then(successCallback, errorCallback);
        return deferred;
    }

    /**
     * Parses the specified tree into a script for the current state.
     */
    protected parseAcornTree(tree: any): TeradataScript {
        return new TeradataScriptBuilder(this.defs_, this).toScript(tree as Program) as any;
    }

    /**
     * Parses the specified source into a script for the initial state.
     */
    protected parseQuery(source: any): string {
        return new TeradataQueryParser(this.VisualQueryService).toScript(source, this.datasources_);
    }

    /**
     * Generates the profile statistics for the specified data.
     */
    private generateProfile(columns: QueryResultColumn[], rows: { [k: string]: any }[]): ProfileOutputRow[] {
        let profile = [];
        for (let i = 0; i < columns.length; ++i) {
            let field = columns[i].field;
            let nulls = 0;
            let values = new Set();
            rows.map(row => row[field]).forEach(value => {
                if (value === null) {
                    ++nulls;
                }
                values.add(value);
            });

            profile.push({columnName: field, metricType: "NULL_COUNT", metricValue: nulls});
            profile.push({columnName: field, metricType: "TOTAL_COUNT", metricValue: rows.length});
            profile.push({columnName: field, metricType: "UNIQUE_COUNT", metricValue: values.size});
            profile.push({columnName: field, metricType: "PERC_NULL_VALUES", metricValue: nulls / rows.length * 100});
            profile.push({columnName: field, metricType: "PERC_UNIQUE_VALUES", metricValue: values.size / rows.length * 100});
            profile.push({columnName: field, metricType: "PERC_DUPLICATE_VALUES", metricValue: 100 - (values.size / rows.length * 100)});
        }
        return profile;
    }
}

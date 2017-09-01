import {QueryEngine} from "../query-engine";
import {UserDatasource} from "../../../model/user-datasource";
import {SparkConstants} from "./spark-constants";
import {SparkScriptBuilder} from "./spark-script-builder";
import {SparkQueryParser} from "./spark-query-parser";
import {Program} from "estree";
import {UnderscoreStatic} from "underscore";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {TableSchema} from "../../../model/table-schema";
import {DatasourcesServiceStatic} from "../../../services/DatasourcesService.typings";
import {SqlDialect} from "../../../services/VisualQueryService";

declare const _: UnderscoreStatic;
declare const angular: angular.IAngularStatic;

/**
 * Generates a Scala script to be executed by Kylo Spark Shell.
 */
export class SparkQueryEngine extends QueryEngine<string> {

    /**
     * URL to the API server
     */
    private apiUrl: string;

    /**
     * Constructs a {@code SparkQueryEngine}.
     */
    constructor(private $http: angular.IHttpService, private $timeout: angular.ITimeoutService, DatasourcesService: DatasourcesServiceStatic.DatasourcesService, private HiveService: any,
                private RestUrlService: any, private VisualQueryService: any) {
        super(DatasourcesService);

        // Initialize properties
        this.apiUrl = RestUrlService.SPARK_SHELL_SERVICE_URL;

        // Ensure Kylo Spark Shell is running
        $http.post(RestUrlService.SPARK_SHELL_SERVICE_URL + "/start", null);
    }

    /**
     * Indicates if multiple data sources are allowed in the same query.
     */
    get allowMultipleDataSources(): boolean {
        return true;
    }

    /**
     * Gets the SQL dialect used by this engine.
     */
    get sqlDialect(): SqlDialect {
        return SqlDialect.HIVE;
    }

    /**
     * Returns the data sources that are supported natively by this engine.
     */
    getNativeDataSources(): Promise<UserDatasource[]> {
        return new Promise(resolve => resolve([{id: SparkConstants.HIVE_DATASOURCE, name: "Hive"} as UserDatasource]));
    }

    /**
     * Gets the Spark script.
     *
     * @param start - the index of the first transformation
     * @param end - the index of the last transformation
     * @param sample - {@code false} to disable sampling
     * @returns the Spark script
     */
    getScript(start: number = 0, end: number = null, sample: boolean = true): string {
        // Parse arguments
        end = (end !== null) ? end + 1 : this.states_.length;

        // Build script
        let sparkScript = "import org.apache.spark.sql._\n";

        if (start === 0) {
            sparkScript += this.source_;
            sparkScript += SparkConstants.DATA_FRAME_VARIABLE + " = " + SparkConstants.DATA_FRAME_VARIABLE;
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
            sparkScript += "var " + SparkConstants.DATA_FRAME_VARIABLE + " = parent\n";
        }

        for (let i = start; i < end; ++i) {
            sparkScript += SparkConstants.DATA_FRAME_VARIABLE + " = " + SparkConstants.DATA_FRAME_VARIABLE + this.states_[i].script + "\n";
        }

        sparkScript += SparkConstants.DATA_FRAME_VARIABLE + "\n";
        return sparkScript;
    }

    /**
     * Gets the schema for the specified table.
     *
     * @param schema - name of the database or schema
     * @param table - name of the table
     * @param datasourceId - id of the datasource
     * @returns the table schema
     */
    getTableSchema(schema: string, table: string, datasourceId: string): Promise<TableSchema> {
        if (datasourceId === SparkConstants.HIVE_DATASOURCE) {
            const self = this;
            return new Promise((resolve, reject) => {
                self.$http.get(self.RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table)
                    .then(function (response: any) {
                        resolve(response.data);
                    }, function (response: any) {
                        reject(response.data);
                    });
            });
        } else {
            return super.getTableSchema(schema, table, datasourceId);
        }
    }

    /**
     * Fetches the Ternjs definitions for this query engine.
     */
    getTernjsDefinitions(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.$http.get(this.RestUrlService.UI_BASE_URL + "/spark-functions")
                .then(function (response: any) {
                    resolve(response.data);
                }, function (err: string) {
                    reject(err);
                });
        });
    }

    /**
     * Searches for table names matching the specified query.
     *
     * @param query - search query
     * @param datasourceId - datasource to search
     * @returns the list of table references
     */
    searchTableNames(query: string, datasourceId: string): DatasourcesServiceStatic.TableReference[] | Promise<DatasourcesServiceStatic.TableReference[]> {
        if (datasourceId === SparkConstants.HIVE_DATASOURCE) {
            const tables = this.HiveService.queryTablesSearch(query);
            if (tables.then) {
                return new Promise((resolve, reject) => tables.then(resolve, reject));
            } else {
                return tables;
            }
        } else {
            return super.searchTableNames(query, datasourceId);
        }
    }

    /**
     * Runs the current Spark script on the server.
     *
     * @return an observable for the response progress
     */
    transform(): Observable<any> {
        // Build the request body
        let body = {};
        let index = this.states_.length - 1;

        if (index > 0) {
            // Find last cached state
            let last = index - 1;
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
            body["datasources"] = this.datasources_.filter(datasource => datasource.id !== SparkConstants.HIVE_DATASOURCE);
        }

        // Create the response handlers
        let self = this;
        let deferred = new Subject();

        let successCallback = function (response: any) {
            // Check status
            if (response.data.status === "PENDING") {
                deferred.next(response.data.progress);

                self.$timeout(function () {
                    self.$http({
                        method: "GET",
                        url: self.apiUrl + "/transform/" + response.data.table,
                        headers: {"Content-Type": "application/json"},
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
            let invalid = _.find(response.data.results.columns, function (column: any) {
                return (column.hiveColumnLabel.match(/[.`]/) !== null);  // Escaping backticks not supported until Spark 2.0
            });
            let reserved = _.find(response.data.results.columns, function (column: any) {
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
                state.columns = response.data.results.columns;
                state.profile = response.data.profile;
                state.rows = response.data.results.rows;
                state.table = response.data.table;
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
            method: "POST",
            url: this.apiUrl + "/transform",
            data: JSON.stringify(body),
            headers: {"Content-Type": "application/json"},
            responseType: "json"
        }).then(successCallback, errorCallback);
        return deferred;
    }

    /**
     * Parses the specified tree into a script for the current state.
     */
    protected parseAcornTree(tree: any): string {
        return new SparkScriptBuilder(this.defs_, this).toScript(tree as Program);
    }

    /**
     * Parses the specified source into a script for the initial state.
     */
    protected parseQuery(source: any): string {
        return new SparkQueryParser(this.VisualQueryService).toScript(source, this.datasources_);
    }
}

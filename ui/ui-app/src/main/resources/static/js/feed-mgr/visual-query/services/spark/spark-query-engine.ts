import {HttpClient} from "@angular/common/http";
import {Injector} from "@angular/core";
import * as angular from "angular";
import {Program} from "estree";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/observable/interval";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/expand";
import "rxjs/add/operator/map";
import "rxjs/add/operator/mergeMap";
import "rxjs/add/operator/take";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import * as _ from "underscore";

import {SchemaField} from "../../../model/schema-field";
import {TableSchema} from "../../../model/table-schema";
import {UserDatasource} from "../../../model/user-datasource";
import {DatasourcesServiceStatic} from "../../../services/DatasourcesService.typings";
import {SqlDialect} from "../../../services/VisualQueryService";
import {DIALOG_SERVICE} from "../../wrangler/api/index";
import {SaveRequest, SaveResponse, SaveResponseStatus} from "../../wrangler/api/rest-model";
import {DialogService} from "../../wrangler/api/services/dialog.service";
import {ColumnController} from "../../wrangler/column-controller";
import {ColumnDelegate} from "../../wrangler/column-delegate";
import {QueryResultColumn} from "../../wrangler/model/query-result-column";
import {ScriptState} from "../../wrangler/model/script-state";
import {TransformResponse} from "../../wrangler/model/transform-response";
import {QueryEngine} from "../../wrangler/query-engine";
import {SparkColumnDelegate} from "./spark-column";
import {SparkConstants} from "./spark-constants";
import {SparkQueryParser} from "./spark-query-parser";
import {SparkScriptBuilder} from "./spark-script-builder";
import {PageSpec} from "../../wrangler/query-engine";

/**
 * Generates a Scala script to be executed by Kylo Spark Shell.
 */
export class SparkQueryEngine extends QueryEngine<string> {

    /**
     * URL to the API server
     */
    private apiUrl: string;

    /**
     * Wrangler dialog service.
     */
    private dialog: DialogService;

    private VALID_NAME_PATTERN = /[^a-zA-Z0-9\s_]|\s/g;

    static readonly $inject: string[] = ["$http", "$mdDialog", "$timeout", "DatasourcesService", "HiveService", "RestUrlService", "uiGridConstants", "VisualQueryService", "$$wranglerInjector"];

    /**
     * Constructs a {@code SparkQueryEngine}.
     */
    constructor(private $http: angular.IHttpService, $mdDialog: angular.material.IDialogService, private $timeout: angular.ITimeoutService,
                DatasourcesService: DatasourcesServiceStatic.DatasourcesService, private HiveService: any, private RestUrlService: any, uiGridConstants: any, private VisualQueryService: any,
                private $$angularInjector?: Injector) {
        super($mdDialog, DatasourcesService, uiGridConstants, $$angularInjector);

        // Initialize properties
        this.apiUrl = RestUrlService.SPARK_SHELL_SERVICE_URL;
        this.dialog = $$angularInjector.get(DIALOG_SERVICE);

        // Ensure Kylo Spark Shell is running
        $http.post(RestUrlService.SPARK_SHELL_SERVICE_URL + "/start", null);
    }

    /**
     * Indicates if both limit and sample can be applied at the same time.
     */
    get allowLimitWithSample(): boolean {
        return true;
    }

    /**
     * Indicates if multiple data sources are allowed in the same query.
     */
    get allowMultipleDataSources(): boolean {
        return true;
    }

    /**
     * Gets the sample formulas.
     */
    get sampleFormulas(): { name: string; formula: string }[] {
        return [
            {name: "Aggregate", formula: "groupBy(COLUMN).agg(count(COLUMN), sum(COLUMN))"},
            {name: "Conditional", formula: "when(CONDITION, VALUE).when(CONDITION, VALUE).otherwise(VALUE)"},
            {name: "Pivot", formula: "groupBy(COLUMN).pivot(COLUMN).agg(count(COLUMN))"},
            {name: "Window", formula: "sum(COLUMN).over(orderBy(COLUMN))"}
        ];
    }

    /**
     * Gets the SQL dialect used by this engine.
     */
    get sqlDialect(): SqlDialect {
        return SqlDialect.HIVE;
    }

    /**
     * Indicates that the Hive data type should be used.
     */
    get useNativeDataType(): boolean {
        return false;
    }

    /**
     * Creates a column delegate of the specified data type.
     */
    createColumnDelegate(dataType: string, controller: ColumnController, column: any): ColumnDelegate {
        return new SparkColumnDelegate(column, dataType, controller, this.$mdDialog, this.uiGridConstants, this.dialog, this.$$angularInjector.get(HttpClient), this.RestUrlService);
    }

    /**
     * Gets the field name for the specified column.
     */
    getColumnName(column: QueryResultColumn): string {
        return column.displayName;
    }

    /**
     * Returns valid alpha numeric name
     * @param {string} label
     * @return {string}
     */
    getValidHiveColumnName(label: string) {
        return label.replace(this.VALID_NAME_PATTERN, '')
    }

    /**
     * Gets the schema fields  for the the current transformation.
     *
     * @returns the schema fields or {@code null} if the transformation has not been applied
     */
    getFields(): SchemaField[] | null {
        var self = this;
        // Get list of columns
        const columns = this.getColumns();
        if (columns === null) {
            return null;
        }

        // Get field list
        return columns.map(function (col: any) {
            let dataType;
            //comment out decimal to double.  Decimals are supported ... will remove after testing
            if (col.dataType.startsWith("decimal")) {
                dataType = "decimal";
            } else if (col.dataType === "smallint") {
                dataType = "int";
            } else {
                dataType = col.dataType;
            }
            var name = angular.isDefined(col.displayName) ? self.getValidHiveColumnName(col.displayName) : col.hiveColumnLabel;

            const colDef = {name: name, description: col.comment, dataType: dataType, primaryKey: false, nullable: false, sampleValues: []} as SchemaField;
            if (dataType === 'decimal') {
                //parse out the precisionScale
                let precisionScale = '20,2';
                if (col.dataType.indexOf("(") > 0) {
                    precisionScale = col.dataType.substring(col.dataType.indexOf("(") + 1, col.dataType.length - 1);
                }
                colDef.precisionScale = precisionScale;
            }
            colDef.derivedDataType = dataType;
            return colDef;
        });
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
    getScript(start: number = null, end: number = null, sample: boolean = true): string {
        // Parse arguments
        start = (start !== null) ? start : 0;
        end = (end !== null) ? end + 1 : this.states_.length;

        // Build script
        let sparkScript = "import org.apache.spark.sql._\n";

        if (start === 0) {


            if (this.hasSampleFile()) {
                //we are working with a file.. add the spark code to use it
                //extract options out from a variable to do the parsing
                sparkScript += this.sampleFile.script;
                sparkScript += "\n";
                sparkScript += SparkConstants.DATA_FRAME_VARIABLE + " = " + SparkConstants.DATA_FRAME_VARIABLE;
            }else {
                sparkScript += this.source_;
                sparkScript += SparkConstants.DATA_FRAME_VARIABLE + " = " + SparkConstants.DATA_FRAME_VARIABLE;
            }
            //limit

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
            if (!this.states_[i].inactive) {
                sparkScript += SparkConstants.DATA_FRAME_VARIABLE + " = " + SparkConstants.DATA_FRAME_VARIABLE + this.states_[i].script + "\n";
            }
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
     * Saves the results to the specified destination.
     *
     * @param request - save target
     * @returns an observable tracking the save status
     */
    saveResults(request: SaveRequest): Observable<SaveResponse> {
        // Build the request body
        let body = {
            async: true,
            datasources: (this.datasources_ !== null) ? this.datasources_.filter(datasource => datasource.id !== SparkConstants.HIVE_DATASOURCE) : null,
            script: this.getFeedScript()
        };

        if (request.jdbc && request.jdbc.id === SparkConstants.HIVE_DATASOURCE) {
            request.jdbc = null;
        }

        // Send the request
        let transformId: string;

        return Observable
        // Send transform script
            .fromPromise(this.$http<TransformResponse>({
                method: "POST",
                url: this.apiUrl + "/transform",
                data: JSON.stringify(body),
                headers: {"Content-Type": "application/json"},
                responseType: "json"
            }))
            // Send save request
            .mergeMap(response => {
                transformId = response.data.table;
                return this.$http<SaveResponse>({
                    method: "POST",
                    url: this.apiUrl + "/transform/" + transformId + "/save",
                    data: JSON.stringify(request),
                    headers: {"Content-Type": "application/json"},
                    responseType: "json"
                });
            })
            // Wait for save to complete
            .expand(response => {
                if (response.data.status === SaveResponseStatus.PENDING || response.data.status === SaveResponseStatus.LIVY_PENDING) {
                    return Observable.interval(1000)
                        .take(1)
                        .mergeMap(() => this.$http<SaveResponse>({
                            method: "GET",
                            url: this.apiUrl + "/transform/" + transformId + "/save/" + response.data.id,
                            responseType: "json"
                        }));
                } else if (response.data.status === SaveResponseStatus.SUCCESS) {
                    return Observable.empty();
                } else {
                    throw response;
                }
            })
            // Map result to SaveResponse
            .map(response => {
                const save = response.data;
                if (save.location != null && save.location.startsWith("./")) {
                    save.location = this.apiUrl + "/transform/" + transformId + "/save/" + save.id + save.location.substr(1);
                }
                return save;
            })
            .catch((response: angular.IHttpResponse<SaveResponse>): Observable<SaveResponse> => {
                throw response.data;
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

    decodeError(msg:string) : string {
        if (msg != null) {

            if (msg.indexOf("Cannot read property") > -1) {
                msg = "Please ensure fieldnames are correct.";
            } else if (msg.indexOf("Program is too long") > -1) {
                msg = "Please check parenthesis align."
            } else if (msg.indexOf("MatchError: ") > -1) {
                msg = "Please remove, impute, or replace all empty values and try again."
            } else if (msg.indexOf("AnalysisException: Can't extract value from ") > -1) {
                msg = "Action would invalidate downstream transformations or requires an upstream transformation that has been disabled.";
            } else if (msg.indexOf("Unsupported literal type class [D") > -1) {
                msg = "Function not available on present version of Spark.";
            }
        }
        return msg;
    }

    /**
     * Runs the current Spark script on the server.
     *
     * @return an observable for the response progress
     */
    transform(pageSpec ?: PageSpec, doValidate: boolean = true, doProfile: boolean = false): Observable<any> {
        // Build the request body

        if (!pageSpec) {
            pageSpec = PageSpec.defaultPage();
        }

        let body = {
            "policies": this.getState().fieldPolicies,
            "pageSpec": pageSpec,
            "doProfile": doProfile,
            "doValidate": doValidate
        };
        let index = this.states_.length - 1;

        if (index > -1) {
            // Find last cached state
            let last = index - 1;
            while (last >= 0 && (this.states_[last].table === null)) {
                --last;
            }

            // Add script to body
            if (!this.hasStateChanged()) {
                body["script"] = "import org.apache.spark.sql._\nvar df = parent\ndf";
                last = index;
            } else {
                body["script"] = this.getScript(last + 1, index);
            }

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
        //add in the datasets
        if(this.datasets !== null){
            body["catalogDatasets"] = this.datasets;
        }

        // Create the response handlers
        let self = this;
        let deferred = new Subject();

        let successCallback = function (response: angular.IHttpResponse<TransformResponse>) {
            let state = self.states_[index];
            self.resetStateChange();
            // Check status
            if (response.data.status === "PENDING") {


                if (state.columns === null && response.data.results && response.data.results.columns) {

                    //Unnecessary and causes table refresh problems
                    // state.columns = response.data.results.columns;
                    // state.rows = [];
                    // state.table = response.data.table;
                    // self.updateFieldPolicies(state);
                }

                deferred.next(response.data.progress);

                self.$timeout(function () {
                    self.$http<TransformResponse>({
                        method: "GET",
                        url: self.apiUrl + "/transform/" + response.data.table,
                        headers: {"Content-Type": "application/json"},
                        responseType: "json"
                    }).then(successCallback, errorCallback);
                }, 500, false);
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

            if (angular.isDefined(invalid)) {
                state.rows = [];
                state.columns = [];
                deferred.error("Column name '" + invalid.hiveColumnLabel + "' is not supported. Please choose a different name.");
            } else if (angular.isDefined(reserved)) {
                state.rows = [];
                state.columns = [];
                deferred.error("Column name '" + reserved.hiveColumnLabel + "' is reserved. Please choose a different name.");
            } else {
                // Update state
                state.profile = response.data.profile;
                state.rows = response.data.results.rows;
                state.table = response.data.table;
                state.validationResults = response.data.results.validationResults;
                state.actualCols = response.data.actualCols;
                state.actualRows = response.data.actualRows;
                state.columns = response.data.results.columns;
                self.updateFieldPolicies(state);

                // Indicate observable is complete
                deferred.complete();
            }
        };
        let errorCallback = function (response: angular.IHttpResponse<TransformResponse>) {
            // Update state
            let state = self.states_[index];
            state.columns = [];
            state.rows = [];
            self.resetStateChange();

            // Respond with error message
            let message;

            if (angular.isString(response.data.message)) {
                message = self.decodeError(response.data.message);
                message = (message.length <= 1024) ? message : message.substr(0, 1021) + "...";
            } else {
                message = "An unknown error occurred.";
            }

            deferred.error(message);
        };

        // Send the request
        self.$http<TransformResponse>({
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

    /**
     * Updates the field policies of the specified state to match the column order.
     * @param {ScriptState<string>} state
     */
    private updateFieldPolicies(state: ScriptState<string>) {
        var self = this;
        if (state.fieldPolicies != null && state.fieldPolicies.length > 0) {
            const policyMap = {};
            state.fieldPolicies.forEach(policy => {
                policyMap[policy.name] = policy;
            });

            state.fieldPolicies = state.columns.map(column => {
                var name = angular.isDefined(column.displayName) ? self.getValidHiveColumnName(column.displayName) : column.hiveColumnLabel;
                if (policyMap[name]) {
                    return policyMap[name];
                } else {
                    return {
                        name: name,
                        fieldName: name,
                        feedFieldName: name,
                        domainTypeId: null,
                        partition: null,
                        profile: true,
                        standardization: null,
                        validation: null
                    };
                }
            });
        }
    }

}


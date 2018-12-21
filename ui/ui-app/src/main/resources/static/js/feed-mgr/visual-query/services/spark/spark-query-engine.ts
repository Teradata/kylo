import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {Inject, Injectable, Injector} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {Program} from "estree";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/observable/interval";
import "rxjs/add/operator/expand";
import "rxjs/add/operator/map";
import "rxjs/add/operator/mergeMap";
import {Observable} from "rxjs/Observable";
import {catchError} from "rxjs/operators/catchError";
import {mergeMap} from "rxjs/operators/mergeMap";
import {take} from "rxjs/operators/take";
import {Subject} from "rxjs/Subject";
import * as _ from "underscore";

import {SchemaField} from "../../../model/schema-field";
import {TableSchema} from "../../../model/table-schema";
import {UserDatasource} from "../../../model/user-datasource";
import {DatasourcesService, TableReference} from "../../../services/DatasourcesServiceIntrefaces";
import {HiveService} from "../../../services/HiveService";
import {RestUrlService} from "../../../services/RestUrlService";
import {SqlDialect, VisualQueryService} from "../../../services/VisualQueryService";
import {DIALOG_SERVICE} from "../../wrangler/api/index";
import {SaveRequest, SaveResponse, SaveResponseStatus} from "../../wrangler/api/rest-model";
import {DialogService} from "../../wrangler/api/services/dialog.service";
import {ColumnController} from "../../wrangler/column-controller";
import {ColumnDelegate} from "../../wrangler/column-delegate";
import {QueryResultColumn} from "../../wrangler/model/query-result-column";
import {ScriptState} from "../../wrangler/model/script-state";
import {TransformResponse} from "../../wrangler/model/transform-response";
import {PageSpec, QueryEngine} from "../../wrangler/query-engine";
import {SparkColumnDelegate} from "./spark-column";
import {SparkConstants} from "./spark-constants";
import {DATASET_PROVIDER, SparkQueryParser} from "./spark-query-parser";
import {SparkScriptBuilder} from "./spark-script-builder";
import {HttpBackendClient} from "../../../../services/http-backend-client";

/**
 * Generates a Scala script to be executed by Kylo Spark Shell.
 */
@Injectable()
export class SparkQueryEngine extends QueryEngine<string> {

    private readonly VALID_NAME_PATTERN = /[^a-zA-Z0-9\s_]|\s/g;

    /**
     * URL to the API server
     */
    private readonly apiUrl: string;

    /**
     * Constructs a {@code SparkQueryEngine}.
     */
    constructor(private $http: HttpClient, dialog: TdDialogService, @Inject(DIALOG_SERVICE) private wranglerDialog: DialogService, @Inject("HiveService") private HiveService: HiveService,
                @Inject("RestUrlService") private RestUrlService: RestUrlService, @Inject("VisualQueryService") private VisualQueryService: VisualQueryService, private $$angularInjector: Injector,
                @Inject("DatasourcesService") datasourcesService: any, @Inject("uiGridConstants") uiGridConstants: any, private httpBackendClient:HttpBackendClient) {
        super(dialog, datasourcesService, uiGridConstants, $$angularInjector);

        // Ensure Kylo Spark Shell is running
        this.apiUrl = this.RestUrlService.SPARK_SHELL_SERVICE_URL;
        $http.post(this.RestUrlService.SPARK_SHELL_SERVICE_URL + "/start", null).subscribe();
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
        return new SparkColumnDelegate(column, dataType, controller, this.dialog, this.uiGridConstants, this.wranglerDialog, this.httpBackendClient, this.RestUrlService);
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
        const self = this;
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
            const name = (typeof col.displayName != "undefined") ? self.getValidHiveColumnName(col.displayName) : col.hiveColumnLabel;

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
            } else {
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
        let dsProvider = DATASET_PROVIDER;
        let joinDataSetIds :string[] = [];
        for (let i = start; i < end; ++i) {
            if (!this.states_[i].inactive) {
                let state = this.states_[i];
                if(state.joinDataSet != undefined && state.joinDataSet != null) {
                    if(joinDataSetIds.indexOf(state.joinDataSet.datasetId) <0){
                        joinDataSetIds.push(state.joinDataSet.datasetId);
                        sparkScript +=state.joinDataSet.joinDataFrameVarScript+"\n";
                    }

                    sparkScript += state.joinDataSet.joinScript;
                }
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
            return this.$http.get<TableSchema>(this.RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table).toPromise();
        } else {
            return super.getTableSchema(schema, table, datasourceId);
        }
    }

    /**
     * Fetches the Ternjs definitions for this query engine.
     */
    getTernjsDefinitions(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.$http.get(this.RestUrlService.UI_BASE_URL + "/spark-functions").toPromise()
                .then(function (response: any) {
                    resolve(response);
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

        //add in the datasets
        if (this.datasets !== null) {
            body["catalogDatasets"] = this.datasets;
        }

        if(this.catalogDataSources_ != null) {
            body['catalogDataSources'] = this.catalogDataSources_;
        }

        // Send the request
        let transformId: string;



        return this.$http.post<TransformResponse>(this.apiUrl + "/transform", JSON.stringify(body), {
            headers: {"Content-Type": "application/json"},
            responseType: "json"
        })
        // Send save request
            .mergeMap(response => {
                transformId = response.table;
                return this.$http.post<SaveResponse>(this.apiUrl + "/transform/" + transformId + "/save", JSON.stringify(request), {
                    headers: {"Content-Type": "application/json"},
                    responseType: "json"
                });
            })
            // Wait for save to complete
            .expand(response => {
                if (response.status === SaveResponseStatus.PENDING ) {
                    return Observable.interval(1000)
                        .pipe(
                            take(1),
                            mergeMap(() => this.$http.get<SaveResponse>(this.apiUrl + "/transform/" + transformId + "/save/" + response.id, {responseType: "json"})),
                            catchError((error: SaveResponse) => {
                                if (error.id == null) {
                                    error.id = response.id;
                                }
                                return Observable.throw(error);
                            })
                        );
                } else if (response.status === SaveResponseStatus.SUCCESS) {
                    return Observable.empty();
                } else {
                    throw response;
                }
            })
            // Map result to SaveResponse
            .map(save => {
                if (save.location != null && save.location.startsWith("./")) {
                    save.location = this.apiUrl + "/transform/" + transformId + "/save/" + save.id + save.location.substr(1);
                }
                return save;
            });
    }

    /**
     * Searches for table names matching the specified query.
     *
     * @param query - search query
     * @param datasourceId - datasource to search
     * @returns the list of table references
     */
    searchTableNames(query: string, datasourceId: string): TableReference[] | Promise<TableReference[]> {
        if (datasourceId === SparkConstants.HIVE_DATASOURCE) {
            const tables = this.HiveService.queryTablesSearch(query);
            if (tables.then) {
                return new Promise((resolve, reject) => tables.then(resolve, reject));
            } else {
                return tables as any;
            }
        } else {
            return super.searchTableNames(query, datasourceId);
        }
    }

    decodeError(msg: string): string {
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
        if (this.datasets !== null) {
            body["catalogDatasets"] = this.datasets;
        }

        if(this.catalogDataSources_ != null) {
            body['catalogDataSources'] = this.catalogDataSources_;
        }

        // Create the response handlers
        let self = this;
        let deferred = new Subject();

        let successCallback = function (response: TransformResponse) {
            let state = self.states_[index];
            self.resetStateChange();
            // Check status
            if (response.status === "PENDING") {

                deferred.next(response.progress);

                setTimeout(function () {
                    self.$http.get<TransformResponse>(self.apiUrl + "/transform/" + response.table, {
                        headers: {"Content-Type": "application/json"},
                        responseType: "json"
                    }).toPromise().then(successCallback, errorCallback);
                }, 250, false);
                return;
            }
            if (response.status !== "SUCCESS") {
                deferred.error("Unexpected server status.");
                return;
            }

            // Verify column names
            let invalid = _.find(response.results.columns, function (column: any) {
                return (column.hiveColumnLabel.match(/[.`]/) !== null);  // Escaping backticks not supported until Spark 2.0
            });
            let reserved = _.find(response.results.columns, function (column: any) {
                return SparkConstants.RESERVED_COLUMN_NAMES.indexOf(column.hiveColumnLabel) >=0;
            });

            if (typeof invalid != "undefined") {
                state.rows = [];
                state.columns = [];
                deferred.error("Column name '" + invalid.hiveColumnLabel + "' is not supported. Please choose a different name.");
            } else if (typeof reserved != "undefined") {
                state.rows = [];
                state.columns = [];
                deferred.error("Column name '" + reserved.hiveColumnLabel + "' is reserved. Please choose a different name.");
            } else {
                // Update state
                state.profile = response.profile;
                state.rows = response.results.rows;
                state.table = response.table;
                state.validationResults = response.results.validationResults;
                state.actualCols = response.actualCols;
                state.actualRows = response.actualRows;
                state.columns = response.results.columns;
                self.updateFieldPolicies(state);

                // Indicate observable is complete
                deferred.complete();
            }
        };
        let errorCallback = function (response: HttpErrorResponse) {
            // Update state
            let state = self.states_[index];
            state.columns = [];
            state.rows = [];
            self.resetStateChange();

            // Respond with error message
            let message;

            if (response.error !== undefined && response.error.message != null) {
                message = self.decodeError(response.error.message.toString());
                message = (message.length <= 1024) ? message : message.substr(0, 1021) + "...";
            } else {
                message = "An unknown error occurred.";
            }

            deferred.error(message);
        };



        // Send the request
        self.httpBackendClient.post<TransformResponse>(this.apiUrl + "/transform", JSON.stringify(body), {
            headers: {"Content-Type": "application/json"},
            responseType: "json"
        }).toPromise().then(successCallback, errorCallback);
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
        return new SparkQueryParser(this.VisualQueryService).toScript(source, this.datasources_, this.catalogDataSources_);
    }

    /**
     * Updates the field policies of the specified state to match the column order.
     * @param {ScriptState<string>} state
     */
    private updateFieldPolicies(state: ScriptState<string>) {
        const self = this;
        if (state.fieldPolicies != null && state.fieldPolicies.length > 0) {
            const policyMap = {};
            state.fieldPolicies.forEach(policy => {
                policyMap[policy.name] = policy;
            });

            state.fieldPolicies = state.columns.map(column => {
                const name = (typeof column.displayName != "undefined") ? self.getValidHiveColumnName(column.displayName) : column.hiveColumnLabel;
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


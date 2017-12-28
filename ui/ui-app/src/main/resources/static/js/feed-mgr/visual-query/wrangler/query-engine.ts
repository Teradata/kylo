import {Observable} from "rxjs/Observable";

import {SaveRequest, SaveResponse} from "./api/rest-model";
import {WranglerEngine} from "./api/wrangler-engine";
import {ColumnController} from "./column-controller";
import {ColumnDelegate} from "./column-delegate";
import {DatasourcesServiceStatic, ProfileOutputRow, QueryResultColumn, SchemaField, SqlDialect, TableSchema, UserDatasource} from "./index";
import {ScriptState} from "./model/script-state";
import {TransformValidationResult} from "./model/transform-validation-result";
import {QueryEngineConstants} from "./query-engine-constants";

/**
 * Provides the ability to query and transform data.
 */
export abstract class QueryEngine<T> implements WranglerEngine {

    /**
     * List of required data source ids.
     */
    protected datasources_: UserDatasource[];

    /**
     * Transformation function definitions.
     */
    protected defs_: any = {};

    /**
     * Number of rows to select in the initial query.
     */
    protected limit_: number = 1000;

    /**
     * Indicates if limiting should be done before sampling.
     */
    protected limitBeforeSample_: boolean = false;

    /**
     * List of states that can be redone.
     */
    protected redo_: ScriptState<T>[] = [];

    /**
     * Fraction of rows to include when sampling.
     */
    protected sample_: number = 1.0;

    /**
     * The source SQL for transformations, escaped for Scala.
     */
    protected source_: string;

    /**
     * List of states.
     */
    protected states_: ScriptState<T>[] = [this.newState()];

    /**
     * Construct a {@code QueryEngine}.
     */
    constructor(protected $mdDialog: angular.material.IDialogService, protected DatasourcesService: DatasourcesServiceStatic.DatasourcesService, protected uiGridConstants: any) {
    }

    /**
     * Indicates if both limit and sample can be applied at the same time.
     */
    abstract get allowLimitWithSample(): boolean;

    /**
     * Indicates if multiple data sources are allowed in the same query.
     */
    get allowMultipleDataSources(): boolean {
        return false;
    }

    /**
     * Gets the sample formulas.
     */
    abstract get sampleFormulas(): { name: string, formula: string }[];

    /**
     * Gets the SQL dialect used by this engine.
     */
    abstract get sqlDialect(): SqlDialect;

    /**
     * Indicates that the native data type should be used instead of the Hive data type.
     */
    get useNativeDataType(): boolean {
        return true;
    }

    /**
     * Indicates if a previously undone transformation can be redone.
     *
     * @returns {@code true} if the transformation can be restored
     */
    canRedo(): boolean {
        return (this.redo_.length !== 0);
    }

    /**
     * Indicates if the current transformation can be undone.
     *
     * @returns {@code true} if the current transformation can be undone
     */
    canUndo(): boolean {
        return (this.states_.length > 1);
    }

    /**
     * Creates a column delegate of the specified data type.
     */
    createColumnDelegate(dataType: string, controller: ColumnController): ColumnDelegate {
        return new ColumnDelegate(dataType, controller, this.$mdDialog, this.uiGridConstants);
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
                defs[column.field] = QueryEngineConstants.TERNJS_COLUMN_TYPE;
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
                    if (column.field === fieldName) {
                        return column.hiveColumnLabel;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the field name of the specified column.
     */
    abstract getColumnName(column: QueryResultColumn): string;

    /**
     * Gets the columns after applying the current transformation.
     *
     * @returns the columns or {@code null} if the transformation has not been applied
     */
    getColumns(): QueryResultColumn[] | null {
        return this.getState().columns;
    }

    /**
     * Gets the Spark script without sampling for the feed.
     *
     * @returns the Spark script
     */
    getFeedScript(): string {
        return this.getScript(null, null, false);
    }

    /**
     * Gets the field policies for the current transformation.
     */
    getFieldPolicies(): any[] | null {
        return this.getState().fieldPolicies;
    }

    /**
     * Gets the schema fields for the the current transformation.
     *
     * @returns the schema fields or {@code null} if the transformation has not been applied
     */
    getFields(): SchemaField[] | null {
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
            } else {
                dataType = col.dataType;
            }
            const colDef = {name: col.hiveColumnLabel, description: col.comment, dataType: dataType, primaryKey: false, nullable: false, sampleValues: []} as SchemaField;
            if (col.precisionScale) {
                colDef.precisionScale = col.precisionScale;
            } else if (dataType === 'decimal') {
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
     * Gets the function definitions being used.
     *
     * @return the function definitions
     */
    getFunctionDefs(): any {
        return this.defs_;
    }

    /**
     * Gets the list of contexts for the current transformations.
     *
     * @return the function history
     */
    getHistory(): any[] {
        return this.states_.slice(1).map(function (state) {
            return state.context;
        });
    }

    /**
     * Returns the data sources that are supported natively by this engine.
     */
    getNativeDataSources(): Promise<UserDatasource[]> {
        return new Promise(resolve => resolve([]));
    }

    /**
     * Gets the column statistics for the current transformation.
     */
    getProfile(): ProfileOutputRow[] {
        let profile: ProfileOutputRow[] = [];
        const state = this.getState();

        // Add total counts
        let hasInvalidCount = false;
        let hasTotalCount = false;
        let hasValidCount = false;

        if (state.profile) {
            state.profile.forEach(row => {
                if (row.columnName === "(ALL)") {
                    hasInvalidCount = hasInvalidCount || (row.metricType === "INVALID_COUNT");
                    hasTotalCount = hasTotalCount || (row.metricType === "TOTAL_COUNT");
                    hasValidCount = hasValidCount || (row.metricType === "VALID_COUNT");
                }
            });
        }

        if (!hasInvalidCount) {
            profile.push({columnName: "(ALL)", metricType: "INVALID_COUNT", metricValue: 0});
        }
        if (!hasTotalCount) {
            profile.push({columnName: "(ALL)", metricType: "TOTAL_COUNT", metricValue: (state.rows) ? state.rows.length : 0});
        }
        if (!hasValidCount) {
            profile.push({columnName: "(ALL)", metricType: "VALID_COUNT", metricValue: 0});
        }

        // Add state profile
        if (state.profile) {
            profile = profile.concat(state.profile);
        }

        return profile;
    }

    /**
     * Gets the rows after applying the current transformation.
     *
     * @returns the rows or {@code null} if the transformation has not been applied
     */
    getRows(): any[][] | null {
        return this.getState().rows;
    }

    /**
     * Gets the Spark script.
     *
     * @param start - the index of the first transformation
     * @param end - the index of the last transformation
     * @param sample - {@code false} to disable sampling
     * @returns the Spark script
     */
    abstract getScript(start?: number, end?: number, sample?: boolean): string;

    /**
     * Lists the names of the supported data source types.
     *
     * Used in error messages to list the supported data source types.
     */
    getSupportedDataSourceNames(): string[] {
        return [];
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
        const self = this;
        return new Promise((resolve, reject) => self.DatasourcesService.getTableSchema(datasourceId, table, schema).then(resolve, reject));
    }

    /**
     * Fetches the Ternjs definitions for this query engine.
     */
    abstract getTernjsDefinitions(): Promise<any>;

    /**
     * Gets the validation results from the current transformation.
     */
    getValidationResults(): TransformValidationResult[][] {
        return this.getState().validationResults;
    }

    /**
     * The number of rows to select in the initial query.
     *
     * @param value - the new value
     * @returns the number of rows
     */
    limit(value?: number): number {
        if (typeof value !== "undefined") {
            this.clearTableState();
            this.limit_ = value;
        }
        return this.limit_;
    }

    /**
     * Removes the last transformation from the stack. This action cannot be undone.
     *
     * @see #undo()
     */
    pop(): void {
        if (this.states_.length > 1) {
            this.states_.pop();
        }
    }

    /**
     * Adds a transformation expression to the stack.
     *
     * @param tree - the abstract syntax tree for the expression
     * @param context - the UI context for the transformation
     */
    push(tree: acorn.Node, context: any): void {
        // Add new state
        let state = this.newState();
        state.context = context;
        state.fieldPolicies = this.getState().fieldPolicies;
        state.script = this.parseAcornTree(tree);
        this.states_.push(state);

        // Clear redo states
        this.redo_ = [];
    }

    /**
     * Restores the last transformation that was undone.
     *
     * @see #undo()
     * @returns the UI context for the transformation
     * @throws {Error} if there are no transformations to redo
     */
    redo(): any {
        if (this.redo_.length > 0) {
            let state = this.redo_.pop();
            this.states_.push(state);
            return state.context;
        } else {
            throw new Error("No states to redo");
        }
    }

    /**
     * The fraction of rows to include when sampling.
     *
     * @param value - the new value
     * @returns the fraction of rows
     */
    sample(value?: number): number {
        if (typeof value !== "undefined") {
            this.clearTableState();
            this.sample_ = value;
        }
        return this.sample_;
    }

    /**
     * Returns an object for recreating this script.
     *
     * @return the saved state
     */
    save(): any[] {
        return this.states_.slice(1).map(function (state) {
            return {context: state.context, script: state.script};
        });
    }

    /**
     * Saves the results to the specified destination.
     *
     * @param request - save target
     * @returns an observable tracking the save status
     */
    abstract saveResults(request: SaveRequest): Observable<SaveResponse>;

    /**
     * Searches for table names matching the specified query.
     *
     * @param query - search query
     * @param datasourceId - datasource to search
     * @returns the list of table references
     */
    searchTableNames(query: string, datasourceId: string): DatasourcesServiceStatic.TableReference[] | Promise<DatasourcesServiceStatic.TableReference[]> {
        const tables = this.DatasourcesService.listTables(datasourceId, query);
        return new Promise((resolve, reject) => tables.then(resolve, reject));
    }

    /**
     * Sets the field policies to use for the current transformation.
     */
    setFieldPolicies(policies: any[]): void {
        this.getState().fieldPolicies = policies;
    }

    /**
     * Sets the function definitions to use.
     *
     * @param defs the function definitions
     */
    setFunctionDefs(defs: any): void {
        this.defs_ = defs;
    }

    /**
     * Loads the specified state for using an existing transformation.
     */
    setState(state: any[]): void {
        this.redo_ = [];
        state.forEach((src) => {
            const state = this.newState();
            state.context = src.context;
            state.script = src.script;
            this.states_.push(state);
        });
    }

    /**
     * Sets the query and datasources.
     */
    setQuery(query: string | object, datasources: UserDatasource[] = []): void {
        this.datasources_ = (datasources.length > 0) ? datasources : null;
        this.redo_ = [];
        this.source_ = this.parseQuery(query);
        this.states_ = [this.newState()];
    }

    /**
     * Indicates if the limiting should be done before sampling.
     *
     * @param value - the new value
     * @returns {@code true} if limiting should be done first, or {@code false} if sampling should be done first
     */
    shouldLimitBeforeSample(value?: boolean): boolean {
        if (typeof value !== "undefined") {
            this.clearTableState();
            this.limitBeforeSample_ = value;
        }
        return this.limitBeforeSample_;
    }

    /**
     * Removes transformations from the current script.
     */
    splice(start: number, deleteCount: number): void {
        // Delete states
        this.states_.splice(start, deleteCount);
        this.clearTableState(start);

        // Clear redo states
        this.redo_ = [];
    }

    /**
     * Indicates if this engine supports the specified data source.
     *
     * @param dataSource - the data source to check
     * @returns true if the data source is support, or false otherwise
     */
    supportsDataSource(dataSource: UserDatasource): boolean {
        return true;
    }

    /**
     * Runs the current script on the server.
     *
     * @return an observable for the response progress
     */
    abstract transform(): Observable<any>;

    /**
     * Reverts to the previous transformation. The current transformation is remembered and may be restored.
     *
     * @see #pop()
     * @see #redo()
     * @returns the UI context for the transformation
     * @throws {Error} if there are no transformations to undo
     */
    undo(): any {
        if (this.states_.length > 1) {
            let state = this.states_.pop();
            this.redo_.push(state);
            return state.context;
        } else {
            throw new Error("No states to undo");
        }
    }

    /**
     * Parses the specified tree into a script for the current state.
     */
    protected abstract parseAcornTree(tree: any): T;

    /**
     * Parses the specified source into a script for the initial state.
     */
    protected abstract parseQuery(source: any): string;

    /**
     * Clears table data from all states. This doesn't affect column information that doesn't change with the limit or sample
     * properties.
     */
    private clearTableState(index: number = 0): void {
        for (let r = index; r < this.redo_.length; ++r) {
            this.redo_[r].rows = null;
            this.redo_[r].table = null;
        }
        for (let s = index; s < this.states_.length; ++s) {
            this.states_[s].rows = null;
            this.states_[s].table = null;
        }
    }

    /**
     * Gets the current state.
     */
    protected getState(): ScriptState<T> {
        return this.states_.length > 0 ? this.states_[this.states_.length - 1] : {} as ScriptState<T>;
    }

    /**
     * Creates a new script state.
     *
     * @returns a new script state
     */
    private newState(): ScriptState<T> {
        return {columns: null, context: {}, fieldPolicies: null, profile: null, rows: null, script: null, table: null, validationResults: null};
    }
}

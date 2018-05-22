import * as angular from "angular";

import {ColumnDelegate as IColumnDelegate, DataType as DT} from "./api/column";
import {DialogService} from "./api/services/dialog.service";
import {ColumnController} from "./column-controller";
import * as $ from "jquery";
import * as _ from "underscore";

/**
 * Categories for data types.
 */
export enum DataCategory {
    ARRAY_DOUBLE,
    ARRAY,
    BINARY,
    BOOLEAN,
    DATETIME,
    MAP,
    NUMERIC,
    STRING,
    STRUCT,
    UNION,
    OTHER
}

/**
 * Hive data types.
 *
 * @readonly
 * @enum {string}
 */
const DataType = {
    // Numeric types
    TINYINT: 'tinyint',
    SMALLINT: 'smallint',
    INT: 'int',
    BIGINT: 'bigint',
    FLOAT: 'float',
    DOUBLE: 'double',
    DECIMAL: 'decimal',

    // Date/time types
    TIMESTAMP: 'timestamp',
    DATE: 'date',

    // String types
    STRING: 'string',
    VARCHAR: 'varchar',
    CHAR: 'char',

    // Misc types
    BOOLEAN: 'boolean',
    BINARY: 'binary',

    // Complex types
    ARRAY_DOUBLE: 'array<double>',
    ARRAY: 'array',
    MAP: 'map',
    STRUCT: 'struct',
    UNION: 'uniontype'
};

/**
 * Represents a sequence of query operations
 */
export class ChainedOperation {
    step: number = 1;
    totalSteps: number;

    constructor(totalSteps: number = 1) {
        this.totalSteps = totalSteps;
    }

    nextStep(): void {
        this.step += 1;
    }

    /**
     * Fractional of overall progress complete
     * @param {number} stepProgress the progress between 0 and 100
     * @returns {number} overall progress between 0 and 100
     */
    fracComplete(stepProgress: number): number {
        let min: number = ((this.step - 1) / this.totalSteps);
        let max: number = this.step / this.totalSteps;
        return (Math.ceil((min * 100) + (max - min) * stepProgress));
    }

    isLastStep(): boolean {
        return this.step == this.totalSteps;
    }
}

/**
 * Handles operations on columns.
 */
export class ColumnDelegate implements IColumnDelegate {

    /**
     * The category for the data in the column.
     */
    dataCategory: DataCategory;

    /**
     * List of column filters.
     */
    filters: object[];

    /**
     * List of column transformations.
     */
    transforms: object[];

    /**
     * Constructs a column delegate.
     */
    constructor(public dataType: string, public controller: ColumnController, protected $mdDialog: angular.material.IDialogService, protected uiGridConstants: any, protected dialog?: DialogService) {
        this.dataCategory = this.fromDataType(dataType);
        this.filters = this.getFilters(this.dataCategory);
        this.transforms = this.getTransforms(this.dataCategory);
    }

    /**
     * Casts this column to the specified type.
     */
    castTo(dataType: DT): void {
        // not supported
    }

    escapeRegExp(text: string): string {
        return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\\\$&');
    }

    stripValueContaining(value: string, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const regex = this.escapeRegExp(value);
        const formula = this.toFormula("regexp_replace(" + fieldName + ", \"" + regex + "\", \"\").as(\"" + fieldName + "\")", column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "content_cut", name: "Strip " + this.getColumnDisplayName(column) + " containing " + value});
    }

    clearRowsEquals(value: string, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const formula = this.toFormula("when(equal(" + fieldName + ", '" + StringUtils.singleQuote(value) + "'),null).otherwise(" + fieldName + ").as(\"" + fieldName + "\")", column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle", name: "Clear " + this.getColumnDisplayName(column) + " equals " + value});
    }

    /**
     * Filters for rows where the specified column is not null.
     */
    deleteNullRows(column: any) {
        const formula = "filter(not(isnull(" + this.getColumnFieldName(column) + ")))";
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle_containing", name: "Delete " + this.getColumnDisplayName(column) + " if null"});
    }

    /**
     * Filters for rows where the specified column does not contain the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsContaining(value: string, column: any) {
        const formula = "filter(not(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.singleQuote(value) + "')))";
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Delete " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is not the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsEqualTo(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " != '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "≠", name: "Delete " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is less than or equal to the specified value.
     *
     * @param value - the maximum value (inclusive)
     * @param column - the column
     */
    deleteRowsGreaterThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " <= '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "≯", name: "Delete " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is greater than or equal to the specified value.
     *
     * @param value - the minimum value (inclusive)
     * @param column - the column
     */
    deleteRowsLessThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " >= '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "≮", name: "Delete " + this.getColumnDisplayName(column) + " less than " + value});
    }

    /**
     * Filters for rows where the specified column is null.
     */
    findNullRows(column: any) {
        const formula = "filter(isnull(" + this.getColumnFieldName(column) + "))";
        this.controller.addFunction(formula, {formula: formula, icon: "=", name: "Find where " + this.getColumnDisplayName(column) + " is null"});
    }

    /**
     * Filters for rows where the specified column contains the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsContaining(value: string, column: any) {
        const formula = "filter(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.singleQuote(value) + "'))";
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Find " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsEqualTo(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " == '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "=", name: "Find " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is greater than the specified value.
     *
     * @param value - the minimum value (exclusive)
     * @param column - the column
     */
    findRowsGreaterThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " > '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "keyboard_arrow_right", name: "Find " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is less than the specified value.
     *
     * @param value - the maximum value (exclusive)
     * @param column - the column
     */
    findRowsLessThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " < '" + StringUtils.singleQuote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "keyboard_arrow_left", name: "Find " + this.getColumnDisplayName(column) + " less than " + value});
    }

    /**
     * Hides the specified column.
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    hideColumn(column: any, grid: any) {
        column.visible = false;

        const formula = "drop(\"" + StringUtils.singleQuote(column.headerTooltip) + "\")";
        this.controller.pushFormula(formula, {formula: formula, icon: "remove_circle", name: "Hide " + this.getColumnDisplayName(column)});
        this.controller.fieldPolicies = this.controller.fieldPolicies.filter((value, index) => index == column.index);
    }

    /**
     * Display the analyze column view
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    showAnalyzeColumn(column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        this.controller.showAnalyzeColumn(fieldName);
    }

    /**
     * Clone the specified column.
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    cloneColumn(column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const script = "clone(" + fieldName + ")";
        const formula = this.toAppendColumnFormula(script, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: 'content_copy', name: 'Clone ' + this.getColumnDisplayName(column)});
    }

    /**
     * Imputes the values using mean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    imputeMeanColumn(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);
        const script = "when(or(isnull(" + fieldName + "),isnan(" + fieldName + ")),mean(" + fieldName + ").over(orderBy(1))).otherwise(" + fieldName + ").as(\"" + fieldName + "\")";
        const formula = self.toFormula(script, column, grid);
        self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: 'Impute mean ' + self.getColumnDisplayName(column)});
    }

    /**
     * Crosstab against another column
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    crosstabColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        let cols = self.controller.engine.getCols();

        self.$mdDialog.show({
            clickOutsideToClose: true,
            controller: class {
                columns : any[] = cols;
                crossColumn : string = "";
                static readonly $inject = ["$mdDialog"];
                constructor(private $mdDialog: angular.material.IDialogService) {
                }
                valid() : boolean {
                    return (this.crossColumn != "");
                }
                cancel() {
                    this.$mdDialog.hide();
                }
                apply() {
                    this.$mdDialog.hide();
                    let crossColumnTemp = (this.crossColumn == fieldName ? this.crossColumn+"_0" : this.crossColumn);
                    let clean2 = self.createCleanFieldFormula(this.crossColumn, crossColumnTemp);
                    const  cleanFormula = `select(${fieldName}, ${clean2})`;
                    let chainedOp: ChainedOperation = new ChainedOperation(2);
                    let crossColumnName = this.crossColumn;
                    self.controller.setChainedQuery(chainedOp);
                    self.controller.pushFormula(cleanFormula, {formula: cleanFormula, icon: 'spellcheck', name: `Clean ${fieldName} and ${this.crossColumn}`}, true, false).then(function() {
                        chainedOp.nextStep();
                        const formula = `crosstab("${fieldName}","${crossColumnTemp}")`
                        self.controller.addFunction(formula, {formula: formula, icon: 'poll', name: `Crosstab ${fieldName} and ${crossColumnName}`});
                    });
                }
            },
            controllerAs: "dialog",
            parent: angular.element("body"),
            template: `
                  <md-dialog arial-label="error executing the query" style="max-width: 640px;">
                    <md-dialog-content class="md-dialog-content" role="document" tabIndex="-1">
                      <h2 class="md-title">Select crosstab field:</h2>

                      <md-input-container>
                        <label>Cross column:</label>
                        <md-select ng-model="dialog.crossColumn" >
                            <md-option ng-repeat="x in dialog.columns" value="{{x.field}}">
                                {{x.field}}
                            </md-option>
                        </md-select>
                      </md-input-container>
                    </md-dialog-content>
                    <md-dialog-actions>
                      <md-button ng-click="dialog.cancel()" class="md-cancel-button" md-autofocus="false">Cancel</md-button>
                      <md-button ng-click="dialog.apply()" ng-disabled="!dialog.valid()" class="md-primary md-confirm-button" md-autofocus="true">Ok</md-button>
                    </md-dialog-actions>
                  </md-dialog>
                `
        });
    }

    /**
     * Generates a script to use a temp column with the desired result and replace the existing column and ordering for
     * which the temp column was derived. This is used by some of the machine
     * learning functions that don't return column types
     * @returns {string}
     */
    generateRenameScript(fieldName: string, tempField: string, grid: any): string {
        // Build select script to drop temp column we generated
        var self = this;
        let cols: string[] = [];
        angular.forEach(grid.columns, col => {
            let colName: string = self.getColumnFieldName(col);
            if (colName != tempField) {
                colName = (colName == fieldName ? `${tempField}.as("${fieldName}")` : colName);
                cols.push(colName);
            }
        });
        let selectCols = cols.join();
        let renameScript = `select(${selectCols})`;
        return renameScript;
    }

    /**
     * Generates a script to move the column B directly to the right of column A
     * @returns {string}
     */
    generateMoveScript(fieldNameA: string, fieldNameB: string | string[], columnSource: any, keepFieldNameA: boolean = true): string {
        var self = this;
        let cols: string[] = [];
        let sourceColumns = (columnSource.columns ? columnSource.columns : columnSource);
        angular.forEach(sourceColumns, col => {
            let colName: string = self.getColumnFieldName(col);
            if (colName == fieldNameA) {
                if (keepFieldNameA) cols.push(colName);
                if(_.isArray(fieldNameB)){
                    cols = cols.concat(fieldNameB);
                }
                else {
                    cols.push(fieldNameB);
                }
            } else if ((_.isArray(fieldNameB ) && !_.contains(fieldNameB,colName)) || (_.isString(fieldNameB) && colName != fieldNameB)) {
                cols.push(colName);
            }
        });
        let selectCols = cols.join();
        return `select(${selectCols})`;
    }

    /**
     * Attempt to determine number of elements in array
     * @param {string} text
     * @returns {string}
     */
    arrayItems(text: string): number {
        return (text && text.length > 0 ? text.split(",").length : 1);
    }

    /**
     * Extract array items into columns
     * @param column
     * @param grid
     */
    extractArrayItems(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        let count = 0;

        // Sample rows determine how many array elements
        if (grid.rows != null && grid.rows.length > 0) {
            let idx: number = 0;
            angular.forEach(grid.columns, (col, key) => {
                if (col.name == fieldName) idx = key;
            })
            angular.forEach(grid.rows, row => {
                count = (row[idx] != null && row[idx].length > count ? row[idx].length : count)
            });
        }
        var columns = []
        for (let i = 0; i < count; i++) {
            let newFieldName = fieldName + "_" + i;
            columns.push(`getItem(${fieldName}, ${i}).as("${newFieldName}")`);
        }
        var formula = self.generateMoveScript(fieldName, columns, grid, false );
        self.controller.pushFormula(formula, {formula: formula, icon: "functions", name: "Extract array"}, true, true);
    }


    /**
     * Adds string labels to indexes
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    indexColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        const newFieldName = fieldName + "_indexed";
        const formula = `StringIndexer().setInputCol("${fieldName}").setOutputCol("${newFieldName}").run(select(${fieldName}))`;
        const moveFormula = self.generateMoveScript(fieldName, [newFieldName], grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);

        self.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Index ' + self.getColumnDisplayName(column)}, true, false)
            .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(moveFormula, {formula: formula, icon: 'functions', name: 'Move new column next to ' + fieldName});
            })
    }

    /**
     * Vectorize a numeric column as a double array
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    vectorizeColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        const tempField = self.createTempField();
        const formula = `vectorAssembler(["${fieldName}"], "${tempField}")`;
        let renameScript = self.generateRenameScript(fieldName, tempField, grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);
        self.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Vectorize ' + self.getColumnDisplayName(column)}, true, false)
            .then(function result() {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, {formula: formula, icon: 'functions', name: 'Remap temp vector column to ' + fieldName});
            })
    }

    /**
     * Rescale the vector column
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     * @param boolean use mean
     * @param boolean use stdDev (normally default)
     */
    rescaleColumn(self: any, column: any, grid: any, mean: boolean, stdDev: boolean) {

        const fieldName = self.getColumnFieldName(column);
        const tempField = self.createTempField();
        const formula = `StandardScaler().setInputCol("${fieldName}").setOutputCol("${tempField}").setWithMean(${mean}).setWithStd(${stdDev}).run(select(${fieldName}))`;
        let renameScript = self.generateRenameScript(fieldName, tempField, grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);
        self.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Std Dev. rescale ' + self.getColumnDisplayName(column)}, true, false)
            .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, {formula: formula, icon: 'functions', name: 'Remap temp rescaled column to ' + fieldName});
            })
    }

    /**
     * Rescale the vector column between min/max
     * @param self
     * @param column
     * @param grid
     */
    rescaleMinMax(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        const tempField = self.createTempField();
        const formula = `MinMaxScaler().setInputCol("${fieldName}").setOutputCol("${tempField}").run(select(${fieldName}))`;
        let renameScript = self.generateRenameScript(fieldName, tempField, grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);

        self.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'MinMax rescale ' + self.getColumnDisplayName(column)}, true, false)
            .then(function () {
                chainedOp.nextStep();
                self.controller.addFunction(renameScript, {formula: formula, icon: 'functions', name: 'Remap temp rescaled column to ' + fieldName});
            })

    }

    /**
     * Rescale the vector column using the standard deviation
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleStdDevColumn(self: any, column: any, grid: any) {
        self.rescaleColumn(self, column, grid, false, true);
    }

    /**
     * Rescale the vector column using the mean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleMeanColumn(self: any, column: any, grid: any) {
        self.rescaleColumn(self, column, grid, true, false);
    }

    /**
     * Rescale using mean and stdDev
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleBothMethodsColumn(self: any, column: any, grid: any) {
        self.rescaleColumn(self, column, grid, true, true);
    }

    toColumnArray(columns: any[], ommitColumn ?: string): string[] {
        const self = this;
        let cols: string[] = [];
        angular.forEach(columns, column => {
            if (!ommitColumn || (ommitColumn && ommitColumn != column.name)) {
                cols.push(self.getColumnFieldName(column));
            }
        });
        return cols;
    }

    imputeMissingColumn(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);
        self.dialog.openImputeMissing({
            message: 'Provide windowing options for sourcing fill-values:',
            fields: self.toColumnArray(grid.columns, fieldName)
        }).subscribe(function (response: any) {

            let script = `coalesce(${fieldName}, last(${fieldName}, true).over(partitionBy(${response.groupBy}).orderBy(${response.orderBy}))).as("${fieldName}")`;
            const formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, {formula: formula, icon: "functions", name: `Impute missing values ${fieldName}`});
        });
    }

    /**
     * Parse a struct field into its top-level fields
     * @param column
     * @returns {string[]} list of fields
     */
    structToFields(column: any) : string[] {

        // Strip initial struct wrapper then remove any additional structs
        let fields : string = column.dataType;
        fields = fields.substr(7, fields.length-2);
        fields = fields.replace(/struct\<(.*?)\>/g, "ignore");
        let fieldArray : string[] = fields.split(",");
        return fieldArray.map( (v:string) => {
            return v.split(":")[0].toLowerCase();
        });
    }

    /**
     * Flattens a struct column into multiple fields (one-level)
     * @param self
     * @param column
     * @param grid
     */
    flattenStructColumn(self: any, column: any, grid:any) {

        const fieldName = self.getColumnFieldName(column);

        // Check for potential name conflicts
        let structFields = self.structToFields(column);
        let existingCols = self.toColumnArray(grid.columns);
        let existingColsLower = existingCols.map((field:string)=> { return field.toLowerCase(); });

        let conflicts = structFields.filter( (n:string) =>  {
            return existingColsLower.indexOf(n) !== -1;
        });

        let formula : string;
        if (conflicts.length == 0) {
            formula = self.toFormula(`col("${fieldName}.*")`, column, grid);
        } else {
            let fieldParts = structFields.map ( (field:string)=> {
                return (conflicts.indexOf(field) !== -1 ? `getField(${fieldName},"${field}").as("${fieldName}_${field}")` : `getField(${fieldName},"${field}").as("${field}")`);
            });

            // Insert new fields into the field list
            let idxOfCurrentColumn = existingCols.indexOf(fieldName);
            let formulaFields : string[] = []
            if (idxOfCurrentColumn == 0) {
                formulaFields.push(fieldParts);
                formulaFields.push(existingCols.slice(idxOfCurrentColumn+1));
            } else if (idxOfCurrentColumn == existingCols.length - 1) {
                formulaFields.push(existingCols.slice(0, idxOfCurrentColumn));
                formulaFields.push(fieldParts);
            } else {
                formulaFields.push(existingCols.slice(0, idxOfCurrentColumn));
                formulaFields.push(fieldParts);
                formulaFields.push(existingCols.slice(idxOfCurrentColumn+1));
            }
            formula = `select(${formulaFields.join(",")})`;
        }
        self.controller.addFunction(formula, {
            formula: formula, icon: "functions",
            name: "Flatten " + fieldName
        });

    }

   /**
     * Generates a temporary fieldname
     * @returns {string} the fieldName
     */
    createTempField(): string {
        return "c_" + (new Date()).getTime();
    }

    /**
     * Creates a formula for cleaning values as future fieldnames
     * @returns {string} a formula for cleaning row values as fieldnames
     */
    createCleanFieldFormula(fieldName: string, tempField: string): string {
        return `when(startsWith(regexp_replace(substring(${fieldName},0,1),"[0-9]","***"),"***"),concat("c_",lower(regexp_replace(${fieldName},"[^a-zA-Z0-9_]+","_")))).otherwise(lower(regexp_replace(${fieldName},"[^a-zA-Z0-9_]+","_"))).as("${tempField}")`;
    }

    /**
     * Extract numerical values from string
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractNumeric(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);
        let script = `regexp_replace(${fieldName}, "[^0-9\-\\\\.]+","").as('${fieldName}')`;

        const formula = self.toFormula(script, column, grid);
        self.controller.addFunction(formula, {
            formula: formula, icon: "filter_2",
            name: "Extract numeric from " + self.getColumnDisplayName(column)
        });

    }

    /**
     * Negate a boolean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    negateBoolean(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);
        let script = `not(${fieldName}).as("${fieldName}")`;

        const formula = self.toFormula(script, column, grid);
        self.controller.addFunction(formula, {
            formula: formula, icon: "exposure",
            name: "Negate boolean from " + self.getColumnDisplayName(column)
        });

    }

    /**
     * One hot encode categorical values
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    oneHotEncodeColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);

        // Chain three calls: 1) clean values as valid column names 2) execute pivot 3) replace null with empty (due to spark2 pivot behavior)
        const tempField = self.createTempField();
        const cleanFormula = self.createCleanFieldFormula(fieldName, tempField);

        // Generate group by and pivot formula from all the columns
        let cols: string[] = self.toColumnArray(grid.columns);

        let colString: string = cols.join();
        const formula = `groupBy(${colString}).pivot("${tempField}").agg(when(count(${tempField})>0,1).otherwise(0))`;

        let chainedOp: ChainedOperation = new ChainedOperation(3);
        self.controller.setChainedQuery(chainedOp);

        self.controller.pushFormula(cleanFormula, {formula: cleanFormula, icon: 'functions', name: 'Clean one hot field ' + fieldName}, true, false)
            .then(function () {
                chainedOp.nextStep();
                self.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'One hot encode ' + fieldName}, true, false)
                    .then(function () {
                        // Now we need to fill in the null values with zero for our new cols
                        let allcols: string[] = self.toColumnArray(self.controller.engine.getCols());
                        let select: string[] = angular.copy(cols);
                        let idx: number = cols.length - 1;
                        angular.forEach(allcols, (col, index) => {
                            if (index > idx) {
                                select.push(`coalesce(${col},0).as("${col}")`);
                            }
                        });
                        let selectString = select.join();
                        let fillNAFormula = `select(${selectString})`
                        chainedOp.nextStep();
                        self.controller.addFunction(fillNAFormula, {formula: fillNAFormula, icon: 'functions', name: 'Fill NA'});
                    })
            })
    }

    /**
     * Gets the target data types supported for casting this column.
     */
    getAvailableCasts(): DT[] {
        // not supported
        return [];
    }

    /**
     * Unsorts the specified column.
     *
     * @param {ui.grid.GridColumn} column the column
     * @param {ui.grid.Grid} grid the grid with the column
     */
    removeSort(column: any, grid: any) {
        column.unsort();
        grid.refresh();
    }

    /**
     * Displays a dialog prompt to rename the specified column.
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    renameColumn(column: any, grid: any) {
        const self = this;
        const prompt = (this.$mdDialog as any).prompt({
            title: "Rename Column",
            textContent: "Enter a new name for the " + this.getColumnDisplayName(column) + " column:",
            placeholder: "Column name",
            ok: "OK",
            cancel: "Cancel"
        });
        this.$mdDialog.show(prompt).then(function (name) {
            // Update field policy
            if (column.index < self.controller.fieldPolicies.length) {
                const name = self.getColumnFieldName(column);
                const policy = self.controller.fieldPolicies[column.index];
                policy.name = name;
                policy.fieldName = name;
                policy.feedFieldName = name;
            }

            // Add rename function
            const script = self.getColumnFieldName(column) + ".as(\"" + StringUtils.singleQuote(name) + "\")";
            const formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, {
                formula: formula, icon: "mode_edit",
                name: "Rename " + self.getColumnDisplayName(column) + " to " + name
            });
        });
    }

    /**
     * Sets the domain type for the specified column.
     */
    setDomainType(column: any, domainTypeId: string) {
        const fieldName = this.getColumnFieldName(column);
        this.controller.setDomainType(column.index, domainTypeId);
        const formula = `withColumn("${fieldName}", ${fieldName})`
        this.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Change domain type'})
    }

    /**
     * Sorts the specified column.
     *
     * @param {string} direction "ASC" to sort ascending, or "DESC" to sort descending
     * @param {ui.grid.GridColumn} column the column to be sorted
     * @param {ui.grid.Grid} grid the grid with the column
     */
    sortColumn(direction: string, column: any, grid: any) {
        grid.sortColumn(column, direction, true);
        grid.refresh();
    }


    /**
     * Splits the specified column on the specified value.
     *
     * @param value - the value to split on
     * @param column - the column
     * @param grid - the table
     */
    splitOn(value: string, column: any, grid: any) {
        const displayName = this.getColumnDisplayName(column);
        const fieldName = this.getColumnFieldName(column);
        const pattern = "[" + StringUtils.singleQuote(value).replace(/]/g, "\\]") + "]";
        const formula = this.toFormula(`split(when(isnull(${fieldName}),"").otherwise(${fieldName}), '${pattern}').as("${displayName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "call_split", name: "Split " + this.getColumnDisplayName(column) + " on " + value});
    }

    /**
     * Executes the specified operation on the column.
     *
     * @param {Object} transform the transformation object from {@link VisualQueryColumnDelegate#getTransforms}
     * @param {ui.grid.GridColumn} column the column to be transformed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    transformColumn(transform: any, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const self = this;
        if ($.isFunction(transform.operation)) {
            transform.operation(self, column, grid);
        } else {
            const script = transform.operation + "(" + fieldName + ").as(\"" + StringUtils.singleQuote(fieldName) + "\")";
            const formula = this.toFormula(script, column, grid);
            const name = (transform.description ? transform.description : transform.name) + " " + this.getColumnDisplayName(column);
            this.controller.addFunction(formula, {formula: formula, icon: transform.icon, name: name});
        }
    }

    /**
     * Displays a dialog prompt to prompt for value to replace
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    replaceEmptyWithValue(self: any, column: any, grid: any) {

        const prompt = (self.$mdDialog as any).prompt({
            title: "Replace Empty",
            textContent: "Enter replace value:",
            placeholder: "0",
            ok: "OK",
            cancel: "Cancel"
        });
        self.$mdDialog.show(prompt).then(function (value : string) {
            let fieldName = self.getColumnFieldName(column);
            let script = `when((${fieldName} == "" || isnull(${fieldName}) ),"${value}").otherwise(${fieldName}).as("${fieldName}")`;
            const formula = self.toFormula(script, column, grid);
            self.controller.addFunction(formula, {
                formula: formula, icon: "find_replace",
                name: "Fill empty with " + value
            });

        });
    }

    /**
     * Round numeric to specified digits
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    roundNumeric(self: any, column: any, grid: any) {

        const prompt = (self.$mdDialog as any).prompt({
            title: "Round Numeric",
            textContent: "Enter scale decimal:",
            placeholder: "0",
            initialValue: "0",
            ok: "OK",
            cancel: "Cancel"
        });

        self.$mdDialog.show(prompt).then(function (value: any) {
            if (value != null && !isNaN(value) && (parseInt(value) >= 0)) {
                let fieldName = self.getColumnFieldName(column);
                let script = `round(${fieldName}, ${value}).as("${fieldName}")`;
                const formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, {
                    formula: formula, icon: "exposure_zero",
                    name: `Round ${fieldName} to ${value} digits`
                });
                return;
            } else {
                alert("Enter 0 or a positive numeric integer");
                self.roundNumeric(self, column, grid);
            }
        });

    }


    /**
     * Validates the specified filter.
     *
     * @param {Object} the column to apply the filter to
     * @param {Object} filter the filter to be validated
     * @param {VisualQueryTable} table the visual query table
     */
    validateFilter(header:any,filter: any, table: any ) {
        if (filter.term == "") {
            filter.term = null;
        } else {
            delete filter.regex;
        }
    }

    /**
     * Apply a list of filters to a given column(header)
     * @param header
     * @param {any[]} filters
     * @param table
     */
    applyFilters(header:any,filters:any[],table:any){
        table.onRowsChange();
        table.refreshRows();
    }

    /**
     * Apply a list single filter to a given column(header)
     * @param header
     * @param filter
     * @param table
     */
    applyFilter(header:any,filter: any, table: any){
        table.onRowsChange();
        table.refreshRows();
    }

    /**
     * Converts from the specified data type to a category.
     *
     * @param dataType - the data type
     * @returns the data category
     */
    protected fromDataType(dataType: string): DataCategory {
        switch (dataType) {
            case DataType.TINYINT:
            case DataType.SMALLINT:
            case DataType.INT:
            case DataType.BIGINT:
            case DataType.FLOAT:
            case DataType.DOUBLE:
            case DataType.DECIMAL:
                return DataCategory.NUMERIC;

            case DataType.TIMESTAMP:
            case DataType.DATE:
                return DataCategory.DATETIME;

            case DataType.STRING:
            case DataType.VARCHAR:
            case DataType.CHAR:
                return DataCategory.STRING;

            case DataType.BOOLEAN:
                return DataCategory.BOOLEAN;

            case DataType.BINARY:
                return DataCategory.BINARY;

            case DataType.ARRAY_DOUBLE:
                return DataCategory.ARRAY_DOUBLE;
        }
        // Deal with complex types
        if (dataType.startsWith(DataType.ARRAY.toString())) {
            return DataCategory.ARRAY;
        } else if (dataType.startsWith(DataType.MAP.toString())) {
            return DataCategory.MAP;
        } else if (dataType.startsWith(DataType.STRUCT.toString())) {
            return DataCategory.STRUCT;
        } else if (dataType.startsWith(DataType.UNION.toString())) {
            return DataCategory.UNION;
        }
        return DataCategory.OTHER;
    }

    /**
     * Gets the human-readable name of the specified column.
     */
    protected getColumnDisplayName(column: any): string {
        return column.displayName;
    }

    /**
     * Gets the SQL identifier for the specified column.
     */
    protected getColumnFieldName(column: any): string {
        return column.field || column.name;
    }

    /**
     * Gets the filters for a column based on category.
     *
     * @param dataCategory - the category for the column
     * @returns the filters for the column
     */
    protected getFilters(dataCategory: DataCategory): object[] {
        const filters = [];

        switch (dataCategory) {
            case DataCategory.STRING:
                filters.push({condition: this.uiGridConstants.filter.CONTAINS, icon: 'search', label: 'Contains...'});
            // fall through

            case DataCategory.NUMERIC:
                filters.push({
                        condition: this.uiGridConstants.filter.LESS_THAN, icon: 'keyboard_arrow_left',
                        label: 'Less than...'
                    },
                    {
                        condition: this.uiGridConstants.filter.GREATER_THAN, icon: 'keyboard_arrow_right',
                        label: 'Greater than...'
                    },
                    {condition: this.uiGridConstants.filter.EXACT, icon: '=', label: 'Equal to...'});
                break;

            default:
        }

        return filters;
    }

    /**
     * Gets the transformations for a column based on category.
     *
     * @param dataCategory - the category for the column
     * @returns the transformations for the column
     */
    protected getTransforms(dataCategory: DataCategory) {
        const transforms = [];
        const self = this;

        if (dataCategory === DataCategory.NUMERIC) {
            transforms.push(
                {description: 'Impute missing with mean', icon: 'functions', name: 'Impute', operation: self.imputeMeanColumn},
                {description: 'Replace null/nan with a specified value', icon: 'find_replace', name: 'Replace null/nan...', operation: self.replaceEmptyWithValue},
                {description: 'Convert to a numerical array for ML', icon: 'functions', name: 'Vectorize', operation: self.vectorizeColumn},
                {description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil'},
                {description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor'},
                {icon: 'exposure_zero', name: 'Round...', operation: self.roundNumeric},
                {descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees'},
                {descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians'});
        }
        else if (dataCategory === DataCategory.STRING) {
            transforms.push({description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower'},
                {description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper'},
                {description: 'Title case', icon: 'format_color_text', name: 'Title Case', operation: 'initcap'},
                {description: 'Extract numeric', icon: 'filter_2', name: 'Extract numeric', operation: self.extractNumeric},
                {icon: 'graphic_eq', name: 'Trim', operation: 'trim'},
                {description: 'One hot encode (or pivot) categorical values', icon: 'functions', name: 'One hot encode', operation: self.oneHotEncodeColumn},
                {description: 'Replace empty with a specified value', icon: 'find_replace', name: 'Replace empty...', operation: self.replaceEmptyWithValue},
                {description: 'Impute missing values by fill-forward', icon: 'functions', name: 'Impute missing values...', operation: self.imputeMissingColumn},
                {description: 'Index labels', icon: 'functions', name: 'Index labels', operation: self.indexColumn},
                {description: 'Crosstab', icon: 'poll', name: 'Crosstab', operation: self.crosstabColumn});
        } else if (dataCategory == DataCategory.ARRAY_DOUBLE) {
            transforms.push(
                {description: 'Rescale using standard deviation', icon: 'functions', name: 'Rescale using std dev', operation: self.rescaleStdDevColumn},
                {description: 'Rescale using mean', icon: 'functions', name: 'Rescale using mean', operation: self.rescaleMeanColumn},
                {description: 'Rescale using mean', icon: 'functions', name: 'Rescale using mean and std dev', operation: self.rescaleBothMethodsColumn},
                {description: 'Rescale min/max between 0-1', icon: 'functions', name: 'Rescale min/max between 0-1', operation: self.rescaleMinMax}
            );
        } else if (dataCategory === DataCategory.ARRAY) {
            transforms.push({icon: 'call_split', name: 'Explode to rows', operation: 'explode'},
                {description: 'Sort', icon: 'sort', name: 'Sort array', operation: 'sort_array'},
                {description: 'Extract to columns', icon: 'call_split', name: 'Extract to columns', operation: self.extractArrayItems}
            );
        }
        else if (dataCategory === DataCategory.BINARY) {
            transforms.push({icon: '#', name: 'CRC32', operation: 'crc32'},
                {icon: '#', name: 'MD5', operation: 'md5'},
                {icon: '#', name: 'SHA1', operation: 'sha1'},
                {icon: '#', name: 'SHA2', operation: 'sha2'});
        }
        else if (dataCategory === DataCategory.DATETIME) {
            transforms.push({description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'dayofmonth'},
                {description: 'Day of year for', icon: 'today', name: 'Day of Year', operation: 'dayofyear'},
                {description: 'Hour of', icon: 'access_time', name: 'Hour', operation: 'hour'},
                {description: 'Last day of month for', icon: 'today', name: 'Last Day of Month', operation: 'last_day'},
                {description: 'Minute of', icon: 'access_time', name: 'Minute', operation: 'minute'},
                {description: 'Month of', icon: 'today', name: 'Month', operation: 'month'},
                {description: 'Quarter of', icon: 'today', name: 'Quarter', operation: 'quarter'},
                {description: 'Second of', icon: 'access_time', name: 'Second', operation: 'second'},
                {description: 'Week of year for', icon: 'today', name: 'Week of Year', operation: 'weekofyear'},
                {description: 'Year of', icon: 'today', name: 'Year', operation: 'year'});
        }
        else if (dataCategory == DataCategory.STRUCT) {
            transforms.push({description: 'Flatten struct', icon: 'functions', name: 'Flatten struct', operation: self.flattenStructColumn});
        }
        else if (dataCategory === DataCategory.MAP) {
            transforms.push({icon: 'call_split', name: 'Explode', operation: 'explode'});
        } else if (dataCategory === DataCategory.BOOLEAN) {
            transforms.push({icon: 'exposure', name: 'Negate boolean', operation: self.negateBoolean});
        }


        return transforms;
    }

    /**
     * Creates a guaranteed unique field name
     * @param columns column list
     * @returns {string} a unique fieldname
     */
    protected toAsUniqueColumnName(columns: Array<any>, columnFieldName: any): string {
        let prefix = "new_";
        let idx = 0;
        let columnSet = new Set();
        let uniqueName = null;
        const self = this;
        columnSet.add(columnFieldName);
        angular.forEach(columns, function (item) {
            columnSet.add(self.getColumnFieldName(item));
        });

        while (uniqueName == null) {
            let name = prefix + idx;
            uniqueName = (columnSet.has(name) ? null : name);
            idx++;
        }
        return ".as(\"" + uniqueName + "\")"
    }

    /**
     * Creates a formula that adds a new column with the specified script. It generates a unique column name.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    protected toAppendColumnFormula(script: string, column: any, grid: any): string {
        const columnFieldName = this.getColumnFieldName(column);
        let formula = "";
        const self = this;
        angular.forEach(grid.columns, function (item, idx) {
            if (item.visible) {
                const itemFieldName = self.getColumnFieldName(item);
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += itemFieldName;
                if (itemFieldName == columnFieldName) {
                    formula += "," + script + self.toAsUniqueColumnName(grid.columns, columnFieldName);
                }
            }
        });

        formula += ")";
        return formula;
    }

    /**
     * Creates a formula that replaces the specified column with the specified script.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    protected toFormula(script: string, column: any, grid: any): string {

        const columnFieldName = this.getColumnFieldName(column);
        let formula = "";
        const self = this;

        angular.forEach(grid.columns, function (item) {
            if (item.visible) {
                const itemFieldName = self.getColumnFieldName(item);
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += (itemFieldName === columnFieldName) ? script : itemFieldName;
            }
        });

        formula += ")";
        return formula;
    }


}

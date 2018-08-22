import * as angular from "angular";

import {ColumnDelegate as IColumnDelegate, DataType as DT} from "./api/column";
import {DialogService} from "./api/services/dialog.service";
import {ColumnController} from "./column-controller";
import * as $ from "jquery";
import * as _ from "underscore";
import {ProfileHelper} from "./api/profile-helper";
import {DialogBuilder, WranglerFormBuilder, WranglerFormField} from "./WranglerFormBuilder";
import {QueryResultColumn} from "./model/query-result-column";

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

export class MenuItem {
    description: string = "";
    icon: string = "";
    name: string;
    operation:any;
}

export class MenuItems {
    math: MenuItem[] = [];
    replace:MenuItem[] = [];
    extract:MenuItem[] = [];
    ml:MenuItem[] = [];
    other:MenuItem[] = [];
    format:MenuItem[] = [];
    defaults:MenuItem[] = [];
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
    transforms: MenuItems;

    /**
     * Builder for dynamic forms
     */
    formBuilder: WranglerFormBuilder;


    /**
     * Constructs a column delegate.
     */
    constructor(public dataType: string, public controller: ColumnController, protected $mdDialog: angular.material.IDialogService, protected uiGridConstants: any, protected dialog?: DialogService) {
        this.dataCategory = this.fromDataType(dataType);
        this.filters = this.getFilters(this.dataCategory);
        this.transforms = this.getTransforms(this.dataCategory);
        this.formBuilder = new WranglerFormBuilder($mdDialog, dialog);
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

    /**
     * If the character is reserved regex then the character is escaped
     */
    escapeRegexCharIfNeeded(chr:string):string {
        if (chr == ' ') return "\\\\s";
        return (chr.match(/[\[\^\$\.\|\?\*\+\(\)]/g) ? `\\\\${chr}` : chr);
    }

    /**
     * Extracts text between regex of start and end of selection
     */
    extractRegex(value: string, column: any, grid: any) {
        const self = this;
        const fieldName = this.getColumnFieldName(column);
        const first = this.escapeRegexCharIfNeeded(value.charAt(0));
        const last = this.escapeRegexCharIfNeeded(value.charAt(value.length-1));

        let regexScript = `regexp_extract(${fieldName}, "${first}(.*)${last}", 0).as("${fieldName}")`
        const regexFormula = self.toFormula(regexScript, column, grid);

        // Now strip the start and end character which demark the extraction and trim whitespace
        let substrScript = `trim(substr(${fieldName},2,length(${fieldName})-2)).as("${fieldName}")`
        const substrFormula = self.toFormula(substrScript, column, grid);

        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);
        self.controller.pushFormula(regexFormula, {formula: regexFormula, icon: 'content_cut', name:  `Regex extract ${this.getColumnDisplayName(column)}`}, true, false).then(function () {
            chainedOp.nextStep();
            self.controller.addFunction(substrFormula, {formula: substrFormula, icon: 'spellcheck', name: `Clean ${fieldName}`});
        });
    }

    stripValueContaining(value: string, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const regex = this.escapeRegExp(value);
        const formula = this.toFormula("regexp_replace(" + fieldName + ", \"" + regex + "\", \"\").as(\"" + fieldName + "\")", column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "content_cut", name: "Strip " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Extracts string at indexes of the current selection
     * @param range selected range object
     * @param column the current column
     * @param grid the table
     */
    extractStringAtSelectedIndex(range: any, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const startOffset = range.startOffset;
        const endOffset = range.endOffset;
        const formula = this.toFormula(`substr(${fieldName}, ${startOffset}, ${endOffset}).as("${fieldName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "content_cut", name: `Extract string between index ${startOffset} and ${endOffset}`});
    }

    clearRowsEquals(value: string, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const formula = this.toFormula(`when(equal(${fieldName}, '${value}'),null).otherwise(${fieldName}).as("${fieldName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle", name: "Clear " + this.getColumnDisplayName(column) + " equals " + value});
    }

    /**
     * Replace value matching the current row
     * @param {string} value
     * @param column
     * @param grid
     */
    replaceValueEqualTo(value: string, column: any, grid: any) {
        const fieldName = this.getColumnFieldName(column);
        const dataType = column.dataType;
        const dataCategory = this.fromDataType(dataType);

        let self = this;
        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Replace value")
            .inputbox("replaceValue").withLabel("Replace value:").optional().build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let formula = '';
                let replaceValue=fields['replaceValue'].value;
                if (dataCategory == DataCategory.NUMERIC) {
                    if (replaceValue == null || replaceValue == '') {
                        replaceValue = `''`
                    }
                    formula = self.toFormula(`when(${fieldName}==${value}, ${replaceValue}).otherwise(${fieldName}).as("${fieldName}")`, column, grid);
                } else {
                    formula = self.toFormula(`when(${fieldName}=='${value}', '${replaceValue}').otherwise(${fieldName}).as("${fieldName}")`, column, grid);
                }
                self.controller.addFunction(formula, {formula: formula, icon: "find_replace", name: `Replace ${value} with ${replaceValue}`});
            });

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
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(not(contains(${fieldName}, '${value}')))`;
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Delete " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is not the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsEqualTo(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} != '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "≠", name: "Delete " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is less than or equal to the specified value.
     *
     * @param value - the maximum value (inclusive)
     * @param column - the column
     */
    deleteRowsGreaterThan(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} <= '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "≯", name: "Delete " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is greater than or equal to the specified value.
     *
     * @param value - the minimum value (inclusive)
     * @param column - the column
     */
    deleteRowsLessThan(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} >= '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "≮", name: "Delete " + this.getColumnDisplayName(column) + " less than " + value});
    }

    /**
     * Filters for rows where the specified column is null.
     */
    findNullRows(column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(isnull(${fieldName}))`;
        this.controller.addFunction(formula, {formula: formula, icon: "=", name: "Find where " + this.getColumnDisplayName(column) + " is null"});
    }

    /**
     * Filters for rows where the specified column contains the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsContaining(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(contains(${fieldName}, '${value}'))`;
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Find " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsEqualTo(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} == '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "=", name: "Find " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is greater than the specified value.
     *
     * @param value - the minimum value (exclusive)
     * @param column - the column
     */
    findRowsGreaterThan(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} > '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "keyboard_arrow_right", name: "Find " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is less than the specified value.
     *
     * @param value - the maximum value (exclusive)
     * @param column - the column
     */
    findRowsLessThan(value: string, column: any) {
        let fieldName = this.getColumnFieldName(column);
        const formula = `filter(${fieldName} < '${value}')`;
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
     * Pad text with leading characters
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    leftPad(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);

        self.controller.extractColumnStatistics(fieldName).then(function (profileData: ProfileHelper) {

            let dialog : DialogBuilder = self.formBuilder.newInstance();
            dialog.withTitle("Left Pad")
                .inputbox("length").withLabel("Fixed length:").default(profileData.maxLen).intNumeric().build()
                .inputbox( "padChar").withLabel("Pad char:").default(" ").withValidation(".", "Single character required.").build()
                .showDialog((fields:Map<String,WranglerFormField>)=> {
                    let length=fields['length'].value;
                    let padChar=fields['padChar'].value;

                    const script = `lpad(${fieldName}, ${length}, "${padChar}").as("${fieldName}")`
                    const formula = self.toFormula(script, column, grid);
                    self.controller.addFunction(formula, {formula: formula, icon: 'format_align_right', name: `Left pad ${fieldName}`});
                });
        });

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
     * Returns column names as an array
     * @returns {string[]}
     */
    getColumnNames() : string[] {
        return (<any>this).controller.engine.getCols().map( (f:QueryResultColumn)=> { return f.field });
    }

    /**
     * Crosstab against another column
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    crosstabColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        let cols = self.getColumnNames();

        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Crosstab")
            .selectbox("crossColumn").withLabel("Crosstab column:").withChoices(cols).build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let crossColumn=fields['crossColumn'].value;

                let crossColumnTemp = (crossColumn == fieldName ? crossColumn + "_0" : crossColumn);
                let clean = self.createCleanFieldFormula(crossColumn, crossColumnTemp);
                const cleanFormula = `select(${fieldName}, ${clean})`;
                let chainedOp: ChainedOperation = new ChainedOperation(2);
                let crossColumnName = crossColumn;
                self.controller.setChainedQuery(chainedOp);
                self.controller.pushFormula(cleanFormula, {formula: cleanFormula, icon: 'spellcheck', name: `Clean ${fieldName} and ${crossColumn}`}, true, false).then(function () {
                    chainedOp.nextStep();
                    const formula = `crosstab("${fieldName}","${crossColumnTemp}")`
                    self.controller.addFunction(formula, {formula: formula, icon: 'poll', name: `Crosstab ${fieldName} and ${crossColumnName}`});
                });
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
                if (_.isArray(fieldNameB)) {
                    cols = cols.concat(fieldNameB);
                }
                else {
                    cols.push(fieldNameB);
                }
            } else if ((_.isArray(fieldNameB) && !_.contains(fieldNameB, colName)) || (_.isString(fieldNameB) && colName != fieldNameB)) {
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
     * Extract array item into a new column
     * @param column
     * @param grid
     */
    extractArrayItem(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);

        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle(`Extract array item`)
            .inputbox("colIndex").withLabel("Array index:").intNumeric().default(0).build()
            .inputbox("colName").withLabel("New column:").default(fieldName+"_0").validName().build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let colIndex=fields['colIndex'].value;
                let colName=fields['colName'].value;

                let formula = self.createAppendColumnFormula(`getItem(${fieldName},${colIndex}).as("${colName}")`, column, grid, colName);
                self.controller.addFunction(formula, {formula: formula, icon: "remove", name: `Extract item ${colIndex}`});
            });
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
        var formula = self.generateMoveScript(fieldName, columns, grid, false);
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
     * Apply logit transform
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    logitTransform(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);
        const script = `ln(${fieldName}/(1-${fieldName}))`;
        const formula = self.toFormula(script, column, grid);
        self.controller.addFunction(formula, {
            formula: formula, icon: "functions",
            name: "Logit transform " + self.getColumnDisplayName(column)
        });
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
     * Calculate outliers 1 (outlier) or 0 (not outlier)
     * @param self
     * @param column
     * @param grid
     * @returns {any}
     */
    identifyOutliers(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);
        let quantileStats = self.approxQuantileFormula(fieldName, 4);
        self.controller.extractFormulaResult(quantileStats, 10000)
            .then(function (value: any) {
                const Q1 = value[0];
                const Q3 = value[2];
                const IQR = Q3 - Q1;

                let lower = (Q1 - (1.5 * IQR));
                let upper = (Q3 + (1.5 * IQR));
                let script = `when(or(${fieldName} < ${lower},${fieldName}>${upper}),1).otherwise(0)`
                const formula = self.toAppendColumnFormula(script, column, grid, `${fieldName}_outlier`);
                self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Find outliers ${fieldName}`});
            });
    }

    approxQuantileFormula(fieldName:string, bins:number) {
        let binSize = 1 / bins;
        let arr = []
        for (let i = 1; i < bins; i++) {
            arr.push(i * binSize)
        }
        return `select(approxQuantile("${fieldName}", [${arr}], 0.0).as("data"))`

    }

    /**
     * Bin values of the column into discrete quantiles
     * @param self
     * @param column
     * @param grid
     */
    binValues(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);

        self.controller.extractColumnStatistics(fieldName).then(function (profileData: ProfileHelper) {
            if (profileData.percNull > 0) {
                self.controller.displayError("Error", "Column must be clean of empty/NaN to use this function");
                return;
            }

            let dialog : DialogBuilder = self.formBuilder.newInstance();
            dialog.withTitle(`Bin Values`)
                .inputbox("bin").withLabel("# of bins:").minIntValidator(2).default(4).build()
                .inputbox("sample").withLabel("# of rows to sample:").intNumeric().default(10000).build()
                .showDialog((fields:Map<String,WranglerFormField>)=> {
                    let bins=fields['bins'].getValueAsNumber();
                    let sample=fields['sample'].getValueAsNumber();

                    let binSize = 1 / bins;
                    let arr = []
                    for (let i = 1; i < bins; i++) {
                        arr.push(i * binSize)
                    }
                    let quantileStats = self.approxQuantileFormula(fieldName, bins);
                    self.controller.extractFormulaResult(quantileStats, sample)
                        .then(function (value: any) {
                            let formulaArray = [];
                            for (let i = 1; i < bins; i++) {
                                let val = value[i-1];
                                formulaArray.push(`when(${fieldName}<${val},${i})`);
                            }
                            formulaArray.push(`otherwise(${bins}).as("${fieldName}")`);
                            let script = formulaArray.join(".");
                            const formula = self.toFormula(script, column, grid);
                            self.controller.addFunction(formula, {
                                formula: formula, icon: "functions",
                                name: "Bin " + self.getColumnDisplayName(column)
                            });
                        })
                });
        });
    }

    rescaleMinMax(self: any, column: any, grid: any) {
        const fieldName = self.getColumnFieldName(column);

        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle(`Rescale Value`)
            .inputbox("minScale").withLabel("Min:").intNumeric().default(0).build()
            .inputbox("maxScale").withLabel("Max:").intNumeric().default(1).build()
            .withValidator(function(fields:Map<String,WranglerFormField>) {
                let minScale=fields['minScale'].getValueAsNumber();
                let maxScale=fields['maxScale'].getValueAsNumber();
                if (minScale != null && maxScale != null) {
                    return (minScale < maxScale);
                }
                return true;
            })
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let minScale = fields['minScale'].getValueAsNumber();
                let maxScale = fields['maxScale'].getValueAsNumber();

                self.controller.extractColumnStatistics(fieldName).then(function (profileData: ProfileHelper) {
                    let min = profileData.min;
                    let max = profileData.max;
                    var algo: string;
                    if (min === max) {
                        algo = `(0.5*((${minScale})+(${maxScale})))`
                    } else {
                        algo = `(((${fieldName}-(${min}))/((${max})-(${min})))*((${maxScale})-(${minScale})+(${minScale})))`
                    }
                    let script = `when(${algo}>${maxScale},${maxScale}).when(${algo}<${minScale},${minScale}).otherwise(${algo}).as("${fieldName}")`

                    const formula = self.toFormula(script, column, grid);
                    self.controller.addFunction(formula, {
                        formula: formula, icon: "functions",
                        name: "Rescale " + self.getColumnDisplayName(column)
                    });
                })
            });

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
    structToFields(column: any): string[] {

        let fields: string = column.dataType;
        fields = fields.substr(7, fields.length - 2);
        let level = 0;
        let cleaned = [];
        for (let i = 0; i < fields.length; i++) {
            switch (fields.charAt(i)) {
                case '<':
                    level++;
                    break;
                case '>':
                    level--;
                    break;
                default:
                    if (level == 0) {
                        cleaned.push(fields.charAt(i));
                    }
            }
        }
        let cleanedString = cleaned.join("");
        let fieldArray: string[] = cleanedString.split(",");
        return fieldArray.map((v: string) => {
            return v.split(":")[0].toLowerCase();
        });
    }

    /**
     * Guaranteed to return a unique column name that conforms to the field naming requirements
     * @param {Array<string>} columns
     * @param {string} columnFieldName
     * @param {number} idx
     * @returns {string}
     */
    uniqueName(columns: Array<string>, columnFieldName: string, idx: number = -1): string {

        if (columns == null || columns.length == 0) {
            return columnFieldName;
        }
        let alias = columnFieldName.replace(/^(_)|[^a-zA-Z0-9_]+/g, "");
        if (idx >= 0) {
            alias += "_"+idx;
        }
        if (columns.indexOf(alias.toLowerCase()) > -1) {
            return this.uniqueName(columns, columnFieldName, idx+1);
        }
        return alias;
    }

    /**
     * Flattens a struct column into multiple fields (one-level)
     * @param self
     * @param column
     * @param grid
     */
    flattenStructColumn(self: any, column: any, grid: any) {

        const fieldName = self.getColumnFieldName(column);

        let structFields = self.structToFields(column);
        let existingCols = self.toColumnArray(grid.columns);

        let formula: string;

        // Generate fields with unique names
        let existingColsLower = existingCols.map((field: string) => {
            return field.toLowerCase();
        });
        let fieldParts = structFields.map((field: string) => {
            let alias = self.uniqueName(existingColsLower, field);
            existingColsLower.push(alias.toLowerCase());
            return (`getField(${fieldName},"${field}").as("${alias}")`);
        });

        // Insert new fields into the field list
        let idxOfCurrentColumn = existingCols.indexOf(fieldName);
        let formulaFields: string[] = []
        if (idxOfCurrentColumn == 0) {
            formulaFields.push(fieldParts);
            formulaFields.push(existingCols.slice(idxOfCurrentColumn + 1));
        } else if (idxOfCurrentColumn == existingCols.length - 1) {
            formulaFields.push(existingCols.slice(0, idxOfCurrentColumn));
            formulaFields.push(fieldParts);
        } else {
            formulaFields.push(existingCols.slice(0, idxOfCurrentColumn));
            formulaFields.push(fieldParts);
            formulaFields.push(existingCols.slice(idxOfCurrentColumn + 1));
        }
        if (formulaFields[formulaFields.length - 1].length == 0) {
            formulaFields.pop();
        }
        let fieldString = formulaFields.join(",");
        formula = `select(${fieldString})`;

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
     * Replace all NaN with a fixed value
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    replaceNaNWithValue(self: any, column: any, grid: any) {
        const dataType = column.dataType;
        const dataCategory = self.fromDataType(dataType);
        let fieldName = self.getColumnFieldName(column);

        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Replace NaN")
            .inputbox("replaceValue").withLabel("Replace value:").anyNumber().default(0).build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let value = fields['replaceValue'].value;
                let script = `when((${fieldName} == "" || isnull(${fieldName}) ),${value}).otherwise(${fieldName}).as("${fieldName}")`;
                const formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, {
                    formula: formula, icon: "find_replace",
                    name: "Fill NaN with " + value
                });
            });
    }

    /**
     * Replace empty with a fixed value
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    replaceEmptyWithValue(self: any, column: any, grid: any) {
        let fieldName = self.getColumnFieldName(column);

        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Replace Missing")
            .inputbox("replaceValue").withLabel("Replace value:").build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let value = fields['replaceValue'].value;

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

        let fieldName = self.getColumnFieldName(column);
        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Round Number")
            .inputbox("scale").withLabel("Scale:").intNumeric().default(2).build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let scale = fields['scale'].value;
                const script = `round(${fieldName},${scale}).as("${fieldName}")`;
                const formula = self.toFormula(script, column, grid);
                self.controller.addFunction(formula, {
                    formula: formula, icon: "exposure_zero",
                    name: `Round ${fieldName} to ${scale} digits`
                });
            });
    }

    /**
     * Extract regex
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractRegexPattern(self: any, column: any, grid: any) {

        let fieldName = self.getColumnFieldName(column);
        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Extract Regex")
            .inputbox("regex").withLabel("Regex:").default("\\\\[(.*?)\\\\]").build()
            .inputbox("group").withLabel("Group:").intNumeric().default(0).build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let regex = fields['regex'].value;
                let group = fields['group'].value;
                self.executeRegex(self,column,grid,regex,group);
            });
    }

    /**
     * Extract between two delimiters
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractDelimiters(self: any, column: any, grid: any) {

        let fieldName = self.getColumnFieldName(column);
        let dialog : DialogBuilder = self.formBuilder.newInstance();
        dialog.withTitle("Extract Between Delimiters")
            .inputbox( "start").withLabel("Start delim:").default("[").withValidation(".", "Single character required.").build()
            .inputbox( "end").withLabel("End delim:").default("]").withValidation(".", "Single character required.").build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let start = self.escapeRegexCharIfNeeded(fields['start'].value.toString().charAt(0));
                let end = self.escapeRegexCharIfNeeded(fields['end'].value.toString().charAt(0));
                const regex = `${start}(.*?)${end}`;
                self.executeRegex(self,column,grid,regex,0);
            });
    }

    // Executes the regex formula
    private executeRegex(self:any, column: any, grid:any, regex:string, group:number) {
        let fieldName = self.getColumnFieldName(column);
        const script = `regexp_extract(${fieldName}, "${regex}", ${group}).as("${fieldName}")`
        const formula = self.toFormula(script, column, grid);
        self.controller.addFunction(formula, {
            formula: formula, icon: "content_cut",
            name: `Extract regex from ${fieldName}`
        });
    }

    private buildOrderBy(clauseCollection : string[], orderBy:string, asc:boolean, defaultIfNotSpecified:string) : void {
        if (!(orderBy == null || orderBy == defaultIfNotSpecified)) {
            let stmt = (asc ? `${orderBy}` : `desc("${orderBy}")`);
            clauseCollection.push(stmt);
        }
    }

    /**
     * Provides a dialog for capturing order by information and returns the orderBy clause
     */
    orderByDialog(self: any, column: any, grid: any, title: string, cb: any) {
        const DEFAULT = "(default)";
        let fieldName = self.getColumnFieldName(column);
        let cols = self.getColumnNames();
        cols.push(DEFAULT);
        let dialog : DialogBuilder = self.formBuilder.newInstance();

        dialog.withTitle(title)
            .selectbox("orderBy1").withLabel("OrderBy:").default(DEFAULT).withChoices(cols).build()
            .checkbox("asc1").withLabel("Asc:").default(true).build()
            .showDialog((fields:Map<String,WranglerFormField>)=> {
                let orderByClause :string[] = [];
                self.buildOrderBy(orderByClause, fields['orderBy1'].value, fields['asc1'].value, DEFAULT);
                let orderBy = (orderByClause.length == 0 ? '1' : orderByClause.join(","));
                cb(orderBy);
            });
    }


    /**
     * % difference from previous value
     */
    percDiffFromPrevious(self: any, column: any, grid: any) {
        self.orderByDialog(self,column,grid, "Percentage Difference From Previous", (orderBy: string)=> {
            let fieldName = self.getColumnFieldName(column);
            let script = `((${fieldName}-lag(${fieldName},1).over(orderBy(${orderBy})))/(lag(${fieldName},1).over(orderBy(${orderBy})))*100)`;
            const formula = self.toAppendColumnFormula(script, column, grid, `${fieldName}_diffp`);
            self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `% diff from prev ${fieldName}`});
        });
    }

    /**
     * Absolute difference from previous value
     */
    diffFromPrevious(self: any, column: any, grid: any) {

        self.orderByDialog(self,column,grid, "Difference From Previous", (orderBy: string)=> {
            let fieldName = self.getColumnFieldName(column);
            let script = `(${fieldName}-lag(${fieldName},1).over(orderBy(${orderBy})))`;
            const formula = self.toAppendColumnFormula(script, column, grid, `${fieldName}_diff`);
            self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Difference from prev ${fieldName}`});
        });
    }

    /**
     * Create a running average
     */
    runningAverage(self: any, column: any, grid: any) {

        self.orderByDialog(self,column,grid, "Running Average", (orderBy: string)=> {
            let fieldName = self.getColumnFieldName(column);
            let script = `(avg(${fieldName}).over(orderBy(${orderBy}).rowsBetween(-2147483647,0)))`;
            const formula = self.toAppendColumnFormula(script, column, grid, `${fieldName}_ravg`);
            self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Running average ${fieldName}`});
        });

    }

    /**
     * Create a running total
     */
    runningTotal(self: any, column: any, grid: any) {

        self.orderByDialog(self,column,grid, "Running Total", (orderBy: string)=> {
            let fieldName = self.getColumnFieldName(column);
            let script = `(sum(${fieldName}).over(orderBy(${orderBy}).rowsBetween(-2147483647,0)))`;
            const formula = self.toAppendColumnFormula(script, column, grid, `${fieldName}_rtot`);
            self.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Running total ${fieldName}`});
        });
    }

    /**
     * Validates the specified filter.
     *
     * @param {Object} the column to apply the filter to
     * @param {Object} filter the filter to be validated
     * @param {VisualQueryTable} table the visual query table
     */
    validateFilter(header: any, filter: any, table: any) {
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
    applyFilters(header: any, filters: any[], table: any) {
        table.onRowsChange();
        table.refreshRows();
    }

    /**
     * Apply a list single filter to a given column(header)
     * @param header
     * @param filter
     * @param table
     */
    applyFilter(header: any, filter: any, table: any) {
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
    protected getTransforms(dataCategory: DataCategory) : MenuItems {

        let transforms = new MenuItems();

        const self = this;

        if (dataCategory === DataCategory.NUMERIC) {
            transforms.ml.push(
                {description: 'Bin values', icon: 'functions', name: 'Bin values...', operation: self.binValues},
                {description: 'Identify outliers', icon: 'unicorn', name: 'Identify outliers', operation: self.identifyOutliers},
                {description: 'Impute missing with mean', icon: 'functions', name: 'Impute using mean...', operation: self.imputeMeanColumn},
                {description: 'Replace empty/NAN with a specified value', icon: 'find_replace', name: 'Replace NaN...', operation: self.replaceNaNWithValue},
                {description: 'Rescale min/max', icon: 'functions', name: 'Rescale min/max...', operation: self.rescaleMinMax}
            );
            transforms.math.push(
                // {description: 'Convert to a numerical array for ML', icon: 'functions', name: 'Vectorize', operation: self.vectorizeColumn},
                {description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil'},
                {description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor'},
                {description: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees'},
                {description: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians'},
                {description: 'Round number', icon: 'exposure_zero', name: 'Round...', operation: self.roundNumeric},
                {description: 'Log', icon: 'functions', name: 'Log10', operation: 'log10'},
                {description: 'Logit transform', icon: 'functions', name: 'Logit', operation: self.logitTransform}
            );
            transforms.other.push(
                {description: 'Crosstab', icon: 'poll', name: 'Crosstab', operation: self.crosstabColumn},
                {description: 'Running average', icon: 'functions', name: 'Running average...', operation: self.runningAverage},
                {description: 'Running total', icon: 'functions', name: 'Running total...', operation: self.runningTotal},
                {description: '% difference', icon: 'functions', name: 'Percent diff from prev value...', operation: self.percDiffFromPrevious},
                {description: 'Difference from previous', icon: 'functions', name: 'Difference from prev value...', operation: self.diffFromPrevious}
            );
        }
        else if (dataCategory === DataCategory.STRING) {

            transforms.format.push({description: 'Lowercase', icon: 'arrow_downward', name: 'lowercase', operation: 'lower'},
                {description: 'Uppercase', icon: 'arrow_upward', name: 'UPPERCASE', operation: 'upper'},
                {description: 'Title case', icon: 'format_color_text', name: 'TitleCase', operation: 'initcap'},
                {description: 'Trim whitespace', icon: 'graphic_eq', name: 'Trim', operation: 'trim'},
                {description: 'Left pad', icon: 'format_align_right', name: 'Left pad', operation : self.leftPad}
                );

            transforms.extract.push(
                {description: 'Extract numeric', icon: 'filter_2', name: 'Numbers', operation: self.extractNumeric},
                {description: 'Extract regex', icon: 'filter_2', name: 'Regex pattern', operation: self.extractRegexPattern},
                {description: 'Extract delimiters', icon: 'filter_2', name: 'Between delimiters', operation: self.extractDelimiters},
            );

            transforms.ml.push(
                {description: 'Impute missing values by fill-forward', icon: 'functions', name: 'Impute missing values...', operation: self.imputeMissingColumn},
                {description: 'Index labels', icon: 'functions', name: 'Index labels', operation: self.indexColumn},
                {description: 'One hot encode (or pivot) categorical values', icon: 'functions', name: 'One hot encode', operation: self.oneHotEncodeColumn},
                {description: 'Replace empty with a specified value', icon: 'find_replace', name: 'Replace missing...', operation: self.replaceEmptyWithValue});

            transforms.other.push(
                {description: 'Crosstab', icon: 'poll', name: 'Crosstab...', operation: self.crosstabColumn});

        } else if (dataCategory === DataCategory.ARRAY) {
            transforms.defaults.push(
                {description: 'Extract to columns', icon: 'call_split', name: 'Extract to columns', operation: self.extractArrayItems},
                {description: 'Extract item to column', icon: 'call_split', name: 'Extract item...', operation: self.extractArrayItem},
                {description: 'Convert array elements to rows', icon: 'call_split', name: 'Explode to rows', operation: 'explode'},
                {description: 'Sort', icon: 'sort', name: 'Sort array', operation: 'sort_array'}
            );
        }
        else if (dataCategory === DataCategory.BINARY) {
            transforms.defaults.push({'description': 'crc32 hash', icon: '#', name: 'CRC32', operation: 'crc32'},
                {'description': 'md5 hash', icon: '#', name: 'MD5', operation: 'md5'},
                {'description': 'sha1 hash', icon: '#', name: 'SHA1', operation: 'sha1'},
                {'description': 'sha2 hash', icon: '#', name: 'SHA2', operation: 'sha2'});
        }
        else if (dataCategory === DataCategory.DATETIME) {
            transforms.other.push({description: 'Day of month for', icon: 'today', name: 'Day of Month', operation: 'dayofmonth'},
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
            transforms.defaults.push({description: 'Flatten struct', icon: 'functions', name: 'Flatten struct', operation: self.flattenStructColumn});
        }
        else if (dataCategory === DataCategory.MAP) {
            transforms.defaults.push({description: 'Explode array to rows', icon: 'call_split', name: 'Explode', operation: 'explode'});
        } else if (dataCategory === DataCategory.BOOLEAN) {
            transforms.defaults.push({description: 'Flip boolean', icon: 'exposure', name: 'Negate boolean', operation: self.negateBoolean});
        }
        return transforms;
    }

    /**
     * Returns the as alias clause
     * @param columns column list
     * @returns {string} a unique fieldname
     */
    protected toAliasClause(name: string): string {
        return ".as(\"" + name + "\")"
    }

    /**
     * Creates a guaranteed unique field name
     * @param columns column list
     * @returns {string} a unique fieldname
     */
    protected toUniqueColumnName(columns: Array<any>, columnFieldName: any): string {
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
        return uniqueName;
    }

    /**
     * Creates a formula that adds a new column with the specified script. It generates a unique column name.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    protected toAppendColumnFormula(script: string, column: any, grid: any, newField ?: string): string {
        const self = this;
        const columnFieldName = self.getColumnFieldName(column);
        const uniqueName = (newField == null ? self.toUniqueColumnName(grid.columns, columnFieldName) : newField);
        return self.createAppendColumnFormula(script, column, grid, uniqueName);
    }

    /**
     * Creates a formula that adds a new column with the specified script.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    protected createAppendColumnFormula(script: string, column: any, grid: any, newField: string): string {
        const self = this;
        const columnFieldName = this.getColumnFieldName(column);
        let formula = "";

        angular.forEach(grid.columns, function (item, idx) {
            if (item.visible) {
                const itemFieldName = self.getColumnFieldName(item);
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += itemFieldName;
                if (itemFieldName == columnFieldName) {
                    formula += "," + script + self.toAliasClause(newField);
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

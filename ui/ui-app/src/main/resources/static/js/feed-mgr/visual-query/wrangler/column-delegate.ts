import {TdDialogService} from "@covalent/core/dialogs";
import * as _ from "underscore";

import {CloneUtil} from "../../../common/utils/clone-util";
import {StringUtils} from "../../../common/utils/StringUtils";
import {ColumnDelegate as IColumnDelegate, DataType as DT} from "./api/column";
import {ColumnProfile} from "./api/column-profile";
import {DialogService} from "./api/services/dialog.service";
import {ColumnController} from "./column-controller";
import {ColumnUtil} from "./core/column-util";
import {BinValuesForm} from "./core/columns/bin-values-form";
import {CrossTabForm} from "./core/columns/cross-tab-form";
import {ExtractDelimsForm} from "./core/columns/extract-delims-form";
import {ExtractIndexForm} from "./core/columns/extract-index-form";
import {ExtractRegexForm} from "./core/columns/extract-regex-form";
import {FillForwardForm} from "./core/columns/fill-forward-form";
import {LpadForm} from "./core/columns/lpad-form";
import {OrderByForm} from "./core/columns/order-by-form";
import {RenameColumnForm} from "./core/columns/rename-column-form";
import {ReplaceNanForm} from "./core/columns/replace-nan-form";
import {ReplaceValueForm} from "./core/columns/replace-value-form";
import {RescaleForm} from "./core/columns/rescale-form";
import {RoundNumberForm} from "./core/columns/round-number-form";
import {AnalyzeColumnDialog} from "../transform-data/main-dialogs/analyze-column-dialog";

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
export const DataType = {
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
    operation?: string;
    operationFn?: Function
}

export class MenuItems {
    calculate: MenuItem[] = [];
    replace: MenuItem[] = [];
    extract: MenuItem[] = [];
    ml: MenuItem[] = [];
    other: MenuItem[] = [];
    format: MenuItem[] = [];
    defaults: MenuItem[] = [];
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
     * Constructs a column delegate.
     */
    constructor(public dataType: string, public controller: ColumnController, protected tdDialog: TdDialogService, protected uiGridConstants: any, protected dialog?: DialogService) {
        this.dataCategory = ColumnUtil.fromDataType(dataType);
        this.filters = this.getFilters(this.dataCategory);
        this.transforms = this.getTransforms(this.dataCategory);
    }

    /**
     * Casts this column to the specified type.
     */
    castTo(dataType: DT): void {
        // not supported
    }


    /**
     * Extracts text between regex of start and end of selection
     */
    extractRegex(value: string, column: any, grid: any) {
        const self = this;
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const first = ColumnUtil.escapeRegexCharIfNeeded(value.charAt(0));
        const last = ColumnUtil.escapeRegexCharIfNeeded(value.charAt(value.length - 1));

        let regexScript = `regexp_extract(${fieldName}, "${first}(.*)${last}", 0).as("${fieldName}")`
        const regexFormula = ColumnUtil.toFormula(regexScript, column, grid);

        // Now strip the start and end character which demark the extraction and trim whitespace
        let substrScript = `trim(substr(${fieldName},2,length(${fieldName})-2)).as("${fieldName}")`
        const substrFormula = ColumnUtil.toFormula(substrScript, column, grid);

        let chainedOp: ChainedOperation = new ChainedOperation(2);
        self.controller.setChainedQuery(chainedOp);
        self.controller.pushFormula(regexFormula, {formula: regexFormula, icon: 'content_cut', name: `Regex extract ${ColumnUtil.getColumnDisplayName(column)}`}, true, false).then(function () {
            chainedOp.nextStep();
            self.controller.addFunction(substrFormula, {formula: substrFormula, icon: 'spellcheck', name: `Clean ${fieldName}`});
        });
    }

    stripValueContaining(value: string, column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const regex = ColumnUtil.escapeRegExp(value);
        const formula = ColumnUtil.toFormula("regexp_replace(" + fieldName + ", \"" + regex + "\", \"\").as(\"" + fieldName + "\")", column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "content_cut", name: "Strip " + ColumnUtil.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Extracts string at indexes of the current selection
     * @param range selected range object
     * @param column the current column
     * @param grid the table
     */
    extractStringAtSelectedIndex(range: any, column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const startOffset = (range.startOffset+1);
        const endOffset = (range.endOffset - startOffset + 1);
        const formula = ColumnUtil.toFormula(`substr(${fieldName}, ${startOffset}, ${endOffset}).as("${fieldName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "content_cut", name: `Extract string between index ${startOffset} and ${endOffset}`});
    }

    clearRowsEquals(value: string, column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = ColumnUtil.toFormula(`when(equal(${fieldName}, '${value}'),null).otherwise(${fieldName}).as("${fieldName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle", name: "Clear " + ColumnUtil.getColumnDisplayName(column) + " equals " + value});
    }

    /**
     * Replace value matching the current row
     * @param {string} value
     * @param column
     * @param grid
     */
    replaceValueEqualTo(value: string, column: any, grid: any) {
        let form = new ReplaceValueForm(column, grid, this.controller, value);
        this.dialog.openColumnForm(form);
    }

    /**
     * Filters for rows where the specified column is not null.
     */
    deleteNullRows(column: any) {
        const formula = "filter(not(isnull(" + ColumnUtil.getColumnFieldName(column) + ")))";
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle_outline", name: "Delete " + ColumnUtil.getColumnDisplayName(column) + " if null"});
    }

    /**
     * Filters for rows where the specified column does not contain the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsContaining(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(not(contains(${fieldName}, '${value}')))`;
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Delete " + ColumnUtil.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is not the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsEqualTo(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} != '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle_outline", name: "Delete " + ColumnUtil.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is less than or equal to the specified value.
     *
     * @param value - the maximum value (inclusive)
     * @param column - the column
     */
    deleteRowsGreaterThan(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} <= '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle", name: "Delete " + ColumnUtil.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is greater than or equal to the specified value.
     *
     * @param value - the minimum value (inclusive)
     * @param column - the column
     */
    deleteRowsLessThan(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} >= '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "remove_circle", name: "Delete " + ColumnUtil.getColumnDisplayName(column) + " less than " + value});
    }

    /**
     * Filters for rows where the specified column is null.
     */
    findNullRows(column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(isnull(${fieldName}))`;
        this.controller.addFunction(formula, {formula: formula, icon: "filter_list", name: "Find where " + ColumnUtil.getColumnDisplayName(column) + " is null"});
    }

    /**
     * Filters for rows where the specified column contains the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsContaining(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(contains(${fieldName}, '${value}'))`;
        this.controller.addFunction(formula, {formula: formula, icon: "filter_list", name: "Find " + ColumnUtil.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsEqualTo(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} == '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "filter_list", name: "Find " + ColumnUtil.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is greater than the specified value.
     *
     * @param value - the minimum value (exclusive)
     * @param column - the column
     */
    findRowsGreaterThan(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} > '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "filter_list", name: "Find " + ColumnUtil.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is less than the specified value.
     *
     * @param value - the maximum value (exclusive)
     * @param column - the column
     */
    findRowsLessThan(value: string, column: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const formula = `filter(${fieldName} < '${value}')`;
        this.controller.addFunction(formula, {formula: formula, icon: "filter_list", name: "Find " + ColumnUtil.getColumnDisplayName(column) + " less than " + value});
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
        this.controller.pushFormula(formula, {formula: formula, icon: "remove_circle", name: "Hide " + ColumnUtil.getColumnDisplayName(column)});
        this.controller.fieldPolicies = this.controller.fieldPolicies.filter((value, index) => index == column.index);
    }

    /**
     * Display the analyze column view
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    showAnalyzeColumn(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
       this.controller.showAnalyzeColumn(fieldName);
    }

    /**
     * Clone the specified column.
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    cloneColumn(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const script = "clone(" + fieldName + ")";
        const formula = ColumnUtil.toAppendColumnFormula(script, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: 'content_copy', name: 'Clone ' + ColumnUtil.getColumnDisplayName(column)});
    }

    /**
     * Pad text with leading characters
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    leftPad(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);

        this.controller.extractColumnStatistics(fieldName).then((profileData: ColumnProfile) => {

            let form = new LpadForm(column, grid, this.controller, profileData.maxLen);
            this.dialog.openColumnForm(form);
        });

    }

    /**
     * Imputes the values using mean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    imputeMeanColumn(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const script = "when(or(isnull(" + fieldName + "),isnan(" + fieldName + ")),mean(" + fieldName + ").over(orderBy(1))).otherwise(" + fieldName + ").as(\"" + fieldName + "\")";
        const formula = ColumnUtil.toFormula(script, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: 'Impute mean ' + ColumnUtil.getColumnDisplayName(column)});
    }

    /**
     * Crosstab against another column
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    crosstabColumn(column: any, grid: any) {
        let form = new CrossTabForm(column, grid, this.controller)
        this.dialog.openColumnForm(form);
    }

    /**
     * Extract array item into a new column
     * @param column
     * @param grid
     */
    extractArrayItem(column: any, grid: any) {

        let form = new ExtractIndexForm(column, grid, this.controller);
        this.dialog.openColumnForm(form);
    }

    /**
     * Extract array items into columns
     * @param column
     * @param grid
     */
    extractArrayItems(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);
        let count = 0;

        // Sample rows determine how many array elements
        if (grid.rows != null && grid.rows.length > 0) {
            let idx: number = 0;
            _.each(grid.columns, (col: any, key: number) => {
                if (col.name == fieldName) {
                    idx = key;
                }
                //TODO revisit and break out of each loop
            })
            _.each(grid.rows, row => {
                count = (row[idx] != null && row[idx].length > count ? row[idx].length : count)
            });
        }
        var columns = []
        for (let i = 0; i < count; i++) {
            let newFieldName = fieldName + "_" + i;
            columns.push(`getItem(${fieldName}, ${i}).as("${newFieldName}")`);
        }
        var formula = ColumnUtil.generateMoveScript(fieldName, columns, grid, false);
        this.controller.pushFormula(formula, {formula: formula, icon: "functions", name: "Extract array"}, true, true);
    }

    /**
     * Adds string labels to indexes
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    indexColumn(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);
        const newFieldName = fieldName + "_indexed";
        const formula = `StringIndexer().setInputCol("${fieldName}").setOutputCol("${newFieldName}").run(select(${fieldName}))`;
        const moveFormula = ColumnUtil.generateMoveScript(fieldName, [newFieldName], grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        this.controller.setChainedQuery(chainedOp);

        this.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Index ' + ColumnUtil.getColumnDisplayName(column)}, true, false)
            .then( () => {
                chainedOp.nextStep();
                this.controller.addFunction(moveFormula, {formula: formula, icon: 'functions', name: 'Move new column next to ' + fieldName});
            })
    }

    /**
     * Vectorize a numeric column as a double array
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    vectorizeColumn(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);

        this.controller.extractColumnStatistics(fieldName).then((profileData: ColumnProfile) => {
            if (profileData.percNull > 0) {
                this.controller.displayError("Error", "Column must be clean of empty/NaN to use this function");
                return;
            }
            const tempField = ColumnUtil.createTempField();
            const formula = `vectorAssembler(["${fieldName}"], "${tempField}")`;
            let renameScript = ColumnUtil.generateRenameScript(fieldName, tempField, grid);

            // Two part conversion
            let chainedOp: ChainedOperation = new ChainedOperation(2);
            this.controller.setChainedQuery(chainedOp);
            this.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Vectorize ' + ColumnUtil.getColumnDisplayName(column)}, true, false)
                .then(() => {
                    chainedOp.nextStep();
                    this.controller.addFunction(renameScript, {formula: formula, icon: 'functions', name: 'Remap temp vector column to ' + fieldName});
                })
        });

    }

    /**
     * Apply logit transform
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    logitTransform(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);
        const script = `ln(${fieldName}/(1-${fieldName}))`;
        const formula = ColumnUtil.toFormula(script, column, grid);
        this.controller.addFunction(formula, {
            formula: formula, icon: "functions",
            name: "Logit transform " + ColumnUtil.getColumnDisplayName(column)
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
    rescaleColumnML(column: any, grid: any, mean: boolean, stdDev: boolean) {

        const fieldName = ColumnUtil.getColumnFieldName(column);
        const tempField = ColumnUtil.createTempField();
        const formula = `StandardScaler().setInputCol("${fieldName}").setOutputCol("${tempField}").setWithMean(${mean}).setWithStd(${stdDev}).run(select(${fieldName}))`;
        let renameScript = ColumnUtil.generateRenameScript(fieldName, tempField, grid);

        // Two part conversion
        let chainedOp: ChainedOperation = new ChainedOperation(2);
        this.controller.setChainedQuery(chainedOp);
        this.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Std Dev. rescale ' + ColumnUtil.getColumnDisplayName(column)}, true, false)
            .then(function () {
                chainedOp.nextStep();
                this.controller.addFunction(renameScript, {formula: formula, icon: 'functions', name: 'Remap temp rescaled column to ' + fieldName});
            })
    }

    /**
     * Calculate outliers 1 (outlier) or 0 (not outlier)
     * @param column
     * @param grid
     * @returns {any}
     */
    identifyOutliers(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        this.controller.extractColumnStatistics(fieldName).then((profileData: ColumnProfile) => {
            if (profileData.percNull > 0) {
                this.controller.displayError("Error", "Column must be clean of empty/NaN to use this function");
                return;
            }
            let quantileStats = ColumnUtil.approxQuantileFormula(fieldName, 4);
            this.controller.extractFormulaResult(quantileStats, 10000)
                .then((value: any) => {
                    const Q1 = value[0];
                    const Q3 = value[2];
                    const IQR = Q3 - Q1;

                    let lower = (Q1 - (1.5 * IQR));
                    let upper = (Q3 + (1.5 * IQR));
                    let script = `when(or(${fieldName} < ${lower},${fieldName}>${upper}),1).otherwise(0)`
                    const formula = ColumnUtil.toAppendColumnFormula(script, column, grid, `${fieldName}_outlier`);
                    this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Find outliers ${fieldName}`});
                });
        });
    }

    /**
     * Bin values of the column into discrete quantiles
     * @param column
     * @param grid
     */
    binValues(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        this.controller.extractColumnStatistics(fieldName).then((profileData: ColumnProfile) => {
            if (profileData.percNull > 0) {
                this.controller.displayError("Error", "Column must be clean of empty/NaN to use this function");
                return;
            }
            let form = new BinValuesForm(column, grid, this.controller)
            this.dialog.openColumnForm(form);
        });

    }

    rescaleMinMax(column: any, grid: any) {

        let form = new RescaleForm(column, grid, this.controller)
        this.dialog.openColumnForm(form);
    }

    /**
     * Rescale the vector column using the standard deviation
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleStdDevColumn(column: any, grid: any) {
        this.rescaleColumnML(column, grid, false, true);
    }

    /**
     * Rescale the vector column using the mean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleMeanColumn(column: any, grid: any) {
        this.rescaleColumnML(column, grid, true, false);
    }

    /**
     * Rescale using mean and stdDev
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    rescaleBothMethodsColumn(column: any, grid: any) {
        this.rescaleColumnML(column, grid, true, true);
    }

    /**
     * Fill missing strings using a fill-forward method provided a grouping column and ordering
     * @param column
     * @param grid
     */
    fillForwardColumn(column: any, grid: any) {

        let form = new FillForwardForm(column, grid, this.controller)
        this.dialog.openColumnForm(form);
    }


    /**
     * Flattens a struct column into multiple fields (one-level)
     * @param self
     * @param column
     * @param grid
     */
    flattenStructColumn(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);

        let structFields = ColumnUtil.structToFields(column);
        let existingCols = ColumnUtil.toColumnArray(grid.columns);

        let formula: string;

        // Generate fields with unique names
        let existingColsLower = existingCols.map((field: string) => {
            return field.toLowerCase();
        });
        let fieldParts = structFields.map((field: string) => {
            let alias = ColumnUtil.uniqueName(existingColsLower, field);
            existingColsLower.push(alias.toLowerCase());
            return (`getField(${fieldName},"${field}").as("${alias}")`);
        });

        // Insert new fields into the field list
        let idxOfCurrentColumn = existingCols.indexOf(fieldName);
        //TODO revisit formulaFields datatype!!!!
        let formulaFields: string[][] = []
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

        this.controller.addFunction(formula, {
            formula: formula, icon: "functions",
            name: "Flatten " + fieldName
        });

    }

    /**
     * Extract numerical values from string
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractNumeric(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        let script = `regexp_replace(${fieldName}, "[^0-9\-\\\\.]+","").as('${fieldName}')`;

        const formula = ColumnUtil.toFormula(script, column, grid);
        this.controller.addFunction(formula, {
            formula: formula, icon: "filter_2",
            name: "Extract numeric from " + ColumnUtil.getColumnDisplayName(column)
        });

    }

    /**
     * Negate a boolean
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    negateBoolean(column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        let script = `not(${fieldName}).as("${fieldName}")`;

        const formula = ColumnUtil.toFormula(script, column, grid);
        this.controller.addFunction(formula, {
            formula: formula, icon: "exposure",
            name: "Negate boolean from " + ColumnUtil.getColumnDisplayName(column)
        });

    }

    /**
     * One hot encode categorical values
     *
     * @param {ui.grid.GridColumn} column the column to be hidden
     * @param {ui.grid.Grid} grid the grid with the column
     */
    oneHotEncodeColumn(column: any, grid: any) {

        const fieldName = ColumnUtil.getColumnFieldName(column);

        // Chain three calls: 1) clean values as valid column names 2) execute pivot 3) replace null with empty (due to spark2 pivot behavior)
        const tempField = ColumnUtil.createTempField();
        const cleanFormula = ColumnUtil.createCleanFieldFormula(fieldName, tempField);

        // Generate group by and pivot formula from all the columns
        let cols: string[] = ColumnUtil.toColumnArray(grid.columns);

        let colString: string = cols.join();
        const formula = `groupBy(${colString}).pivot("${tempField}").agg(when(count(${tempField})>0,1).otherwise(0))`;

        let chainedOp: ChainedOperation = new ChainedOperation(3);
        this.controller.setChainedQuery(chainedOp);

        this.controller.pushFormula(cleanFormula, {formula: cleanFormula, icon: 'functions', name: 'Clean one hot field ' + fieldName}, true, false)
            .then(() => {
                chainedOp.nextStep();
                this.controller.pushFormula(formula, {formula: formula, icon: 'functions', name: 'One hot encode ' + fieldName}, true, false)
                    .then(function () {
                        // Now we need to fill in the null values with zero for our new cols
                        let allcols: string[] = ColumnUtil.toColumnArray(this.controller.engine.getCols());
                        let select: string[] = CloneUtil.deepCopy(cols);
                        let idx: number = cols.length - 1;
                        _.each(allcols, (col, index) => {
                            if (index > idx) {
                                select.push(`coalesce(${col},0).as("${col}")`);
                            }
                        });
                        let selectString = select.join();
                        let fillNAFormula = `select(${selectString})`
                        chainedOp.nextStep();
                        this.controller.addFunction(fillNAFormula, {formula: fillNAFormula, icon: 'functions', name: 'Fill NA'});
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
        let renameForm = new RenameColumnForm(column, grid, this.controller);
        this.dialog.openColumnForm(renameForm);
    }

    /**
     * Sets the domain type for the specified column.
     */
    setDomainType(column: any, domainTypeId: string) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
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
        const displayName = ColumnUtil.getColumnDisplayName(column);
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const pattern = "[" + StringUtils.singleQuote(value).replace(/]/g, "\\]") + "]";
        const formula = ColumnUtil.toFormula(`split(when(isnull(${fieldName}),"").otherwise(${fieldName}), '${pattern}').as("${displayName}")`, column, grid);
        this.controller.addFunction(formula, {formula: formula, icon: "call_split", name: "Split " + ColumnUtil.getColumnDisplayName(column) + " on " + value});
    }

    /**
     * Executes the specified operation on the column.
     *
     * @param {Object} transform the transformation object from {@link VisualQueryColumnDelegate#getTransforms}
     * @param {ui.grid.GridColumn} column the column to be transformed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    transformColumn(transform: MenuItem, column: any, grid: any) {
        const fieldName = ColumnUtil.getColumnFieldName(column);
        const self = this;
        if (transform.operationFn) {
            transform.operationFn.bind(this)(column, grid);
        }
        else {
            const script = transform.operation + "(" + fieldName + ").as(\"" + StringUtils.singleQuote(fieldName) + "\")";
            const formula = ColumnUtil.toFormula(script, column, grid);
            const name = (transform.description ? transform.description : transform.name) + " " + ColumnUtil.getColumnDisplayName(column);
            this.controller.addFunction(formula, {formula: formula, icon: transform.icon, name: name});
        }
    }

    /**
     * Replace all NaN with a fixed value
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    replaceNaNWithValue(column: any, grid: any) {

        let replaceForm = new ReplaceNanForm(column, grid, this.controller);
        this.dialog.openColumnForm(replaceForm);
    }

    /**
     * Replace empty with a fixed value
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    replaceMissing(column: any, grid: any) {
        let fieldName = ColumnUtil.getColumnFieldName(column);

        let replaceMissing = new ReplaceValueForm(column, grid, this.controller, '');
        this.dialog.openColumnForm(replaceMissing);
    }

    /**
     * Round numeric to specified digits
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    roundNumeric(column: any, grid: any) {

        let form = new RoundNumberForm(column, grid, this.controller)
        this.dialog.openColumnForm(form);
    }

    /**
     * Extract regex
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractRegexPattern(column: any, grid: any) {

        let form = new ExtractRegexForm(column, grid, this.controller, this)
        this.dialog.openColumnForm(form);
    }

    /**
     * Extract between two delimiters
     *
     * @param {ui.grid.GridColumn} column the column to be renamed
     * @param {ui.grid.Grid} grid the grid with the column
     */
    extractDelimiters(column: any, grid: any) {

        let form = new ExtractDelimsForm(column, grid, this.controller)
        this.dialog.openColumnForm(form);
    }

    // Executes the regex formula
    private executeRegex(column: any, grid: any, regex: string, group: number) {
        let fieldName = ColumnUtil.getColumnFieldName(column);
        const script = `regexp_extract(${fieldName}, "${regex}", ${group}).as("${fieldName}")`
        const formula = ColumnUtil.toFormula(script, column, grid);
        this.controller.addFunction(formula, {
            formula: formula, icon: "content_cut",
            name: `Extract regex from ${fieldName}`
        });
    }


    /**
     * Provides a dialog for capturing order by information and returns the orderBy clause
     */
    orderByDialog(column: any, grid: any, title: string, cb: Function) {

        let form = new OrderByForm(column, grid, this.controller, cb);
        this.dialog.openColumnForm(form);

    }


    /**
     * % difference from previous value
     */
    percDiffFromPrevious(column: any, grid: any) {
        this.orderByDialog(column, grid, "Percentage Difference From Previous", (orderBy: string) => {
            let fieldName = ColumnUtil.getColumnFieldName(column);
            let script = `((${fieldName}-lag(${fieldName},1).over(orderBy(${orderBy})))/(lag(${fieldName},1).over(orderBy(${orderBy})))*100)`;
            const formula = ColumnUtil.toAppendColumnFormula(script, column, grid, `${fieldName}_diffp`);
            this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `% diff from prev ${fieldName}`});
        });
    }

    /**
     * Absolute difference from previous value
     */
    diffFromPrevious(column: any, grid: any) {

        this.orderByDialog(column, grid, "Difference From Previous", (orderBy: string) => {
            let fieldName = ColumnUtil.getColumnFieldName(column);
            let script = `(${fieldName}-lag(${fieldName},1).over(orderBy(${orderBy})))`;
            const formula = ColumnUtil.toAppendColumnFormula(script, column, grid, `${fieldName}_diff`);
            this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Difference from prev ${fieldName}`});
        });
    }

    /**
     * Create a running average
     */
    runningAverage(column: any, grid: any) {

        this.orderByDialog(column, grid, "Running Average", (orderBy: string) => {
            let fieldName = ColumnUtil.getColumnFieldName(column);
            let script = `(avg(${fieldName}).over(orderBy(${orderBy}).rowsBetween(-2147483647,0)))`;
            const formula = ColumnUtil.toAppendColumnFormula(script, column, grid, `${fieldName}_ravg`);
            this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Running average ${fieldName}`});
        });

    }

    /**
     * Create a running total
     */
    runningTotal(column: any, grid: any) {

        this.orderByDialog(column, grid, "Running Total", (orderBy: string) => {
            let fieldName = ColumnUtil.getColumnFieldName(column);
            let script = `(sum(${fieldName}).over(orderBy(${orderBy}).rowsBetween(-2147483647,0)))`;
            const formula = ColumnUtil.toAppendColumnFormula(script, column, grid, `${fieldName}_rtot`);
            this.controller.addFunction(formula, {formula: formula, icon: 'functions', name: `Running total ${fieldName}`});
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
     * Gets the filters for a column based on category.
     *
     * @param dataCategory - the category for the column
     * @returns the filters for the column
     */
    protected getFilters(dataCategory: DataCategory): object[] {
        const filters = [];

        switch (dataCategory) {
            case DataCategory.STRING:
                filters.push({condition: this.uiGridConstants.filter.CONTAINS, icon: 'mdi mdi-file-find', label: 'Contains...'});
            // fall through

            case DataCategory.NUMERIC:
                filters.push({
                        condition: this.uiGridConstants.filter.LESS_THAN, icon: 'mdi mdi-code-less-than',
                        label: 'Less than...'
                    },
                    {
                        condition: this.uiGridConstants.filter.GREATER_THAN, icon: 'mdi mdi-code-greater-than',
                        label: 'Greater than...'
                    },
                    {condition: this.uiGridConstants.filter.EXACT, icon: 'mdi mdi-code-equal', label: 'Equal to...'});
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
    protected getTransforms(dataCategory: DataCategory): MenuItems {

        let transforms = new MenuItems();

        const self = this;

        if (dataCategory === DataCategory.NUMERIC) {

            transforms.replace.push(
                {description: 'Replace empty with a specified value', icon: 'find_replace', name: 'Missing values...', operationFn: self.replaceMissing}
            );
            transforms.ml.push(
                {description: 'Bin values', name: 'Bin values...',icon:'mdi mdi-group', operationFn: self.binValues},
                {description: 'Identify outliers',  name: 'Identify outliers',icon:'mdi mdi-chart-histogram', operationFn: self.identifyOutliers},
                {description: 'Impute missing with mean',  name: 'Impute using mean...',icon:'mdi mdi-basket-fill', operationFn: self.imputeMeanColumn},
                {description: 'Replace empty with a specified value',  name: 'Replace missing...',icon:'mdi mdi-find-replace', operationFn: self.replaceMissing},
                {description: 'Rescale min/max',  name: 'Rescale min/max...',icon:'mdi mdi-function-variant', operationFn: self.rescaleMinMax},
                {description: 'Convert to a numerical array for ML',  name: 'Vectorize',icon:'mdi mdi-matrix', operationFn: self.vectorizeColumn}
            );
            transforms.format.push(
                {description: 'Round up',  name: 'Round up',icon:'mdi mdi-format-vertical-align-top', operation: 'ceil'},
                {description: 'Round down',  name: 'Round down',icon:'mdi mdi-format-vertical-align-bottom', operation: 'floor'},
                {description: 'Round number',  name: 'Round...',icon:'mdi mdi-numeric-0', operationFn: self.roundNumeric});

            transforms.calculate.push(
                {description: 'Degrees of',  name: 'To Degrees',icon:'', operation: 'toDegrees'},
                {description: 'Radians of',  name: 'To Radians',icon:'', operation: 'toRadians'},
                {description: 'Log',  name: 'Log10',icon:'', operation: 'log10'},
                {description: 'Logit transform', name: 'Logit',icon:'', operationFn: self.logitTransform},
                {description: 'Running average',  name: 'Running average...',icon:'', operationFn: self.runningAverage},
                {description: 'Running total',  name: 'Running total...',icon:'', operationFn: self.runningTotal},
                {description: 'Difference from previous', name: 'Difference from prev value...',icon:'', operationFn: self.diffFromPrevious},
                {description: '% difference', name: '% Difference from prev value...',icon:'', operationFn: self.percDiffFromPrevious}
            );
            transforms.other.push(
                {description: 'Crosstab', icon: 'poll', name: 'Crosstab', operationFn: self.crosstabColumn},
            );
        }
        else if (dataCategory === DataCategory.STRING) {

            transforms.format.push({description: 'Lowercase', icon: 'mdi mdi-format-letter-case-lower', name: 'lowercase', operation: 'lower'},
                {description: 'Uppercase', icon: 'mdi mdi-format-letter-case-upper', name: 'UPPERCASE', operation: 'upper'},
                {description: 'Title case', icon: 'mdi mdi-format-letter-case', name: 'TitleCase', operation: 'initcap'},
                {description: 'Trim whitespace', icon: 'mdi mdi-playlist-remove', name: 'Trim', operation: 'trim'},
                {description: 'Left pad', icon: 'mdi mdi-format-align-right', name: 'Left pad', operationFn: self.leftPad}
            );

            transforms.extract.push(
                {description: 'Extract numeric', name: 'Numbers',icon:'mdi mdi-numeric', operationFn: self.extractNumeric},
                {description: 'Extract regex',  name: 'Regex pattern',icon:'mdi mdi-regex', operationFn: self.extractRegexPattern},
                {description: 'Extract delimiters', name: 'Between delimiters',icon:'mdi mdi-code-braces', operationFn: self.extractDelimiters},
            );

            transforms.ml.push(
                {description: 'Fill-forward missing values', name: 'Fill-forward missing...',icon:'mdi mdi-basket-fill', operationFn: self.fillForwardColumn},
                {description: 'Index labels', name: 'Index labels',icon:'mdi mdi-label-outline', operationFn: self.indexColumn},
                {description: 'One hot encode (or pivot) categorical values',  name: 'One hot encode',icon:'mdi mdi-matrix', operationFn: self.oneHotEncodeColumn},
                {description: 'Replace NAN with a specified value', name: 'Replace NaN...',icon:'mdi mdi-null', operationFn: self.replaceNaNWithValue},
                {description: 'Replace empty with a specified value',  name: 'Replace missing...',icon:'mdi mdi-find-replace', operationFn: self.replaceMissing});

            transforms.other.push(
                {description: 'Crosstab', icon: 'poll', name: 'Crosstab...', operationFn: self.crosstabColumn});

        } else if (dataCategory === DataCategory.ARRAY) {
            transforms.defaults.push(
                {description: 'Extract to columns',  name: 'Extract to columns',icon:'mdi mdi-table-column-plus-after', operationFn: self.extractArrayItems},
                {description: 'Extract item to column',  name: 'Extract item...',icon:'mdi mdi-table-column', operationFn: self.extractArrayItem},
                {description: 'Convert array elements to rows',  name: 'Explode to rows',icon:'mdi mdi-table-row', operation: 'explode'},
                {description: 'Sort', name: 'Sort array',icon:'mdi mdi-sort', operation: 'sort_array'}
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
            transforms.defaults.push({description: 'Flatten struct', name: 'Flatten struct',icon:'mdi mdi-table-column-plus-after', operationFn: self.flattenStructColumn});
        }
        else if (dataCategory === DataCategory.MAP) {
            transforms.defaults.push({description: 'Explode array to rows', name: 'Explode',icon:'mdi mdi-table-row', operation: 'explode'});
        } else if (dataCategory === DataCategory.BOOLEAN) {
            transforms.defaults.push({description: 'Flip boolean',  name: 'Negate boolean',icon:'mdi-swap-vertical', operationFn: self.negateBoolean});
        }
        return transforms;
    }


}

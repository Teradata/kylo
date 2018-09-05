import {ColumnProfile} from "./api/column-profile";
import {ChainedOperation} from "./column-delegate";
import {PageSpec} from "./index";

export interface ColumnController {

    /**
     * List of field policies for current transformation.
     */
    fieldPolicies: any[];

    /**
     * Adds the specified formula to the current script and refreshes the table data.
     *
     * @param {string} formula the formula
     * @param {TransformContext} context the UI context for the transformation
     */
    addFunction(formula: any, context: any): Promise<{}>

    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     * @param {TransformContext} context - the UI context for the transformation
     * @param {boolean} doQuery - true to immediately execute the query
     * @param {boolean} refreshGrid - whether to refresh grid
     */
    pushFormula(formula: any, context: any, doQuery?: boolean, refreshGrid?: boolean): Promise<{}>;

    /**
     * Adds the column filter to the grid
     */
    addColumnFilter(filter: any, column: any, query?: boolean): Promise<{}>;

    addColumnSort(direction: string, column: any, query?: boolean): Promise<{}>;

    /**
     * Generates column statistics
     * @param {string} fieldName the fieldname
     * @returns {Promise<ColumnProfile>} profile data
     */
    extractColumnStatistics(fieldName: string): Promise<ColumnProfile>;

    /**
     * Executes a query returning a single value
     * @param {string} formula
     * @param {number} sample number of sample rows
     */
    extractFormulaResult(formula: string, sample: number): Promise<any>;

    /**
     * Show column statistics for provided field
     * @param {string} fieldName the fieldname
     */
    showAnalyzeColumn(fieldName: string): any;

    /**
     * Sets the domain type for the specified field.
     *
     * @param columnIndex - the field index
     * @param domainTypeId - the domain type id
     */
    setDomainType(columnIndex: number, domainTypeId: string): void;

    /**
     * Perform the latest query
     * @param {boolean} refresh true if refresh refresh the table
     * @param {PageSpec} pageSpec specifies the number of rows and columns to return
     * @param {boolean} doValidate whether to perform the validate stage
     * @param {boolean} doProfile whether to perform the profile stage
     * @returns {angular.IPromise<any>}
     */
    query(refresh?: boolean, pageSpec ?: PageSpec, doValidate ?: boolean, doProfile ?: boolean): Promise<any>;

    /**
     * Sets a chain of formulas to be executed in sequential order
     * @param {ChainedOperation} chainedOp
     */
    setChainedQuery(chainedOp : ChainedOperation): void;

    /**
     * Display error message
     */
    displayError(title:string, msg:string): void;
}

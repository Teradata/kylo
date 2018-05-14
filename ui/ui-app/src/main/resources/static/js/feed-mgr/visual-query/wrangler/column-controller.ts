import {IPromise} from "angular";
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
    addFunction(formula: any, context: any): IPromise<{}>

    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     * @param {TransformContext} context - the UI context for the transformation
     * @param {boolean} doQuery - true to immediately execute the query
     * @param {boolean} refreshGrid - whether to refresh grid
     */
    pushFormula(formula: any, context: any, doQuery?: boolean, refreshGrid?:boolean): IPromise<{}>;

    /**
     * Adds the column filter to the grid
     * @param filter
     * @param column
     */
    addColumnFilter(filter: any, column: any, query ?: boolean) :IPromise<{}>;

    /**
     *
     * @param {string} direction
     * @param column
     * @param {boolean} query
     * @return {angular.IPromise<any>}
     */
    addColumnSort(direction:string,column:any,query?:boolean) : IPromise<{}>;



    showAnalyzeColumn(fieldName: string) : any;

    /**
     * Sets the domain type for the specified field.
     *
     * @param columnIndex - the field index
     * @param domainTypeId - the domain type id
     */
    setDomainType(columnIndex: number, domainTypeId: string): void;

    /**
     * Perform the latest query
     * @param {boolean} true if refresh refresh the table
     * @param {PageSpec} pageSpec specifies the number of rows and columns to return
     * @param {boolean} doValidate whether to perform the validate stage
     * @param {boolean} doProfile whether to perform the profile stage
     * @returns {angular.IPromise<any>}
     */
    query(refresh ?: boolean, pageSpec ?: PageSpec, doValidate ?: boolean, doProfile ?: boolean) : IPromise<any>;
}

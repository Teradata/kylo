import {IPromise} from "angular";

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

    showAnalyzeColumn(fieldName: string) : any;

    /**
     * Sets the domain type for the specified field.
     *
     * @param columnIndex - the field index
     * @param domainTypeId - the domain type id
     */
    setDomainType(columnIndex: number, domainTypeId: string): void;
}

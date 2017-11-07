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
    addFunction(formula: any, context: any): void

    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     * @param {TransformContext} context - the UI context for the transformation
     * @param {boolean} doQuery - true to immediately execute the query
     */
    pushFormula(formula: any, context: any): void;
    pushFormula(formula: any, context: any, doQuery: boolean): void;

    /**
     * Sets the domain type for the specified field.
     *
     * @param columnIndex - the field index
     * @param domainTypeId - the domain type id
     */
    setDomainType(columnIndex: number, domainTypeId: string): void;
}

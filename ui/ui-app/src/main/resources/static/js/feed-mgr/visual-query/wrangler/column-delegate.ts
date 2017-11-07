import * as angular from "angular";
import {ColumnController} from "./column-controller";

/**
 * Categories for data types.
 */
export enum DataCategory {
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
    ARRAY: 'array',
    MAP: 'map',
    STRUCT: 'struct',
    UNION: 'uniontype'
};

/**
 * Handles operations on columns.
 */
export class ColumnDelegate {

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
    constructor(public dataType: string, public controller: ColumnController, protected $mdDialog: angular.material.IDialogService, protected uiGridConstants: any) {
        this.dataCategory = this.fromDataType(dataType);
        this.filters = this.getFilters(this.dataCategory);
        this.transforms = this.getTransforms(this.dataCategory);
    }

    /**
     * Filters for rows where the specified column is not null.
     */
    deleteNullRows(column: any) {
        const formula = "filter(not(isnull(" + this.getColumnFieldName(column) + ")))";
        this.controller.addFunction(formula, {formula: formula, icon: "≠", name: "Delete " + this.getColumnDisplayName(column) + " if null"});
    }

    /**
     * Filters for rows where the specified column does not contain the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsContaining(value: string, column: any) {
        const formula = "filter(not(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.quote(value) + "')))";
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Delete " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is not the specified value.
     *
     * @param value - the value to remove
     * @param column - the column
     */
    deleteRowsEqualTo(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " != '" + StringUtils.quote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "≠", name: "Delete " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is less than or equal to the specified value.
     *
     * @param value - the maximum value (inclusive)
     * @param column - the column
     */
    deleteRowsGreaterThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " <= '" + StringUtils.quote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "≯", name: "Delete " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is greater than or equal to the specified value.
     *
     * @param value - the minimum value (inclusive)
     * @param column - the column
     */
    deleteRowsLessThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " >= '" + StringUtils.quote(value) + "')";
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
        const formula = "filter(contains(" + this.getColumnFieldName(column) + ", '" + StringUtils.quote(value) + "'))";
        this.controller.addFunction(formula, {formula: formula, icon: "search", name: "Find " + this.getColumnDisplayName(column) + " containing " + value});
    }

    /**
     * Filters for rows where the specified column is the specified value.
     *
     * @param value - the value to find
     * @param column - the column
     */
    findRowsEqualTo(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " == '" + StringUtils.quote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "=", name: "Find " + this.getColumnDisplayName(column) + " equal to " + value});
    }

    /**
     * Filters for rows where the specified column is greater than the specified value.
     *
     * @param value - the minimum value (exclusive)
     * @param column - the column
     */
    findRowsGreaterThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " > '" + StringUtils.quote(value) + "')";
        this.controller.addFunction(formula, {formula: formula, icon: "keyboard_arrow_right", name: "Find " + this.getColumnDisplayName(column) + " greater than " + value});
    }

    /**
     * Filters for rows where the specified column is less than the specified value.
     *
     * @param value - the maximum value (exclusive)
     * @param column - the column
     */
    findRowsLessThan(value: string, column: any) {
        const formula = "filter(" + this.getColumnFieldName(column) + " < '" + StringUtils.quote(value) + "')";
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

        const formula = "drop(\"" + StringUtils.quote(column.headerTooltip) + "\")";
        this.controller.pushFormula(formula, {formula: formula, icon: "remove_circle", name: "Hide " + this.getColumnDisplayName(column)});
        this.controller.fieldPolicies = this.controller.fieldPolicies.filter((value, index) => index == column.index);

        grid.onColumnsChange();
        grid.refresh();
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
            const script = self.getColumnFieldName(column) + ".as(\"" + StringUtils.quote(name) + "\")";
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
        this.controller.setDomainType(column.index, domainTypeId);
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
        const pattern = "[" + StringUtils.quote(value).replace(/]/g, "\\]") + "]";
        const formula = this.toFormula("split(" + fieldName + ", '" + pattern + "').as(\"" + StringUtils.quote(displayName) + "\")", column, grid);
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
        const script = transform.operation + "(" + fieldName + ").as(\"" + StringUtils.quote(fieldName) + "\")";
        const formula = this.toFormula(script, column, grid);
        const name = (transform.description ? transform.description : transform.name) + " " + this.getColumnDisplayName(column);
        this.controller.addFunction(formula, {formula: formula, icon: transform.icon, name: name});
    }

    /**
     * Validates the specified filter.
     *
     * @param {Object} filter the filter to be validated
     * @param {VisualQueryTable} table the visual query table
     */
    validateFilter(filter: any, table: any) {
        if (filter.term == "") {
            filter.term = null;
        } else {
            delete filter.regex;
        }
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

            case DataType.ARRAY:
                return DataCategory.ARRAY;

            case DataType.MAP:
                return DataCategory.MAP;

            case DataType.STRUCT:
                return DataCategory.STRUCT;

            case DataType.UNION:
                return DataCategory.UNION;
        }
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

        if (dataCategory === DataCategory.ARRAY) {
            transforms.push({icon: 'call_split', name: 'Explode', operation: 'explode'},
                {description: 'Sort', icon: 'sort', name: 'Sort Array', operation: 'sort_array'});
        }
        if (dataCategory === DataCategory.BINARY) {
            transforms.push({icon: '#', name: 'CRC32', operation: 'crc32'},
                {icon: '#', name: 'MD5', operation: 'md5'},
                {icon: '#', name: 'SHA1', operation: 'sha1'},
                {icon: '#', name: 'SHA2', operation: 'sha2'});
        }
        if (dataCategory === DataCategory.DATETIME) {
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
        if (dataCategory === DataCategory.MAP) {
            transforms.push({icon: 'call_split', name: 'Explode', operation: 'explode'});
        }
        if (dataCategory === DataCategory.NUMERIC) {
            transforms.push({description: 'Ceiling of', icon: 'arrow_upward', name: 'Ceiling', operation: 'ceil'},
                {description: 'Floor of', icon: 'arrow_downward', name: 'Floor', operation: 'floor'},
                {icon: 'swap_vert', name: 'Round', operation: 'round'},
                {descriptions: 'Degrees of', icon: '°', name: 'To Degrees', operation: 'toDegrees'},
                {descriptions: 'Radians of', icon: '㎭', name: 'To Radians', operation: 'toRadians'});
        }
        if (dataCategory === DataCategory.STRING) {
            transforms.push({description: 'Lowercase', icon: 'arrow_downward', name: 'Lower Case', operation: 'lower'},
                {description: 'Title case', icon: 'format_color_text', name: 'Title Case', operation: 'initcap'},
                {icon: 'graphic_eq', name: 'Trim', operation: 'trim'},
                {description: 'Uppercase', icon: 'arrow_upward', name: 'Upper Case', operation: 'upper'});
        }

        return transforms;
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

import {DataCategory,DataType} from "../column-delegate";
import * as _ from "underscore";

export class ColumnUtil {




    /**
     * Converts from the specified data type to a category.
     *
     * @param dataType - the data type
     * @returns the data category
     */
    public static fromDataType(dataType: string): DataCategory {
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
     * Creates a formula that replaces the specified column with the specified script.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    static toFormula(script: string, column: any, grid: any): string {

        const columnFieldName = this.getColumnFieldName(column);
        let formula = "";
        const self = this;

        _.each(grid.columns,  (item:any) =>{
            if (item.visible) {
                const itemFieldName = ColumnUtil.getColumnFieldName(item);
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += (itemFieldName === columnFieldName) ? script : itemFieldName;
            }
        });

        formula += ")";
        return formula;
    }

    /**
     * Creates a formula for cleaning values as future fieldnames
     * @returns {string} a formula for cleaning row values as fieldnames
     */
    static createCleanFieldFormula(fieldName: string, tempField: string): string {
        return `when(startsWith(regexp_replace(substring(${fieldName},0,1),"[0-9]","***"),"***"),concat("c_",lower(regexp_replace(${fieldName},"[^a-zA-Z0-9_]+","_")))).otherwise(lower(regexp_replace(${fieldName},"[^a-zA-Z0-9_]+","_"))).as("${tempField}")`;
    }


    /**
     * Gets the SQL identifier for the specified column.
     */
    static getColumnFieldName(column: any): string {
        return column.field || column.name;
    }

    /**
     * Gets the human-readable name of the specified column.
     */
    static getColumnDisplayName(column: any): string {
        return column.displayName;
    }



    static approxQuantileFormula(fieldName:string, bins:number) {
        let binSize = 1 / bins;
        let arr = []
        for (let i = 1; i < bins; i++) {
            arr.push(i * binSize)
        }
        return `select(approxQuantile("${fieldName}", [${arr}], 0.0).as("data"))`

    }


    /**
     * Creates a formula that adds a new column with the specified script. It generates a unique column name.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    static toAppendColumnFormula(script: string, column: any, grid: any, newField ?: string): string {
        const self = this;
        const columnFieldName = ColumnUtil.getColumnFieldName(column);
        const uniqueName = (newField == null ? ColumnUtil.toUniqueColumnName(grid.columns, columnFieldName) : newField);
        return ColumnUtil.createAppendColumnFormula(script, column, grid, uniqueName);
    }

    /**
     * Creates a formula that adds a new column with the specified script.
     *
     * @param {string} script the expression for the column
     * @param {ui.grid.GridColumn} column the column to be replaced
     * @param {ui.grid.Grid} grid the grid with the column
     * @returns {string} a formula that replaces the column
     */
    static createAppendColumnFormula(script: string, column: any, grid: any, newField: string): string {
        const self = this;
        const columnFieldName = ColumnUtil.getColumnFieldName(column);
        let formula = "";

        _.each(grid.columns,  (item:any, idx:number) => {
            if (item.visible) {
                const itemFieldName = ColumnUtil.getColumnFieldName(item);
                formula += (formula.length == 0) ? "select(" : ", ";
                formula += itemFieldName;
                if (itemFieldName == columnFieldName) {
                    formula += "," + script + ColumnUtil.toAliasClause(newField);
                }
            }
        });

        formula += ")";
        return formula;
    }


    /**
     * Generates a script to move the column B directly to the right of column A
     * @returns {string}
     */
    static generateMoveScript(fieldNameA: string, fieldNameB: string | string[], columnSource: any, keepFieldNameA: boolean = true): string {
        var self = this;
        let cols: string[] = [];
        let sourceColumns = (columnSource.columns ? columnSource.columns : columnSource);
        _.each(sourceColumns, col => {
            let colName: string = ColumnUtil.getColumnFieldName(col);
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
     * Returns the as alias clause
     * @param columns column list
     * @returns {string} a unique fieldname
     */
    static toAliasClause(name: string): string {
        return ".as(\"" + name + "\")"
    }

    /**
     * Creates a guaranteed unique field name
     * @param columns column list
     * @returns {string} a unique fieldname
     */
    static toUniqueColumnName(columns: Array<any>, columnFieldName: any): string {
        let prefix = "new_";
        let idx = 0;
        let columnSet = new Set();
        let uniqueName = null;
        const self = this;
        columnSet.add(columnFieldName);
        _.each(columns,  (item:any)=> {
            columnSet.add(ColumnUtil.getColumnFieldName(item));
        });

        while (uniqueName == null) {
            let name = prefix + idx;
            uniqueName = (columnSet.has(name) ? null : name);
            idx++;
        }
        return uniqueName;
    }

    /**
     * Guaranteed to return a unique column name that conforms to the field naming requirements
     * @param {Array<string>} columns
     * @param {string} columnFieldName
     * @param {number} idx
     * @returns {string}
     */
   static uniqueName(columns: Array<string>, columnFieldName: string, idx: number = -1): string {

        if (columns == null || columns.length == 0) {
            return columnFieldName;
        }
        let alias = columnFieldName.replace(/^(_)|[^a-zA-Z0-9_]+/g, "");
        if (idx >= 0) {
            alias += "_"+idx;
        }
        if (columns.indexOf(alias.toLowerCase()) > -1) {
            return ColumnUtil.uniqueName(columns, columnFieldName, idx+1);
        }
        return alias;
    }

    /**
     * Parse a struct field into its top-level fields
     * @param column
     * @returns {string[]} list of fields
     */
    static structToFields(column: any): string[] {

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
     * Generates a script to use a temp column with the desired result and replace the existing column and ordering for
     * which the temp column was derived. This is used by some of the machine
     * learning functions that don't return column types
     * @returns {string}
     */
    static generateRenameScript(fieldName: string, tempField: string, grid: any): string {
        // Build select script to drop temp column we generated
        var self = this;
        let cols: string[] = [];
        _.each(grid.columns, col => {
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
     * Generates a temporary fieldname
     * @returns {string} the fieldName
     */
   static createTempField(): string {
        return "c_" + (new Date()).getTime();
    }

    static toColumnArray(columns: any[], ommitColumn ?: string): string[] {
        let cols: string[] = [];
        _.each(columns, column => {
            if (!ommitColumn || (ommitColumn && ommitColumn != column.name)) {
                cols.push(ColumnUtil.getColumnFieldName(column));
            }
        });
        return cols;
    }


    static escapeRegExp(text: string): string {
        return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\\\$&');
    }

    /**
     * If the character is reserved regex then the character is escaped
     */
    static escapeRegexCharIfNeeded(chr:string):string {
        if (chr == ' ') return "\\\\s";
        return (chr.match(/[\[\^\$\.\|\?\*\+\(\)]/g) ? `\\\\${chr}` : chr);
    }


    /**
     * Attempt to determine number of elements in array
     * @param {string} text
     * @returns {string}
     */
    static arrayItems(text: string): number {
        return (text && text.length > 0 ? text.split(",").length : 1);
    }

}
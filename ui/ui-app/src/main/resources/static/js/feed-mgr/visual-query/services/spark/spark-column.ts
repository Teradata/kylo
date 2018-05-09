import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import * as moment from "moment";
import * as angular from 'angular';
import * as _ from "underscore";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/map";
import {Observable} from "rxjs/Observable";
import {DateFormatResponse} from "../../wrangler/api";
import {DataType} from "../../wrangler/api/column";
import {DateFormatType, DateFormatUnit, DialogService} from "../../wrangler/api/services/dialog.service";
import {ColumnController} from "../../wrangler/column-controller";
import {ColumnDelegate, DataCategory} from "../../wrangler/column-delegate";

/**
 * Data types supported by Spark and Hive.
 */
const SparkDataType = {
    // Numeric types
    BYTE: new DataType("byte", "Byte"),
    SHORT: new DataType("short", "Short"),
    INT: new DataType("int", "Int", "exposure_zero"),
    BIGINT: new DataType("bigint", "Bigint", "exposure_zero"),
    FLOAT: new DataType("float", "Float"),
    DOUBLE: new DataType("double", "Double", "exposure_zero"),
    DECIMAL: new DataType("decimal", "Decimal"),

    // Date/time types
    DATE: new DataType("date", "Date", "today"),
    TIMESTAMP: new DataType("timestamp", "Timestamp", "access_time"),

    // Misc types
    BOOLEAN: new DataType("boolean", "Boolean"),
    BINARY: new DataType("binary", "Binary"),
    STRING: new DataType("string", "String", "format_quote"),

    // Complex types
    ARRAY: new DataType("array", "Array"),
    MAP: new DataType("map", "Map"),
    STRUCT: new DataType("struct", "Struct")
};

/**
 * Response model for the format-date endpoint.
 */
interface FormatDateResponse {
    text: string;
}

/**
 * Response model for the parse-date endpoint.
 */
interface ParseDateResponse {
    date: number;
}

/**
 * Handles user interactions with a Spark column.
 */
export class SparkColumnDelegate extends ColumnDelegate {

    constructor(private column: any, dataType: string, controller: ColumnController, $mdDialog: angular.material.IDialogService, uiGridConstants: any, dialog: DialogService, private http: HttpClient, private RestUrlService: any) {
        super(dataType, controller, $mdDialog, uiGridConstants, dialog);
    }

    /**
     * Gets the display name of this column.
     */
    private get displayName() {
        return this.getColumnDisplayName(this.column);
    }

    /**
     * Gets the field name of this column.
     */
    private get fieldName() {
        return this.getColumnFieldName(this.column);
    }

    /**
     * Casts this column to the specified type.
     */
    castTo(dataType: DataType): void {
        if (dataType === SparkDataType.BIGINT) {
            const formula = this.toFormula(this.fieldName + ".cast(\"bigint\")", this.column, {columns: (this.controller as any).tableColumns});
            this.controller.addFunction(formula, {formula: formula, icon: "exposure_zero", name: "Cast " + this.displayName + " to bigint"});
        }
        if (dataType === SparkDataType.DATE) {
            return this.castToDate();
        }
        if (dataType === SparkDataType.DOUBLE) {
            const formula = this.toFormula(this.fieldName + ".cast(\"double\")", this.column, {columns: (this.controller as any).tableColumns});
            this.controller.addFunction(formula, {formula: formula, icon: "exposure_zero", name: "Cast " + this.displayName + " to double"});
        }
        if (dataType === SparkDataType.INT) {
            const formula = this.toFormula(this.fieldName + ".cast(\"int\")", this.column, {columns: (this.controller as any).tableColumns});
            this.controller.addFunction(formula, {formula: formula, icon: "exposure_zero", name: "Cast " + this.displayName + " to int"});
        }
        if (dataType === SparkDataType.STRING) {
            return this.castToString();
        }
        if (dataType === SparkDataType.TIMESTAMP) {
            return this.castToTimestamp();
        }
    }

    /**
     * Gets the target data types supported for casting this column.
     */
    getAvailableCasts(): DataType[] {
        switch (this.dataType) {
            case SparkDataType.BIGINT.value:
            case SparkDataType.INT.value:
                return [SparkDataType.DATE, SparkDataType.STRING];

            case SparkDataType.DATE.value:
                return [SparkDataType.STRING, SparkDataType.TIMESTAMP];

            case SparkDataType.STRING.value:
                return [SparkDataType.BIGINT, SparkDataType.DATE, SparkDataType.DOUBLE, SparkDataType.INT, SparkDataType.TIMESTAMP];

            default:
                return [SparkDataType.STRING];
        }
    }

    /**
     * Override default validate so we dont refresh teh grid
     * @param filter
     * @param table
     */
    applyFilter(header:any,filter: any, table: any) {
       this.controller.addColumnFilter(filter,header,true)
    }

    applyFilters(header:any,filters:any[],table:any){
        //filter out any filters that dont have anything
        let validFilters =_.filter(filters,(filter) => {
            return (angular.isDefined(filter.term)&& filter.term != '')
        });

        _.each(validFilters,(filter,i)=> {
               let query = false;
               if(i == (validFilters.length -1)){
                   query = true;
               }
                this.controller.addColumnFilter(filter,header,true)
            });
    }


    sortColumn(direction: string, column: any, grid: any) {
        this.controller.addColumnSort(direction,column,true);
    }

    /**
     * Casts this column to a date type.
     */
    private castToDate(): void {
        // Generate preview function
        const sampleValue = this.getSampleValue();
        const preview = (sampleValue != null) ? (format: DateFormatResponse) => this.parseDate(sampleValue, format).map(date => date.toLocaleDateString()) : null;

        // Get date pattern from user
        if (this.dataCategory === DataCategory.NUMERIC) {
            this.dialog.openDateFormat({
                message: "Enter the pattern for parsing this column as a date:",
                preview: preview,
                title: "Convert " + this.dataType.toLowerCase() + " to date",
                type: DateFormatType.TIMESTAMP
            }).subscribe(response => {
                // Build script
                let script = this.fieldName;

                if (response.unit === DateFormatUnit.MILLISECONDS) {
                    script = "(" + script + "/1000)";
                }

                script = "to_date(" + script + ".cast(\"timestamp\")).as(\"" + StringUtils.quote(this.displayName) + "\")";

                // Add function to wrangler
                const formula = this.toFormula(script, this.column, {columns: (this.controller as any).tableColumns});
                this.controller.addFunction(formula, {formula: formula, icon: "event", name: "Cast " + this.displayName + " to date"});
            });
        } else if (this.dataCategory === DataCategory.STRING) {
            this.dialog.openDateFormat({
                message: "Enter the pattern for parsing this column as a date:",
                pattern: "yyyy-MM-dd",
                patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                preview: preview,
                title: "Convert " + this.dataType.toLowerCase() + " to date"
            }).subscribe(response => {
                // Build script
                let script;
                if (response.type === DateFormatType.STRING) {
                    script = "to_date(unix_timestamp(" + this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").cast(\"timestamp\")).as(\"" + StringUtils.quote(this.displayName) + "\")";
                } else if (response.type === DateFormatType.TIMESTAMP) {
                    script = this.fieldName + ".cast(\"bigint\")";

                    if (response.unit === DateFormatUnit.MILLISECONDS) {
                        script = "(" + script + "/1000)";
                    }

                    script = "to_date(" + script + ".cast(\"timestamp\")).as(\"" + StringUtils.quote(this.displayName) + "\")";
                }

                // Add function to wrangler
                const formula = this.toFormula(script, this.column, {columns: (this.controller as any).tableColumns});
                this.controller.addFunction(formula, {formula: formula, icon: "event", name: "Cast " + this.displayName + " to date"});
            });
        }
    }

    /**
     * Converts this column to a string type.
     */
    private castToString(): void {
        if (this.dataCategory === DataCategory.DATETIME) {
            const sampleValue = this.getSampleValue();

            // Get date pattern from user
            this.dialog.openDateFormat({
                message: "Enter the pattern for formatting this column as a string:",
                pattern: "yyyy-MM-dd",
                patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                preview: (sampleValue != null) ? format => this.formatDate(new Date(sampleValue), format) : null,
                title: "Convert " + this.dataType.toLowerCase() + " to string"
            }).subscribe(response => {
                // Build script
                let script;
                if (response.type === DateFormatType.STRING) {
                    script = "date_format(" + this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").as(\"" + StringUtils.quote(this.displayName) + "\")";
                } else if (response.type === DateFormatType.TIMESTAMP) {
                    script = (this.dataCategory === DataCategory.NUMERIC) ? this.fieldName : "unix_timestamp(" + this.fieldName + ")";

                    if (response.unit === DateFormatUnit.SECONDS) {
                        script += ".cast(\"bigint\")"
                    } else if (response.unit === DateFormatUnit.MILLISECONDS) {
                        script = "(" + script + ".cast(\"decimal(23,3)\") * 1000).cast(\"bigint\")";
                    }

                    script += ".cast(\"string\").as(\"" + StringUtils.quote(this.displayName) + "\")";
                }

                // Add function to wrangler
                const formula = this.toFormula(script, this.column, {columns: (this.controller as any).tableColumns});
                this.controller.addFunction(formula, {formula: formula, icon: "format_quote", name: "Cast " + this.displayName + " to string"});
            });
        } else {
            const formula = this.toFormula(this.fieldName + ".cast(\"string\")", this.column, {columns: (this.controller as any).tableColumns});
            this.controller.addFunction(formula, {formula: formula, icon: "format_quote", name: "Cast " + this.displayName + " to string"});
        }
    }

    /**
     * Cast this column to a timestamp type.
     */
    private castToTimestamp(): void {
        const sampleValue = this.getSampleValue();

        // Detect ISO dates

        if (this.dataCategory === DataCategory.DATETIME) {
            const formula = this.toFormula("unix_timestamp(" + this.fieldName + ")", this.column, {columns: (this.controller as any).tableColumns});
            this.controller.addFunction(formula, {formula: formula, icon: "access_time", name: "Cast " + this.displayName + " to timestamp"});
        } else if (this.dataCategory === DataCategory.STRING) {
            // If ISO date then just convert it. Otherwise, prompt.
            if (Date.parse(sampleValue) != undefined) {
                var script = `${this.fieldName}.cast("timestamp")`;
                const formula = this.toFormula(script, this.column, {columns: (this.controller as any).tableColumns});
                this.controller.addFunction(formula, {formula: formula, icon: "access_time", name: "Cast " + this.displayName + " to timestamp"});
            } else {
                this.dialog.openDateFormat({
                    message: "Enter the pattern for parsing this column as a timestamp:",
                    pattern: "yyyy-MM-dd HH:mm:ss",
                    patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                    preview: (sampleValue != null) ? format => this.parseDate(sampleValue, format).map(date => moment(date).format("YYYY-MM-DD HH:mm:ss")) : null,
                    title: "Convert " + this.dataType.toLowerCase() + " to timestamp",
                    type: DateFormatType.STRING
                }).subscribe(response => {
                    const script = "unix_timestamp(" + this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").as(\"" + StringUtils.quote(this.displayName) + "\")";
                    const formula = this.toFormula(script, this.column, {columns: (this.controller as any).tableColumns});
                    this.controller.addFunction(formula, {formula: formula, icon: "access_time", name: "Cast " + this.displayName + " to timestamp"});
                });
            }
        }
    }

    /**
     * Formats the specified Date as a string.
     *
     * @param value - the date
     * @param format - the format configuration
     * @returns the date string
     */
    private formatDate(value: Date, format: DateFormatResponse): Observable<string> {
        if (format.type === DateFormatType.STRING) {
            return this.http
                .get<FormatDateResponse>(this.RestUrlService.FORMAT_DATE, {
                    params: {
                        date: value.getTime().toString(),
                        pattern: format.pattern
                    }
                })
                .map(response => response.text)
                .catch((response: HttpErrorResponse): Observable<string> => {
                    throw response.error.message;
                });
        } else if (format.type === DateFormatType.TIMESTAMP) {
            if (format.unit === DateFormatUnit.SECONDS) {
                return Observable.of(Math.floor(value.getTime() / 1000).toString());
            } else if (format.unit === DateFormatUnit.MILLISECONDS) {
                return Observable.of(value.getTime().toString());
            }
        }
        return Observable.empty();
    }

    /**
     * Gets a sample value for this column.
     */
    private getSampleValue() {
        const columnIndex = (this.controller as any).tableColumns.findIndex((column: any) => column.field === this.column.field);
        return (this.controller as any).tableRows.map((row: any) => row[columnIndex]).find((value: any) => value != null);
    }

    /**
     * Parses the specified string into a Date.
     *
     * @param value - the date string
     * @param format - the format configuration
     * @returns the Date
     */
    private parseDate(value: string, format: DateFormatResponse): Observable<Date> {
        let date: Observable<Date> = null;

        if (format.type === DateFormatType.STRING) {
            date = this.http
                .get<ParseDateResponse>(this.RestUrlService.PARSE_DATE, {
                    params: {
                        pattern: format.pattern,
                        text: value
                    }
                })
                .map(response => new Date(response.date))
                .catch((response: HttpErrorResponse): Observable<Date> => {
                    throw response.error.message;
                });
        } else if (format.type === DateFormatType.TIMESTAMP) {
            if (format.unit === DateFormatUnit.SECONDS) {
                date = Observable.of(new Date(parseInt(value) * 1000));
            } else if (format.unit === DateFormatUnit.MILLISECONDS) {
                date = Observable.of(new Date(parseInt(value)));
            }
        }

        if (date !== null) {
            return date.map(utc => new Date(utc.getUTCFullYear(), utc.getUTCMonth(), utc.getUTCDate(), utc.getUTCHours(), utc.getUTCMinutes(), utc.getUTCSeconds(), utc.getUTCMilliseconds()));
        } else {
            return Observable.empty();
        }
    }
}

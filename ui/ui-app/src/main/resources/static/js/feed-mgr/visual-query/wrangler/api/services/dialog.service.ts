import {Observable} from "rxjs/Observable";
import {DataCategory} from "../../column-delegate";
import {QueryResultColumn} from "../..";
import {DynamicFormDialogData} from "../../../../../../lib/dynamic-form/simple-dynamic-form/dynamic-form-dialog-data";
import {ColumnForm} from "../../core/columns/column-form";

/**
 * Standard configuration for dialogs.
 */
export interface DialogConfig {

    /**
     * Text message for the dialog content.
     */
    message: string;

    /**
     * Title for the dialog.
     */
    title?: string;
}

/**
 * Type formats for the date format dialog.
 */
export enum DateFormatType {

    /**
     * String input / output type.
     */
    STRING = "STRING",

    /**
     * Timestamp input / output type.
     */
    TIMESTAMP = "TIMESTAMP"
}

/**
 * Timestamp unit for the date format dialog.
 */
export enum DateFormatUnit {

    /**
     * Number of seconds since 1970-01-01.
     */
    SECONDS = "SECONDS",

    /**
     * Number of milliseconds since 1970-01-01.
     */
    MILLISECONDS = "MILLISECONDS"
}

/**
 * Configuration for the date format dialog.
 */
export interface DateFormatConfig extends DialogConfig {

    /**
     * Default value for the date format pattern. Used only by the STRING type.
     */
    pattern?: string;

    /**
     * Hint text for the date format pattern control.
     */
    patternHint?: string;

    /**
     * Calculates the value for the preview control. Called when the pattern or unit control are changed.
     */
    preview?: (format: DateFormatResponse) => Observable<string>;

    /**
     * Sets the type of date format. If not specified then the user may select the value.
     *
     * The STRING type enables only the date format pattern control. The TIMESTAMP type enables only the unit control.
     */
    type?: DateFormatType

    /**
     * Default value for the timestamp unit. Used only by the TIMESTAMP type.
     */
    unit?: DateFormatUnit;
}

/**
 * Result of the date format dialog.
 */
export interface DateFormatResponse {

    /**
     * A date format pattern. Used only by the STRING Type.
     */
    pattern?: string;

    /**
     * Type of date format.
     */
    type: DateFormatType;

    /**
     * A timestamp unit. Used only by the TIMESTAMP type.
     */
    unit?: DateFormatUnit;
}


/**
 * Configuration for the impute missing dialog.
 */
export interface ImputeMissingConfig extends DialogConfig {

    /**
     * List of valid fields for group and order dropdowns
     */
    fields: string[];
}

/**
 * Result of the impute dialog.
 */
export interface ImputeMissingResponse {

    /**
     * Type of operation
     */
    type: string;

    /**
     * GroupBy field
     */
    groupBy: string;

    /**
     * OrderBy field
     */
    orderBy: string;
}


/**
 * Configuration for the replace values equal to dialog.
 */
export interface ReplaceValueEqualToConfig extends DialogConfig {

    /**
     * the Field
     */
    fieldName: string

    /**
     * The filed data type
     */
    dataType:string;

    /**
     * The category of the data type
     */
    dataCategory:DataCategory;

    /**
     * the value to replace
     */
    value:string;
}


/**
 * Response for replace value equal to dialog
 */
export interface ReplaceValueEqualToResponse {

    /**
     * value to replace
     */
    replaceValue: any;
}



/**
 * Configuration for the replace values equal to dialog.
 */
export interface BinValuesConfig extends DialogConfig {

    /**
     * the Field
     */
    fieldName: string

    /**
     * The filed data type
     */
    dataType:string;

    /**
     * The category of the data type
     */
    dataCategory:DataCategory;

}

export interface BinValuesResponse {
    bins: number
}


/**
 * Configuration for the replace values equal to dialog.
 */
export interface CrossTabConfig extends DialogConfig {

    /**
     * the Field
     */
    fieldName: string

    /**
     * possible columns in the transformation
     */
    columns:QueryResultColumn[]

}

export interface CrossTabResponse {
    field: string
}




/**
 * Opens modal dialogs for alerting the user or receiving user input.
 */
export interface DialogService {

    /**
     * Opens a modal dialog for the user to input a date format string.
     *
     * @param config - dialog configuration
     * @returns the date format string
     */
    openDateFormat(config: DateFormatConfig): Observable<DateFormatResponse>;

    /**
     * Opens the dynamic form as a dialog
     * @param {DynamicFormDialogData} data
     * @return {Observable<any>}
     */
    openColumnForm(data:ColumnForm):Observable<any>;

}

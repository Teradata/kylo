import {Observable} from "rxjs/Observable";

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
}

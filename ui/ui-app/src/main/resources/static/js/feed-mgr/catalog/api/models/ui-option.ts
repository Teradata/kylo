export class SelectOption {
    label: string;
    value: string;
}

export class UiOptionValidator {
    type: string;
    params: any;
}

/**
 * UI option for user input.
 */
export class UiOption {
    /**
     * Unique key, normally would correspond to option key in
     * Datasource that this UiOption is getting value for
     */
    key: string;
    /**
     * Human readable label
     */
    label: string;
    /**
     * Optional type, defaults to "text".
     * Supported types: "text", "password", "select"
     */
    type?: string;
    /**
     * Value of the input, applies equally to all types including "select"
     */
    value?: string;
    /**
     * Whether this option is required or not
     */
    required?: boolean;
    /**
     * How many percent of the row this input should take
     */
    flex?: number;
    /**
     * Select options if type is "select"
     */
    selections?: SelectOption[];
    /**
     * Hint to be displayed at the bottom of input field.
     */
    hint?: string;

    validators?: UiOptionValidator[];

    sensitive?: boolean;
}

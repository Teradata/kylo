import {FieldConfig} from "./FieldConfig";

export class RadioButton extends FieldConfig<string> {
    static CONTROL_TYPE = 'radio'
    controlType = RadioButton.CONTROL_TYPE;
    options: {label: string, value: string}[] = [];

    constructor(options: {} = {}) {
        super(options);
        this.options = options['options'] || [];
    }
}
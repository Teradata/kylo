import {FieldConfig} from "./FieldConfig";

export class RadioButton extends FieldConfig<string> {
    controlType = 'radio';
    options: {label: string, value: string}[] = [];

    constructor(options: {} = {}) {
        super(options);
        this.options = options['options'] || [];
    }
}
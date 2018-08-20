import {FieldConfig} from "./FieldConfig";

export class Select extends FieldConfig<string> {
    controlType = 'select';
    options: {label: string, value: string}[] = [];

    constructor(options: {} = {}) {
        super(options);
        this.options = options['options'] || [];
    }
}
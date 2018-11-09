import {FieldConfig} from "./FieldConfig";

export class Select extends FieldConfig<string> {
    static CONTROL_TYPE = 'select';
    controlType = Select.CONTROL_TYPE;
    options: {label: string, value: string}[] = [];

    constructor(options: {} = {}) {
        super(options);
        this.options = options['options'] || [];
    }
}
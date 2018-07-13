import {FieldConfig} from "./FieldConfig";

export class Select extends FieldConfig<string> {
    controlType = 'select';
    options: {key: string, value: string}[] = [];

    constructor(options: {} = {}) {
        super(options);
        this.options = options['options'] || [];
    }
}
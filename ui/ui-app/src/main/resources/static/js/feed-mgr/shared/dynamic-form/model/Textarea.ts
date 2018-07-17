import {FieldConfig} from "./FieldConfig";

export class Textarea extends FieldConfig<string> {
    controlType = 'textarea';

    constructor(options: {} = {}) {
        super(options);
    }
}
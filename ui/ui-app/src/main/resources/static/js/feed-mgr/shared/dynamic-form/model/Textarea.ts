import {FieldConfig} from "./FieldConfig";

export class Textarea extends FieldConfig<string> {
    static CONTROL_TYPE = 'textarea'
    controlType = Textarea.CONTROL_TYPE;

    constructor(options: {} = {}) {
        super(options);
    }
}
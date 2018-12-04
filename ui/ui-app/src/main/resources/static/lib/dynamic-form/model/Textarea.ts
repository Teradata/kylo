import {FieldConfig} from "./FieldConfig";

export class Textarea extends FieldConfig<string> {
    static CONTROL_TYPE = 'textarea'
    controlType = Textarea.CONTROL_TYPE;
    readonly :boolean;

    constructor(options: {} = {}) {
        super(options);
        this.readonly = options['readonly'];
        if(this.readonly) {
            this.value = this.readonlyValue;
        }
    }
}
import {FieldConfig} from "./FieldConfig";

export class Icon extends FieldConfig<string> {
    static CONTROL_TYPE = 'icon';
    controlType = Icon.CONTROL_TYPE;
    name: string;
    size: number;

    constructor(options: {} = {}) {
        super(options);
        this.name = options['name'] || "";
        this.size = options['size'] || 20;
    }
}
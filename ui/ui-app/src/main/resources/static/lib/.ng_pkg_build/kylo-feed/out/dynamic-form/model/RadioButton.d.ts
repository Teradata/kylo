import { FieldConfig } from "./FieldConfig";
export declare class RadioButton extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    options: {
        label: string;
        value: string;
    }[];
    constructor(options?: {});
}

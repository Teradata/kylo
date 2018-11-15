import { FieldConfig } from "./FieldConfig";
export declare class Select extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    options: {
        label: string;
        value: string;
    }[];
    constructor(options?: {});
}

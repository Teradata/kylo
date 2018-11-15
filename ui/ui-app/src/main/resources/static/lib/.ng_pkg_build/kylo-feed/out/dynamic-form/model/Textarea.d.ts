import { FieldConfig } from "./FieldConfig";
export declare class Textarea extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    readonly: boolean;
    constructor(options?: {});
}

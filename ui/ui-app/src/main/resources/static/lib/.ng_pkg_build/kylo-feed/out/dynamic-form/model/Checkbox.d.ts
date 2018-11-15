import { FieldConfig } from "./FieldConfig";
import { MatCheckboxChange } from "@angular/material";
export declare class Checkbox extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    trueValue: string;
    falseValue: string;
    checked: boolean;
    constructor(options?: {});
    initCheckedValue(): void;
    onChange(event: MatCheckboxChange): void;
}

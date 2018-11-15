import { FieldConfig } from "./FieldConfig";
export declare class StaticText extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    showDivider: boolean;
    staticText: string;
    textStyleClass: string;
    constructor(options?: {});
}

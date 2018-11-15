import { FieldConfig } from "./FieldConfig";
export declare enum Layout {
    COLUMN = "column",
    ROW = "row"
}
export declare class FieldGroup {
    layout: Layout;
    private DEFAULT_LAYOUT;
    fields: FieldConfig<any>[];
    layoutAlign: string;
    constructor(layout?: Layout);
    setLayoutAlign(layoutAlign: string): void;
}

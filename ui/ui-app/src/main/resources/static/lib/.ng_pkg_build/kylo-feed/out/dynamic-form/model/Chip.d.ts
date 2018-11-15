import { FieldConfig } from "./FieldConfig";
export declare class Chip extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    items: any[];
    stacked: boolean;
    constructor(options?: {});
    disabled: boolean;
    chipAddition: boolean;
    chipRemoval: boolean;
    filteredItems: string[];
    selectedItems: string[];
    ngOnInit(): void;
    filterItems(value: string): void;
    updateModel(value: any): void;
}

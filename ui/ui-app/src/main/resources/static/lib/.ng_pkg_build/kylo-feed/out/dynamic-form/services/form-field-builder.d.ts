import { FieldConfig } from "../model/FieldConfig";
import { FieldConfigBuilder } from "./field-config-builder";
export declare class FormFieldBuilder {
    fields: FieldConfigBuilder<any>[];
    constructor();
    field(fieldBuilder: FieldConfigBuilder<any>): this;
    /**
     * Builds the FieldConfig objects needed for a form
     */
    build(): FieldConfig<any>[];
}

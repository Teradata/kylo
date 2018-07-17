import {FieldConfig} from "./FieldConfig";

export class SectionHeader extends FieldConfig<string> {

    static CONTROL_TYPE = "section-header";
    controlType = SectionHeader.CONTROL_TYPE
    constructor(options: {} = {}) {
        super(options);

    }
}
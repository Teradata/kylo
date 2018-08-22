import {FieldConfig} from "./FieldConfig";

export class SectionHeader extends FieldConfig<string> {

    static CONTROL_TYPE = "section-header";
    controlType = SectionHeader.CONTROL_TYPE
    showDivider:boolean;
    constructor(options: {} = {}) {
        super(options);
        this.showDivider = options['showDivider'] || true;

    }
}
import {FieldConfig} from "./FieldConfig";

export class StaticText extends FieldConfig<string> {

    static CONTROL_TYPE = "static-text";
    controlType = StaticText.CONTROL_TYPE
    showDivider:boolean;

    staticText:string;
    textStyleClass:string;
    constructor(options: {} = {}) {
        super(options);
        this.showDivider = options['showDivider'] || true;
        this.textStyleClass = options['textStyleClass'] ||'';
        this.staticText = options['staticText'] ||'';
    }
}
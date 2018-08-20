import {FieldConfig} from "./FieldConfig";
import {MatCheckboxChange} from "@angular/material";


export class Checkbox extends FieldConfig<string> {
    controlType = 'checkbox';
    type: string;
    trueValue:string;
    falseValue:string;
    checked:boolean;

    constructor(options: {} = {}) {
        super(options);
        this.trueValue = options['trueValue'] || 'true';
        this.falseValue = options['falseValue'] || 'falseValue';
        this.initCheckedValue();
    }

    initCheckedValue(){
       let value = this.getModelValue();
       if(value && value == this.trueValue){
           this.checked = true;
       }
       else {
           this.checked = false;
       }
    }

    onChange(event:MatCheckboxChange){
        if(event.checked){
            this.setModelValue(this.trueValue);
        }
        else {this.setModelValue(this.falseValue)}
    }
}
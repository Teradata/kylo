import {TableColumnDefinition} from "../../../../model/TableColumnDefinition";
import {TableFieldPolicy} from "../../../../model/TableFieldPolicy";

export class SelectedColumn {

        standardizers:TableFieldPolicy[];
        validators:TableFieldPolicy[];
        standardizerCount:number = 0;
        validatorCount:number = 0;

        constructor(public field:TableColumnDefinition, private fieldPolicy:TableFieldPolicy) {

           this.update();
        };

        get selectedSampleValue() {
            return this.field.selectedSampleValue;
        }

        set selectedSampleValue(sampleValue:any) {
            this.field.selectedSampleValue = sampleValue
        }

        update(){
            if(this.fieldPolicy) {
                this.standardizers = this.fieldPolicy.standardization || [];
                this.validators = this.fieldPolicy.validation || [];
                this.standardizerCount = this.standardizers.length;
                this.validatorCount = this.validators.length;
            }
        }


    }

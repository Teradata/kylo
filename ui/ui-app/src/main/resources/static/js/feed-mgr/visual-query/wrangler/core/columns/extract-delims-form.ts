import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {ColumnUtil} from "../column-util";
import {Validators} from "@angular/forms";

export class ExtractDelimsForm extends ColumnForm{


    constructor(column:any, grid:any,controller:ColumnController){
        super(column,grid,controller)
    }

    buildForm(){

        let fieldName = this.fieldName;

        return new DynamicFormBuilder()
            .setTitle("Extract Between Delimiters")
            .setMessage("Extracts a string between two delimiters.")
            .column()
            .text().setKey("start").setType(InputType.text).setRequired(true).setPattern(".").setValue("[").setPlaceholder("Start delim").done()
            .text().setKey("end").setType(InputType.text).setRequired(true).setPattern(".").setValue("]").setPlaceholder("End delim").done()
            .text().setKey("group").setType(InputType.number).setRequired(true).setValue(1).setValidators([Validators.min(0)]).setPlaceholder("Group").done()
            .columnComplete()
            .onApply((values:any) => {
                let start = ColumnUtil.escapeRegexCharIfNeeded(values.start);
                let end = ColumnUtil.escapeRegexCharIfNeeded(values.end);
                let group = values.group;
                let regex = `${start}(.*?)${end}`;
                this.executeRegex(regex,group);
            })
            .build()
    }

}

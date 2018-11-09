import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {ColumnForm} from "./column-form";
import {ColumnController} from "../../column-controller";
import {InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {Validators} from "@angular/forms";

export class ExtractRegexForm extends ColumnForm{


    constructor(column:any, grid:any,controller:ColumnController, private delegate:any){
        super(column,grid,controller)
    }

    buildForm(){

        let fieldName = this.fieldName;

        return new DynamicFormBuilder()
            .setTitle("Extract Regex")
            .setMessage("Extracts a string matching regex pattern and provided group.")
            .column()
            .text().setKey("regex").setType(InputType.text).setRequired(true).setValue("\\\\[(.*?)\\\\]").setPlaceholder("Regex pattern").done()
            .text().setKey("group").setType(InputType.number).setRequired(true).setValue(0).setValidators([Validators.min(0)]).setPlaceholder("Group").done()
            .columnComplete()
            .onApply((values:any) => {
                let regex = values.regex;
                let group = values.group;
                this.executeRegex(regex,group);
            })
            .build()
    }

}

import {Component, OnDestroy, OnInit} from "@angular/core";
import "rxjs/add/observable/merge";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/do";
import {InputType} from "../model/InputText";
import {InputTextFieldBuilder, RadioButtonFieldBuilder, SectionHeaderBuilder, SelectFieldBuilder, TextareaFieldBuilder} from "../services/field-config-builder";
import {DynamicFormBuilder, FormConfig} from "../services/dynamic-form-builder";


/**
 * A prompt dialog for providing a date format pattern that can be used for formatting or parsing dates or timestamps.
 */
@Component({
    selector:"dynamic-form-example",
    templateUrl: "js/feed-mgr/shared/dynamic-form/example/dynamic-form-example.component.html"
})
export class DynamicFormExampleComponent implements OnInit,OnDestroy{

    formConfig:FormConfig;

    constructor() {

        // Create form
        this.formConfig = new DynamicFormBuilder().setTitle("Example Form")
            .field(new SelectFieldBuilder().setKey("color")
                .setLabel("Favorite color")
                .addOption("red", "red")
                .addOption("blue", "blue")
                .addOption("green", "green"))
            .field(new InputTextFieldBuilder().setKey("age")
                .setLabel("Age").setType(InputType.number)
                .setHint("Your age"))
            .field(new RadioButtonFieldBuilder().setLabel("Favorite dessert")
                .setValue("CAKE")
                .addOption("ice cream", "ICE_CREAM")
                .addOption("cake", "CAKE")
                .addOption("cookies", "COOKIES")
                .setRequired(true))
            .field(new SectionHeaderBuilder().setLabel("New Section Header"))
            .field(new InputTextFieldBuilder().setKey("homeAddress")
                .setLabel("Home address")
                .setType(InputType.text)
                .setRequired(true))
            .field(new InputTextFieldBuilder()
                .setKey("phone")
                .setLabel("Phone")
                .setType(InputType.tel)
                .setPattern("[0-9]{3}-[0-9]{3}-[0-9]{4}")
                .setPlaceholder("123-456-7890")
                .setRequired(true))
            .field(new TextareaFieldBuilder()
                .setLabel("description"))
            .done()
            .build();
    }

    ngOnInit(){

    }
    ngOnDestroy(){

    }





    /**
     * Closes this dialog and returns the form value.
     */
    apply() {
        console.log("APPLY ",this.formConfig.form.value)
    }

    /**
     * Cancel this dialog.
     */
    cancel() {
       console.log("CANCELLED!!! ")
    }



}

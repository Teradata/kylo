import {Component, OnDestroy, OnInit} from "@angular/core";
import "rxjs/add/observable/merge";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/do";
import {InputType} from "../model/InputText";
import {DynamicFormBuilder, FormConfig} from "../services/dynamic-form-builder";


@Component({
    selector:"dynamic-form-example",
    templateUrl: "./dynamic-form-example.component.html"
})
export class DynamicFormExampleComponent implements OnInit,OnDestroy{

    formConfig:FormConfig;

    constructor() {
        

        // Create form
        this.formConfig = new DynamicFormBuilder().setTitle("Example Form")
            .row()
                 .select().setKey("color")
                    .setPlaceholder("Backup Label")
                    .setPlaceholderLocaleKey("views.define-feed-general-info.DN")
                    .addOption("red", "red")
                    .addOption("blue", "blue")
                    .addOption("green", "green")
                    .setStyleClass("pad-right")
                 .done()
                 .text().setKey("age")
                    .setPlaceholder("Age")
                    .setType(InputType.number)
                    .setHint("Your age")
                 .done()
            .rowComplete()
            .column()
                .radio().setKey("dessert")
                    .setPlaceholder("Favorite dessert")
                    .setValue("CAKE")
                    .addOption("ice cream", "ICE_CREAM")
                    .addOption("cake", "CAKE")
                    .addOption("cookies", "COOKIES")
                    .setRequired(true)
                .done()
                .sectionHeader().setPlaceholder("New Section Header")
                      .setShowDivider(true).done()
                .text().setKey("homeAddress")
                    .setPlaceholder("Home address")
                    .setType(InputType.text)
                    .setRequired(true).done()
                .text().setKey("phone")
                    .setPlaceholder("Phone")
                    .setType(InputType.tel)
                    .setPattern("[0-9]{3}-[0-9]{3}-[0-9]{4}")
                    .setPlaceholder("123-456-7890")
                    .setRequired(true).done()
                .textarea()
                    .setPlaceholder("description")
                .done()
            .columnComplete()
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
        
    }

    /**
     * Cancel this dialog.
     */
    cancel() {
       
    }



}

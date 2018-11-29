import {Component} from "@angular/core";
import {DynamicFormBuilder, FormConfig} from "../services/dynamic-form-builder";
import {InputType} from "../model/InputText";

@Component({
    selector:"example-forms",
    templateUrl: "./example-forms.component.html"
})
export class ExampleFormsComponent {

    form1:FormConfig;

    form2:FormConfig;

    constructor(){

        this.form1 = new DynamicFormBuilder().row()
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
            .done().columnComplete().onApply((values:any)=> {
                console.log("THE VALUES ARE ",values,this);
            },this).onCancel(()=> {

            },this).build();


        this.form2 = new DynamicFormBuilder().row()
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
            .done().columnComplete().onApply((values:any)=> {

            },this).onCancel(()=> {

            },this).build();



    }
}
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { Component } from "@angular/core";
import { DynamicFormBuilder, FormConfig } from "../services/dynamic-form-builder";
import { InputType } from "../model/InputText";
export class ExampleFormsComponent {
    constructor() {
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
            .done().columnComplete().onApply((values) => {
            console.log("THE VALUES ARE ", values, this);
        }, this).onCancel(() => {
            console.log("CANCELLED!!!!");
        }, this).build();
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
            .done().columnComplete().onApply((values) => {
            console.log("Form2 THE VALUES ARE ", values, this);
        }, this).onCancel(() => {
            console.log("form2 CANCELLED!!!!");
        }, this).build();
    }
}
ExampleFormsComponent.decorators = [
    { type: Component, args: [{
                selector: "example-forms",
                template: `Form1<br/>
<simple-dynamic-form [formConfig]="form1"></simple-dynamic-form>

<mat-divider></mat-divider>
Form2<br/>
<simple-dynamic-form [formConfig]="form2"></simple-dynamic-form>`
            },] },
];
/** @nocollapse */
ExampleFormsComponent.ctorParameters = () => [];
function ExampleFormsComponent_tsickle_Closure_declarations() {
    /** @type {!Array<{type: !Function, args: (undefined|!Array<?>)}>} */
    ExampleFormsComponent.decorators;
    /**
     * @nocollapse
     * @type {function(): !Array<(null|{type: ?, decorators: (undefined|!Array<{type: !Function, args: (undefined|!Array<?>)}>)})>}
     */
    ExampleFormsComponent.ctorParameters;
    /** @type {?} */
    ExampleFormsComponent.prototype.form1;
    /** @type {?} */
    ExampleFormsComponent.prototype.form2;
}
//# sourceMappingURL=example-forms.component.js.map
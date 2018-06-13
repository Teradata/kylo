import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";

import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {PolicyInputFormComponent} from "./policy-input-form.component";
import {InlinePolicyInputFormComponent} from "./inline-field-policy-form.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FieldPolicyOptionsService} from "./field-policy-options.service";
import {PolicyInputFormService} from "./policy-input-form.service";
import {MatDialogModule} from "@angular/material/dialog";
import {CovalentChipsModule} from "@covalent/core/chips";

@NgModule({
    declarations: [
        InlinePolicyInputFormComponent,
        PolicyInputFormComponent
    ],
    imports: [
        CommonModule,
        FlexLayoutModule,
        FormsModule,
        MatDatepickerModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressBarModule,
        MatTabsModule,
        MatSelectModule,
        MatSlideToggleModule,
        ReactiveFormsModule,
        CovalentChipsModule
    ],
    providers: [
        PolicyInputFormService,
        FieldPolicyOptionsService

    ],
    exports:[ InlinePolicyInputFormComponent,
        PolicyInputFormComponent]
})
export class FieldPoliciesModule {
}

import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTabsModule} from "@angular/material/tabs";
import {MatListModule} from "@angular/material/list";
import {MatInputModule} from "@angular/material/input";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatNativeDateModule, MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {DynamicFormService} from "./services/dynamic-form.service";
import {DynamicFormFieldComponent} from "./dynamic-form-field.component";
import {DynamicFormComponent} from "./dynamic-form.component";


@NgModule({
    declarations: [
        DynamicFormFieldComponent,
        DynamicFormComponent
        ],
    exports:[
        DynamicFormFieldComponent,
        DynamicFormComponent
    ],
    imports: [
        CommonModule,
        MatCardModule,
        FormsModule,
        MatAutocompleteModule,
        MatInputModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatCheckboxModule,
        MatOptionModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatDividerModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        MatNativeDateModule,
        MatButtonModule
    ],
    providers: [
        DynamicFormService

    ]
})
export class DynamicFormModule {

}

import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
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
import {MatRadioModule} from "@angular/material/radio";
import {CovalentChipsModule} from "@covalent/core/chips";
import {TranslateModule} from "@ngx-translate/core";
import {FlexLayoutModule} from "@angular/flex-layout";

import {ExampleFormsComponent} from "./example/example-forms.component";
import {SimpleDynamicFormComponent} from "./simple-dynamic-form/simple-dynamic-form.component";
import {SimpleDynamicFormDialogComponent} from "./simple-dynamic-form/simple-dynamic-form-dialog.component";
import {DynamicFormService} from "./services/dynamic-form.service";
import {DynamicFormComponent} from "./dynamic-form.component";
import {TrueFalseValueDirective} from "./true-false-value.directive";
import {DynamicFormExampleComponent} from "./example/dynamic-form-example.component";
import {DynamicFormFieldComponent} from "./dynamic-form-field.component";
import {CronExpressionPreviewModule} from '../cron-expression-preview/cron-expression-preview2.module';

@NgModule({
    declarations: [
        DynamicFormFieldComponent,
        DynamicFormComponent,
        TrueFalseValueDirective,
        DynamicFormExampleComponent,
        ExampleFormsComponent,
        SimpleDynamicFormComponent,
        SimpleDynamicFormDialogComponent
        ],
    entryComponents:[
        DynamicFormExampleComponent,
        ExampleFormsComponent,
        SimpleDynamicFormComponent,
        SimpleDynamicFormDialogComponent
    ],
    exports:[
        DynamicFormFieldComponent,
        DynamicFormComponent,
        TrueFalseValueDirective,
        DynamicFormExampleComponent,
        ExampleFormsComponent,
        SimpleDynamicFormComponent,
        SimpleDynamicFormDialogComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatNativeDateModule,
        MatOptionModule,
        MatRadioModule,
        MatSelectModule,
        MatTabsModule,
        MatToolbarModule,
        CovalentChipsModule,
        ReactiveFormsModule,
        TranslateModule,
        FlexLayoutModule,
        CronExpressionPreviewModule,
    ],
    providers: [
        DynamicFormService

    ]
})
export class DynamicFormModule {


}

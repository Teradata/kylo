import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";

import {DynamicFormModule} from "../dynamic-form/dynamic-form.module";
import {ProcessorFormComponent} from "./processor/processor-form.component";

@NgModule({
    declarations: [
        ProcessorFormComponent
    ],
    exports: [
        ProcessorFormComponent,
    ],
    imports: [
        CommonModule,
        DynamicFormModule,
        FormsModule
    ]
})
export class KyloFeedModule {
}

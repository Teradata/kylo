import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {KyloCommonModule} from "../../../common/common.module";
import {FeedPreconditionDialogComponent} from "./feed-precondition-dialog.component";
import {MatDialogModule} from "@angular/material/dialog";
import {FeedPreconditionDialogService} from "./feed-precondition-dialog-service";
import {MatButtonModule} from "@angular/material/button";
import {MatSelectModule} from "@angular/material/select";
import {MatFormFieldModule} from "@angular/material/form-field";
import {DynamicFormModule} from "../../../../lib/dynamic-form/dynamic-form.module";
import {FieldPoliciesModule} from "../field-policies-angular2/field-policies.module";
import {CovalentChipsModule} from "@covalent/core/chips";

@NgModule({
    declarations: [
        FeedPreconditionDialogComponent
    ],
    entryComponents: [
        FeedPreconditionDialogComponent
    ],
    imports: [
        CommonModule,
        KyloCommonModule,
        DynamicFormModule,
        FlexLayoutModule,
        FormsModule,
        FieldPoliciesModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatSelectModule,
        CovalentChipsModule
    ],
    providers: [
        FeedPreconditionDialogService
    ],
    exports:[ FeedPreconditionDialogComponent]
})
export class FeedPreconditionModule {
}

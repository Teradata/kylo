import {CommonModule} from "@angular/common";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {KyloCommonModule} from "../../common/common.module";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSelectModule} from "@angular/material/select";
import {KyloServicesModule} from "../../services/services.module";
import {UIRouterModule} from "@uirouter/angular";
import {NgModule} from "@angular/core";
import {slaStates} from "./sla.states";
import {SlaListComponent} from "./list/sla-list.componment";
import {SlaDetailsComponent} from "./details/sla-details.componment";
import {SlaFormComponent} from "./details/sla-form.componment";
import {SlaRowComponent} from "./list/sla-row.componment";
import {FieldPoliciesModule} from "../shared/field-policies-angular2/field-policies.module";
import {CronExpressionPreviewModule} from "../../../lib";
import {DynamicFormModule} from "../../../lib/dynamic-form/dynamic-form.module";
import {SlaComponent} from "./sla.componment";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {TranslateModule} from "@ngx-translate/core";
import {MatButtonModule} from "@angular/material/button";
import {MatListModule} from "@angular/material/list";
import {MatCardModule} from "@angular/material/card";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {CodemirrorModule} from "ng2-codemirror";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentDataTableModule} from "@covalent/core/data-table";

@NgModule({
    declarations: [
        SlaListComponent,
        SlaDetailsComponent,
        SlaFormComponent,
        SlaRowComponent,
        SlaComponent
    ],
    entryComponents: [
    ],
    exports:[
        SlaListComponent,
        SlaDetailsComponent,
        SlaFormComponent,
        SlaRowComponent
    ],
    imports: [
        CommonModule,
        CovalentDialogsModule,
        CovalentLoadingModule,
        CovalentLayoutModule,
        CovalentDataTableModule,
        CovalentSearchModule,
        FlexLayoutModule,
        FormsModule,
        MatButtonModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatDialogModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatToolbarModule,
        MatTooltipModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatButtonModule,
        MatListModule,
        MatCardModule,
        ReactiveFormsModule,
        TranslateModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        KyloServicesModule,
        FieldPoliciesModule,
        CronExpressionPreviewModule,
        DynamicFormModule,
        CodemirrorModule,
        UIRouterModule.forChild()
    ],
    providers:[
    ]

})
export class SlaModule {
    constructor(){
        
    }
}




@NgModule({
    imports: [
        SlaModule,
        UIRouterModule.forChild({states: slaStates})
    ]
})
export class SlaRouterModule {
    constructor() {
        
    }
}

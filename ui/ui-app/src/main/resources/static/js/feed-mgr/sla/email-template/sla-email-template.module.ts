import {CommonModule} from "@angular/common";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {KyloCommonModule} from "../../../common/common.module";
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
import {KyloServicesModule} from "../../../services/services.module";
import {UIRouterModule} from "@uirouter/angular";
import {NgModule} from "@angular/core";
import {slaEmailTemplateStates} from "./sla-email-template.states";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {TranslateModule} from "@ngx-translate/core";
import {MatButtonModule} from "@angular/material/button";
import {MatListModule} from "@angular/material/list";
import {MatCardModule} from "@angular/material/card";
import {SlaEmailTemplateComponent} from "./sla-email-template.component";
import {SlaEmailTemplatesComponent} from "./sla-email-templates.component";
import {VelocityTemplateDialogComponent} from "./velocity-template-dialog.component";
import {SlaEmailTemplateService} from "./sla-email-template.service";
import {CodemirrorModule} from "ng2-codemirror";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {SlaEmailTemplateContainerComponment} from "./sla-email-template-container.componment";

@NgModule({
    declarations: [
        SlaEmailTemplateContainerComponment,
        SlaEmailTemplateComponent,
        SlaEmailTemplatesComponent,
        VelocityTemplateDialogComponent
    ],
    entryComponents: [
        VelocityTemplateDialogComponent
    ],
    exports:[
        SlaEmailTemplateContainerComponment,
        SlaEmailTemplateComponent,
        SlaEmailTemplatesComponent,
        VelocityTemplateDialogComponent
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
        KyloServicesModule,
        CodemirrorModule,
        UIRouterModule.forChild()
    ],
    providers:[
        SlaEmailTemplateService
    ]

})
export class SlaEmailTemplateModule {
    constructor(){
        
    }
}




@NgModule({
    imports: [
        SlaEmailTemplateModule,
        UIRouterModule.forChild({states: slaEmailTemplateStates})
    ]
})
export class SlaEmailTemplatesRouterModule {
    constructor() {
        
    }
}

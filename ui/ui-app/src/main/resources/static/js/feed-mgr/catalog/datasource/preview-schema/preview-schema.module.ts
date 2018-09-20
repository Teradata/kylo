import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";

import {PreviewSchemaComponent, SchemaDefinitionComponent, SimpleTableComponent} from "./preview-schema.component";
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {CovalentNotificationsModule} from '@covalent/core/notifications';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatDialogModule} from "@angular/material/dialog";
import {SatusDialogComponent} from "../../dialog/status-dialog.component";
import {CovalentChipsModule} from "@covalent/core/chips";
import {FieldPoliciesModule} from "../../../shared/field-policies-angular2/field-policies.module"
import {MatIconModule} from "@angular/material/icon";
import {KyloCommonModule} from "../../../../common/common.module";
import {MatButtonModule} from "@angular/material/button";
import {SchemaParseSettingsDialog} from "./schema-parse-settings-dialog.component";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

import {PreviewSchemaService} from "./service/preview-schema.service";
import {PreviewRawService} from "./service/preview-raw.service";
import {TransformResponseTableBuilder} from "./service/transform-response-table-builder";
import {FileMetadataTransformService} from "./service/file-metadata-transform.service";
import {KyloServicesModule} from "../../../../services/services.module";
import {MatStepperModule} from "@angular/material/stepper";
import {MatCheckboxModule} from "@angular/material/checkbox";

@NgModule({
    declarations: [
        PreviewSchemaComponent,
        SatusDialogComponent,
        SchemaParseSettingsDialog,
        SimpleTableComponent,
        SchemaDefinitionComponent
    ],
    entryComponents: [
        SatusDialogComponent,
        SchemaParseSettingsDialog
    ],
    exports:[
        PreviewSchemaComponent,
        SimpleTableComponent,
        SchemaParseSettingsDialog,
        SchemaDefinitionComponent
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentNotificationsModule,
        FlexLayoutModule,
        FormsModule,
        KyloCommonModule,
        MatButtonModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatDialogModule,
        MatDividerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatStepperModule,
        ReactiveFormsModule,
        CovalentChipsModule,
        FieldPoliciesModule,
        KyloServicesModule,
        //VisualQuery2Module,
       // WranglerModule,

    ],
    providers:[
        FileMetadataTransformService,
        PreviewSchemaService,
        PreviewRawService,
        TransformResponseTableBuilder
    ]

})
export class PreviewSchemaModule {
}

@NgModule({
    imports: [
        PreviewSchemaModule,
        UIRouterModule.forChild({
            states: [
                {
                    name: "catalog.datasource.preview",
                    url: "/preview",
                    component: PreviewSchemaComponent
                }
            ]
        })
    ]
})
export class PreviewSchemaRouterModule {
}

import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {StateService, UIRouterModule} from "@uirouter/angular";

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
import {DatasetSimpleTableComponent} from "./dataset-simple-table.component";
import {DatasetSchemaDefinitionComponent} from "./dataset-schema-definition.component";
import {DatasetPreviewComponent} from "./preview/dataset-preview.component";
import {DatasetPreviewContainerComponent} from "./preview/dataset-preview-container.component";
import {DatasetPreviewDialogComponent} from "./preview-dialog/dataset-preview-dialog.component";
import {DatasetPreviewService} from "./service/dataset-preview.service";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CatalogPreviewDatasetComponent} from "./catalog-preview-dataset.component";
import {CatalogComponent} from "../../catalog.component";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {DataSource} from "../../api/models/datasource";
import {MatCardModule} from "@angular/material/card";
import {LoadMode} from "../../../model/feed/feed.model";

@NgModule({
    declarations: [
        SatusDialogComponent,
        SchemaParseSettingsDialog,
        DatasetSimpleTableComponent,
        DatasetSchemaDefinitionComponent,
        DatasetPreviewComponent,
        DatasetPreviewContainerComponent,
        DatasetPreviewDialogComponent,
        CatalogPreviewDatasetComponent
    ],
    entryComponents: [
        SatusDialogComponent,
        SchemaParseSettingsDialog,
        DatasetPreviewDialogComponent
    ],
    exports:[
        DatasetSimpleTableComponent,
        SchemaParseSettingsDialog,
        DatasetSchemaDefinitionComponent,
        DatasetPreviewComponent,
        DatasetPreviewContainerComponent,
        DatasetPreviewDialogComponent,
        CatalogPreviewDatasetComponent
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentNotificationsModule,
        CovalentLoadingModule,
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
        MatCardModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatToolbarModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatStepperModule,
        ReactiveFormsModule,
        CovalentChipsModule,
        FieldPoliciesModule,
        KyloServicesModule,
        CovalentLayoutModule,
        UIRouterModule
        //VisualQuery2Module,
       // WranglerModule,

    ],
    providers:[
        FileMetadataTransformService,
        PreviewSchemaService,
        PreviewRawService,
        TransformResponseTableBuilder,
        DatasetPreviewService
    ]

})
export class PreviewSchemaModule {
}

@NgModule({
    imports: [
        PreviewSchemaModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        UIRouterModule.forChild({
            states: [
                {
                    name: "catalog.datasource.preview",
                    url: "/preview",
                    component:CatalogPreviewDatasetComponent,
                    params:{autoSelectSingleDataSet:true,
                            displayInCard:true},
                    resolve: [
                        {
                            token: 'displayInCard',
                            resolveFn: () => true
                        },
                        {
                            token: 'autoSelectSingleDataSet',
                            resolveFn: () => true
                        }
                    ]

                }
            ]
        })
    ]
})
export class PreviewSchemaRouterModule {
}

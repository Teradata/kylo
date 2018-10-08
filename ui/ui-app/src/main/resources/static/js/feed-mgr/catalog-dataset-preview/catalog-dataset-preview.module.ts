import {MatExpansionModule} from "@angular/material/expansion";
import {RemoteFilesModule} from "../catalog/datasource/files/remote-files.module";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
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
import {CovalentChipsModule} from "@covalent/core/chips";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatStepperModule} from "@angular/material/stepper";
import {SelectDatasetStepComponent} from "./preview-stepper/select-dataset-step.component";
import {PreviewDatasetStepComponent} from "./preview-stepper/preview-dataset-step.component";
import {DatasetPreviewStepperComponent} from "./preview-stepper/dataset-preview-stepper.component";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";
import {PreviewSchemaModule} from "../catalog/datasource/preview-schema/preview-schema.module";
import {MatToolbarModule} from "@angular/material/toolbar";
import {DatasetPreviewStepperDialogComponent} from "./preview-stepper/dataset-preview-stepper-dialog.component";
import {TablesModule} from "../catalog/datasource/tables/tables.module";
import {UploadModule} from "../catalog/datasource/upload/upload.module";
import {DatasetPreviewStepperService} from "./preview-stepper/dataset-preview-stepper.service";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {MatTooltipModule} from "@angular/material/tooltip";
import {CatalogModule} from "../catalog/catalog.module";
import {UIRouterModule} from "@uirouter/angular";


@NgModule({
    declarations: [
        SelectDatasetStepComponent,
        DatasetPreviewStepperComponent,
        PreviewDatasetStepComponent,
        DatasetPreviewStepperDialogComponent
    ],
    entryComponents: [
        DatasetPreviewStepperDialogComponent
    ],
    exports:[
        SelectDatasetStepComponent,
        DatasetPreviewStepperComponent,
        PreviewDatasetStepComponent,
        DatasetPreviewStepperDialogComponent
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentNotificationsModule,
        CovalentDialogsModule,
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
        MatToolbarModule,
        MatTooltipModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatStepperModule,
        ReactiveFormsModule,
        CovalentChipsModule,
        MatExpansionModule,
        RemoteFilesModule,
        CatalogModule,
        PreviewSchemaModule,
        TablesModule,
        UploadModule,
        KyloServicesModule,
        UIRouterModule
    ],
    providers:[
        DatasetPreviewStepperService
    ]

})
export class CatalogDatasetPreviewModule {
}



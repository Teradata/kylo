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
import {CovalentLoadingModule, TdLoadingService} from "@covalent/core/loading";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CatalogPreviewDatasetComponent} from "./catalog-preview-dataset.component";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {MatCardModule} from "@angular/material/card";
import {DatasetPreviewContainerAccordionComponent} from './preview/dataset-preview-container-accordion.component';
import {PreviewSchemaComponent, SchemaDefinitionComponent, SimpleTableComponent} from './preview-schema.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {CatalogService} from '../../api/services/catalog.service';
import {catchError, finalize} from 'rxjs/operators';
import {DatasetInfoDescriptionComponent} from './dataset/dataset-info/dataset-info-description/dataset-info-description.component';
import {DatasetInfoTitleComponent} from './dataset/dataset-info/dataset-info-title/dataset-info-title.component';
import {DatasetInfoTagsComponent} from './dataset/dataset-info/dataset-info-tags/dataset-info-tags.component';
import {DatasetInfoComponent} from './dataset/dataset-info/dataset-info.component';
import {DatasetUsageComponent} from './dataset/dataset-usage/dataset-usage.component';
import {DatasetColumnProfileComponent} from './dataset/dataset-column-profile/dataset-column-profile.component';
import {DatasetSampleContentComponent} from './dataset/dataset-sample-content/dataset-sample-content.component';
import {DatasetLoadingService} from './dataset/dataset-loading-service';
import {DatasetService} from './dataset/dataset-service';
import {KyloFeedManagerModule} from '../../../feed-mgr.module';

@NgModule({
    declarations: [
        SatusDialogComponent,
        SchemaParseSettingsDialog,
        DatasetSimpleTableComponent,
        DatasetSchemaDefinitionComponent,
        DatasetPreviewComponent,
        DatasetPreviewContainerComponent,
        DatasetPreviewDialogComponent,
        CatalogPreviewDatasetComponent,
        PreviewSchemaComponent,
        DatasetPreviewContainerAccordionComponent,
        DatasetInfoDescriptionComponent,
        DatasetInfoTitleComponent,
        DatasetInfoTagsComponent,
        DatasetInfoComponent,
        DatasetUsageComponent,
        DatasetColumnProfileComponent,
        DatasetSampleContentComponent,
        SimpleTableComponent,
        SchemaDefinitionComponent,
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
        KyloFeedManagerModule,
        MatButtonModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatDialogModule,
        MatDividerModule,
        MatExpansionModule,
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
        DatasetPreviewService,
        DatasetLoadingService,
        DatasetService,
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
                    url: "/preview/:datasetId",
                    component:CatalogPreviewDatasetComponent,
                    params: {
                        autoSelectSingleDataSet:true,
                        displayInCard:true,
                        datasetId:null,
                        objectsToPreview:null},
                    resolve: [
                        {
                            token: 'displayInCard',
                            resolveFn: resolveTrue
                        },
                        {
                            token: 'autoSelectSingleDataSet',
                            resolveFn: resolveTrue
                        },
                        {
                            token:'objectsToPreview',
                            deps:[StateService],
                            resolveFn: resolveParams
                        },
                        {
                            token: "dataset",
                            deps: [CatalogService, StateService, TdLoadingService],
                            resolveFn: resolveDataset
                        },
                    ]

                }
            ]
        })
    ]
})
export class PreviewSchemaRouterModule {
}

export function resolveTrue() {
    return true;
}

export function resolveParams(state:StateService) {
    let params = state.transition.params();
    return params.objectsToPreview;
}

export function resolveDataset(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(CatalogPreviewDatasetComponent.LOADER);
    let id = state.transition.params().datasetId;
    if (id) {
        return catalog.getDataset(id)
            .pipe(finalize(() => {
                loading.resolve(CatalogPreviewDatasetComponent.LOADER)
            }))
            .pipe(catchError((err) => {
                console.log('Failed to transition to dataset preview', err);
                return state.go("catalog")
            }))
            .toPromise();
    } else {
        //dataset id is optional, i.e. dataset hasn't been saved/annotated yet
        return undefined;
    }
}

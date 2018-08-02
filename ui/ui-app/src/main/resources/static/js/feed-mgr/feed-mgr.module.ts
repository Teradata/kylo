import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDialogModule} from '@angular/material/dialog';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {
    entityAccessControlDialogServiceProvider
} from "./services/angular2";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import {RestUrlService} from "./services/RestUrlService";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";
import {VisualQueryService} from "./services/VisualQueryService";
import CategoriesService from "./services/CategoriesService";
import {KyloCommonModule} from "../common/common.module";
import {UpgradeModule} from "@angular/upgrade/static";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";
import {UiComponentsService} from "./services/UiComponentsService";
import {DefaultImportService} from "./services/ImportService";
import {RegisterTemplateServiceFactory} from "./services/RegisterTemplateServiceFactory";
import {FeedCreationErrorService, FeedErrorDialogController} from "./services/FeedCreationErrorService";
import { DefaultFeedPropertyService } from "./services/DefaultFeedPropertyService";
import { CronExpressionPreview } from "./shared/cron-expression-preview/cron-expression-preview.component";

import {TranslateModule} from "@ngx-translate/core";
import { MatFormFieldModule } from "@angular/material/form-field";
import { DomainTypesService } from "./services/DomainTypesService";
import { DatasourcesService } from "./services/DatasourcesService";
import { HiveService } from "./services/HiveService";
import { CodeMirrorService } from "./services/CodeMirrorService";
import { DBCPTableSchemaService } from "./services/DBCPTableSchemaService";
import { EditFeedNifiPropertiesService } from "./services/EditFeedNifiPropertiesService";
import { FeedDetailsProcessorRenderingHelper } from "./services/FeedDetailsProcessorRenderingHelper";
import { FeedInputProcessorPropertiesTemplateService } from "./services/FeedInputProcessorPropertiesTemplateService";
import { FeedTagService } from "./services/FeedTagService";
import { SlaService } from "./services/SlaService";
import { RegisterTemplatePropertyService } from "./services/RegisterTemplatePropertyService";
import { FattableService } from "./services/fattable/FattableService";

@NgModule({
    declarations: [
        CronExpressionPreview,
        FeedSavingDialogController,
        FeedErrorDialogController
    ],
    entryComponents: [
        CronExpressionPreview,
        FeedSavingDialogController,
        FeedErrorDialogController
    ],
    imports: [
        CommonModule,
        UpgradeModule,
        KyloCommonModule,
        TranslateModule,
        MatFormFieldModule,
        MatToolbarModule,
        MatProgressSpinnerModule,
        CovalentDialogsModule,
        MatDialogModule,
        MatListModule,
        MatButtonModule,
        MatSnackBarModule
    ],
    providers: [
        CategoriesService,
        CodeMirrorService,
        DatasourcesService,
        DBCPTableSchemaService,
        DefaultFeedPropertyService,
        DomainTypesService,
        EditFeedNifiPropertiesService,
        EntityAccessControlService,
        entityAccessControlDialogServiceProvider,
        FattableService,
        FeedCreationErrorService,
        FeedDetailsProcessorRenderingHelper,
        FeedInputProcessorPropertiesTemplateService,
        FeedSecurityGroups,
        FeedService,
        FeedTagService,
        HiveService,
        DefaultImportService,
        RegisterTemplateServiceFactory,
        RegisterTemplatePropertyService,
        RestUrlService,
        SlaService,
        UiComponentsService,
        VisualQueryService,
        PolicyInputFormService,
    ],
    exports:[
        CronExpressionPreview
    ]
})
export class KyloFeedManagerModule {
}

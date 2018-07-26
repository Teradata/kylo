import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDialogModule} from '@angular/material/dialog';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {
    domainTypesServiceProvider,
    feedDetailsProcessorRenderingHelperProvider,
    feedInputProcessorPropertiesTemplateServiceProvider,
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
        MatDialogModule,
        MatListModule,
        MatButtonModule
    ],
    providers: [
        CategoriesService,
        EntityAccessControlService,
        entityAccessControlDialogServiceProvider,
        FeedService,
        VisualQueryService,
        domainTypesServiceProvider,
        UiComponentsService,
        DefaultFeedPropertyService,
        feedInputProcessorPropertiesTemplateServiceProvider,
        feedDetailsProcessorRenderingHelperProvider,
        PolicyInputFormService,
        RestUrlService,
        FeedSecurityGroups,
        DefaultImportService,
        RegisterTemplateServiceFactory,
        FeedCreationErrorService
    ],
    exports:[
        CronExpressionPreview
    ]
})
export class KyloFeedManagerModule {
}

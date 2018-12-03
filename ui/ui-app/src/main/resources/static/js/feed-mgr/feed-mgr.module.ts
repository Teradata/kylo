import {CommonModule} from "@angular/common";
import {NgModule, Injector, ViewContainerRef} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from '@angular/material/button';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatListModule} from '@angular/material/list';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatToolbarModule} from '@angular/material/toolbar';
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from '@covalent/core/dialogs';
import {TranslateModule} from "@ngx-translate/core";

import {KyloCommonModule} from "../common/common.module";
import {entityAccessControlDialogServiceProvider} from "./services/angular2";
import CategoriesService from "./services/CategoriesService";
import {CodeMirrorService} from "./services/CodeMirrorService";
import {DatasourcesService} from "./services/DatasourcesService";
import {DBCPTableSchemaService} from "./services/DBCPTableSchemaService";
import {DefaultFeedPropertyService} from "./services/DefaultFeedPropertyService";
import {DomainTypesService} from "./services/DomainTypesService";
import {EditFeedNifiPropertiesService} from "./services/EditFeedNifiPropertiesService";
import {FattableService} from "./services/fattable/FattableService";
import {FeedCreationErrorService, FeedErrorDialogController} from "./services/FeedCreationErrorService";
import {FeedDetailsProcessorRenderingHelper} from "./services/FeedDetailsProcessorRenderingHelper";
import {FeedInputProcessorPropertiesTemplateService} from "./services/FeedInputProcessorPropertiesTemplateService";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";
import {FeedTagService} from "./services/FeedTagService";
import {HiveService} from "./services/HiveService";
import {DefaultImportService} from "./services/ImportService";
import {NiFiService} from "./services/NiFiService";
import {RegisterTemplatePropertyService} from "./services/RegisterTemplatePropertyService";
import {RegisterTemplateServiceFactory} from "./services/RegisterTemplateServiceFactory";
import {RestUrlService} from "./services/RestUrlService";
import {SlaService} from "./services/SlaService";
import {UiComponentsService} from "./services/UiComponentsService";
import {VisualQueryService} from "./services/VisualQueryService";
import {CronExpressionPreview} from "./shared/cron-expression-preview/cron-expression-preview.component";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {ApplyDomainTypesDialogComponent} from "./shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {DynamicFormModule} from "./shared/dynamic-form/dynamic-form.module";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import { VisualQueryPainterService } from "./visual-query/transform-data/visual-query-table/visual-query-painter.service";
import { INJECTOR, VIEW_CONTAINER_REF } from "./visual-query/transform-data/transform-data.component";
// import {PropertyListModule} from "./shared/property-list/property-list.module";

@NgModule({
    declarations: [
        CronExpressionPreview,
        FeedSavingDialogController,
        FeedErrorDialogController,
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent
    ],
    entryComponents: [
        CronExpressionPreview,
        FeedSavingDialogController,
        FeedErrorDialogController,
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent
    ],
    imports: [
        CommonModule,
        UpgradeModule,
        KyloCommonModule,
        TranslateModule.forChild(),
        MatFormFieldModule,
        MatToolbarModule,
        MatProgressSpinnerModule,
        CovalentDialogsModule,
        MatDialogModule,
        MatListModule,
        MatButtonModule,
        MatSnackBarModule,
        DynamicFormModule,
        // PropertyListModule,
        FlexLayoutModule,
        CovalentDataTableModule
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
        VisualQueryPainterService,
        {provide: INJECTOR, useExisting: Injector},
        {provide : VIEW_CONTAINER_REF, useExisting : ViewContainerRef},
        PolicyInputFormService,
        NiFiService
    ],
    exports: [
        CronExpressionPreview
    ]
})
export class KyloFeedManagerModule {
}

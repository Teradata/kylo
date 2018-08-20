import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDialogModule} from '@angular/material/dialog';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {moduleName} from "./module-name";
import {
    entityAccessControlDialogServiceProvider, feedPropertyServiceProvider
} from "./services/angular2";
import {NiFiService} from "./services/NiFiService";
import {DynamicFormModule} from "./shared/dynamic-form/dynamic-form.module";
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
import {PropertyListModule} from "./shared/property-list/property-list.module";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {ApplyDomainTypesDialogComponent} from "./shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {FlexLayoutModule} from "@angular/flex-layout";

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
        PropertyListModule,
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
        PolicyInputFormService,
        feedPropertyServiceProvider,
        NiFiService
    ],
    exports:[
        CronExpressionPreview
    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {
        console.log("Loading KyloFeedManagerModule")
        require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");
    }
}

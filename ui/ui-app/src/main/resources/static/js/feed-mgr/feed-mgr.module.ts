import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatListModule} from '@angular/material/list';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from "@angular/material/select";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatToolbarModule} from '@angular/material/toolbar';
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentChipsModule} from "@covalent/core/chips";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from '@covalent/core/dialogs';
import {TranslateModule} from "@ngx-translate/core";
import {DynamicFormModule} from "../../lib/dynamic-form/dynamic-form.module";

import {KyloCommonModule} from "../common/common.module";
import {AccessControlService} from "../services/AccessControlService";
import {UserGroupService} from "../services/UserGroupService";
import {DefineFeedService} from './feeds/define-feed-ng2/services/define-feed.service';
import {FeedAccessControlService} from './feeds/define-feed-ng2/services/feed-access-control.service';
import {FeedLoadingService} from './feeds/define-feed-ng2/services/feed-loading-service';
import {CategoriesService} from "./services/CategoriesService";
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
import {SlaService} from "./services/sla.service";
import {UiComponentsService} from "./services/UiComponentsService";
import {VisualQueryService} from "./services/VisualQueryService";
import {CronExpressionPreview} from "./shared/cron-expression-preview/cron-expression-preview.component";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {ApplyDomainTypesDialogComponent} from "./shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {EntityAccessControlComponent} from "./shared/entity-access-control/entity-access-control.component";
import {EntityAccessControlDialogController, EntityAccessControlDialogService} from "./shared/entity-access-control/EntityAccessControlDialogService";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import {PolicyInputFormController} from "./shared/policy-input-form/policy-input-form.component";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import {PropertyListModule} from "./shared/property-list/property-list.module";
import {SqlEditorModule} from "./shared/sql-editor/sql-editor.module";

@NgModule({
    imports: [
        // PropertyListModule,
        CommonModule,
        CovalentChipsModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        DynamicFormModule,
        FlexLayoutModule,
        FormsModule,
        KyloCommonModule,
        MatButtonModule,
        MatDialogModule,
        MatFormFieldModule,
        MatListModule,
        MatProgressSpinnerModule,
        MatSnackBarModule,
        MatToolbarModule,
        PropertyListModule,
        ReactiveFormsModule,
        SqlEditorModule,
        TranslateModule.forChild(),
        UpgradeModule,
        MatCheckboxModule,
        MatSelectModule,
        MatDatepickerModule,
    ],
    declarations: [
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent,
        CronExpressionPreview,
        DomainTypeConflictDialogComponent,
        EntityAccessControlComponent,
        EntityAccessControlDialogController,
        FeedErrorDialogController,
        FeedSavingDialogController,
        PolicyInputFormController
    ],
    entryComponents: [
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent,
        CronExpressionPreview,
        DomainTypeConflictDialogComponent,
        EntityAccessControlDialogController,
        FeedErrorDialogController,
        FeedSavingDialogController,
        PolicyInputFormController,
    ],
    exports: [
        CronExpressionPreview,
        EntityAccessControlComponent,
    ],
    providers: [
        AccessControlService,
        CategoriesService,
        CodeMirrorService,
        DatasourcesService,
        DBCPTableSchemaService,
        DefaultFeedPropertyService,
        DefaultImportService,
        DefineFeedService,
        DomainTypesService,
        EditFeedNifiPropertiesService,
        EntityAccessControlDialogService,
        EntityAccessControlService,
        FattableService,
        FeedAccessControlService,
        FeedCreationErrorService,
        FeedDetailsProcessorRenderingHelper,
        FeedInputProcessorPropertiesTemplateService,
        FeedLoadingService,
        DefaultFeedPropertyService,
        FeedSecurityGroups,
        FeedService,
        FeedTagService,
        HiveService,
        NiFiService,
        PolicyInputFormService,
        RegisterTemplatePropertyService,
        RegisterTemplateServiceFactory,
        RestUrlService,
        SlaService,
        UiComponentsService,
        UserGroupService,
        VisualQueryService,
    ]
})
export class KyloFeedManagerModule {
}

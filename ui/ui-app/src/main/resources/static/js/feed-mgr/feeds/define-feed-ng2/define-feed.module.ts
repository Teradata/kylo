import {CommonModule} from "@angular/common";
import {Compiler, COMPILER_OPTIONS, CompilerFactory, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatGridListModule} from "@angular/material/grid-list";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from "@angular/material/list";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatStepperModule} from "@angular/material/stepper";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {CovalentChipsModule} from "@covalent/core/chips";
import {CovalentCommonModule} from '@covalent/core/common';
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentExpansionPanelModule} from "@covalent/core/expansion-panel";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMediaModule} from "@covalent/core/media";
import {CovalentMessageModule} from "@covalent/core/message";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentVirtualScrollModule} from "@covalent/core/virtual-scroll";
import {CovalentDynamicFormsModule} from "@covalent/dynamic-forms";
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";
import {NvD3Module} from 'ng2-nvd3';

import {KyloCommonModule} from "../../../common/common.module";
import {CatalogApiModule} from "../../catalog/api/catalog-api.module";
import {RemoteFilesModule} from "../../catalog/datasource/files/remote-files.module";
import {KyloFeedManagerModule} from "../../feed-mgr.module";
import {DynamicFormModule} from "../../../../lib/dynamic-form/dynamic-form.module";
import {FeedPreconditionModule} from "../../shared/feed-precondition/feed-precondition.module";
import {FieldPoliciesModule} from "../../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../../shared/property-list/property-list.module";
import {VisualQueryModule} from "../../visual-query/visual-query.module";
import {defineFeedStates} from "./define-feed-states";
import {DefineFeedComponent} from "./define-feed.component";
import {FeedScheduleComponent} from "./summary/setup-guide-summary/feed-schedule/feed-schedule.component";
import {NewFeedDialogComponent} from "./new-feed-dialog/new-feed-dialog.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {CategoryAutocompleteComponent} from "./shared/category-autocomplete.component";
import {SystemFeedNameComponent} from "./shared/system-feed-name.component";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedStepCardComponent} from "./steps/define-feed-step-card/define-feed-step-card.component";
import {DefineFeedStepEditContentComponent} from "./steps/define-feed-step-card/define-feed-step-edit-content.component";
import {DefineFeedStepReadonlyContentComponent} from "./steps/define-feed-step-card/define-feed-step-readonly-content.component";
import {DefineFeedTableComponent, FilterPartitionFormulaPipe} from "./steps/define-table/define-feed-table.component";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {FeedDetailsProcessorFieldComponent} from "./steps/feed-details/feed-details-processor-field.component";
import {DefineFeedStepSourceSampleComponent} from "./steps/define-table/source-sample/define-feed-step-source-sample.component";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {FeedLineageComponment} from "./summary/feed-lineage/feed-lineage.componment";
import {OverviewComponent} from './summary/overview/overview.component';
import {MatExpansionModule} from "@angular/material/expansion";
import {FeedSideNavService} from "./services/feed-side-nav.service";
import {ProfileContainerComponent} from './summary/profile/container/profile-container.component';
import {ProfileStatsComponent} from './summary/profile/container/stats/profile-stats.component';
import {ProfileHistoryComponent} from './summary/profile/history/profile-history.component';
import {ProfileComponent} from './summary/profile/profile.component';
import {ProfileInvalidComponent} from './summary/profile/container/invalid/profile-invalid.component';
import {ProfileValidComponent} from './summary/profile/container/valid/profile-valid.component';
import {DefineFeedStepCustomContentComponent} from "./steps/define-feed-step-card/define-feed-step-custom-content.component";
import {DefineFeedPermissionsComponent} from "./steps/permissions/define-feed-permissions.component";
import {DefineFeedPropertiesComponent} from "./steps/properties/define-feed-properties.component";
import {FeedInfoItemComponent} from "./summary/setup-guide-summary/feed-info/feed-info-item.component";
import {FeedInfoScheduleComponent} from "./summary/setup-guide-summary/feed-info/feed-info-schedule/feed-info-schedule.component";
import {FeedInfoDescriptionComponent} from "./summary/setup-guide-summary/feed-info/feed-info-description/feed-info-description.component";
import {FeedInfoCategoryComponent} from "./summary/setup-guide-summary/feed-info/feed-info-category/feed-info-category.component";
import {FeedInfoNameComponent} from "./summary/setup-guide-summary/feed-info/feed-info-name/feed-info-name.component";
import {FeedItemInfoService} from "./summary/setup-guide-summary/feed-info/feed-item-info.service";
import {FeedInfoTagsComponent} from "./summary/setup-guide-summary/feed-info/feed-info-tags/feed-info-tags.component";
import {FeedSlaComponent} from './summary/sla/feed-sla.component';
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {FeedNifiPropertiesService} from "./services/feed-nifi-properties.service";
import {FeedNifiPropertiesComponent} from "./steps/feed-details/feed-nifi-properties.component";
import {DefineFeedSourceSampleService} from "./steps/define-table/source-sample/define-feed-source-sample.service";
import {DefineFeedStepSourceComponent} from "./steps/source/define-feed-step-source.component";
import {KyloFeedModule} from "../../../../lib/feed/feed.module";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {CatalogDatasetPreviewModule} from "../../catalog-dataset-preview/catalog-dataset-preview.module";
import {CronExpressionPreviewModule} from '../../../../lib/cron-expression-preview/cron-expression-preview2.module';
import {SelectNetworkNodeComponent} from "./summary/feed-lineage/select-network-node.component";

import {FeedSetupGuideComponent} from "./summary/setup-guide-summary/feed-setup-guide/feed-setup-guide.component";
import {SetupGuideSummaryComponent} from "./summary/setup-guide-summary/setup-guide-summary.component";
import {FeedActivitySummaryComponent} from "./summary/feed-activity-summary/feed-activity-summary.component";
import {DefineFeedSideNavComponent} from "./steps/define-feed-side-nav/define-feed-side-nav.component";
import {FeedSummaryContainerComponent} from "./summary/feed-summary-container.component";
import {MatMenuModule} from "@angular/material/menu";


import {JobsListModule} from "../../../ops-mgr/jobs/jobs-list/jobs-list.module";
import {OpsManagerServicesModule} from "../../../ops-mgr/services/ops-manager-services.module";
import {KyloServicesModule} from "../../../services/services.module";
import {FeedAlertsComponent} from "./summary/feed-activity-summary/feed-alerts/feed-alerts.component";
import {FeedJobActivityComponent} from "./summary/feed-activity-summary/feed-job-activity/feed-job-activity.component";
import {FeedOperationsHealthInfoComponent} from "./summary/feed-activity-summary/feed-operations-health-info/feed-operations-health-info.component";
import {FeedUploadFileDialogComponent} from "./summary/feed-activity-summary/feed-upload-file-dialog/feed-upload-file-dialog.component";
import {SkipHeaderComponent} from "./shared/skip-header/skip-header.component";
import {DeployFeedDialogComponent} from "./summary/setup-guide-summary/deploy-feed-dialog/deploy-feed-dialog.component";
import {DefineFeedPermissionsDialogComponent} from "./steps/permissions/define-feed-permissions-dialog/define-feed-permissions-dialog.component";
import {FeedToolbarActions} from "./shared/feed-toolbar-actions";
import {ImportFeedComponent} from "../define-feed-ng2/import/import-feed.component";
import {FeedVersionsComponent} from "./summary/versions/feed-versions.component";
import {JoinPipe} from "./shared/pipes/JoinPipe";
import {VerboseTimeUnitPipe} from "./shared/pipes/VerboseTimeUnitPipe";
import {CharactersPipe} from "./shared/pipes/CharactersPipe";
import {SlaModule} from "../../sla/sla.module";
import {ExampleStepComponent} from './steps/example-step/example-step.component';
import {TruncatePipe} from './summary/profile/container/stats/truncate-pipe';
import {FeedStatsModule} from '../../../ops-mgr/feeds/feed-stats-ng2/feed-stats.module';
import {MetadataIndexingComponent} from "../../shared/metadata-indexing/metadata-indexing.component";
import {JitCompilerFactory} from '@angular/platform-browser-dynamic';
import {CovalentPagingModule} from '@covalent/core/paging';

export function createCompilerFn(c: CompilerFactory) {
        return c.createCompiler([{useJit: true}]);
    }

@NgModule({
    declarations: [
        DefineFeedComponent,
        DefineFeedSelectTemplateComponent,
        DefineFeedContainerComponent,
        DefineFeedStepCardComponent,
        DefineFeedStepSourceComponent,
        DefineFeedStepSourceSampleComponent,
        DefineFeedStepFeedDetailsComponent,
        DefineFeedStepReadonlyContentComponent,
        DefineFeedStepEditContentComponent,
        DefineFeedStepWranglerComponent,
        DefineFeedTableComponent,
        OverviewComponent,
        ProfileHistoryComponent,
        ProfileComponent,
        ProfileContainerComponent,
        ProfileStatsComponent,
        ProfileInvalidComponent,
        ProfileValidComponent,
        FeedScheduleComponent,
        FilterPartitionFormulaPipe,
        FeedLineageComponment,
        CategoryAutocompleteComponent,
        SystemFeedNameComponent,
        NewFeedDialogComponent,
        DefineFeedStepCustomContentComponent,
        DefineFeedPermissionsComponent,
        DefineFeedPermissionsDialogComponent,
        DefineFeedPropertiesComponent,
        FeedInfoItemComponent,
        FeedInfoNameComponent,
        FeedInfoScheduleComponent,
        FeedInfoDescriptionComponent,
        FeedInfoCategoryComponent,
        FeedInfoTagsComponent,
        FeedSlaComponent,
        SelectNetworkNodeComponent,
        FeedInfoTagsComponent,
        FeedNifiPropertiesComponent,
        FeedInfoTagsComponent,
        FeedDetailsProcessorFieldComponent,
        FeedInfoTagsComponent,
        FeedSetupGuideComponent,
        SetupGuideSummaryComponent,
        FeedActivitySummaryComponent,
        DefineFeedSideNavComponent,
        FeedSummaryContainerComponent,
        FeedAlertsComponent,
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent,
        FeedUploadFileDialogComponent,
        SkipHeaderComponent,
        DeployFeedDialogComponent,
        FeedToolbarActions,
        ImportFeedComponent,
        FeedToolbarActions,
        FeedVersionsComponent,
        JoinPipe,
        VerboseTimeUnitPipe,
        CharactersPipe,
        ExampleStepComponent,
        TruncatePipe,
        MetadataIndexingComponent,
    ],
    entryComponents:[
        NewFeedDialogComponent,
        FeedUploadFileDialogComponent,
        DeployFeedDialogComponent,
        DefineFeedPermissionsDialogComponent
    ],
    providers:[
        FilterPartitionFormulaPipe,
        FeedSideNavService,
        FeedItemInfoService,
        FeedNifiPropertiesService,
        DefineFeedSourceSampleService,
        {provide: COMPILER_OPTIONS, useValue: {}, multi: true},
        {provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS]},
        {provide: Compiler, useFactory: createCompilerFn, deps: [CompilerFactory]},
    ],
    imports: [
        CommonModule,
        CovalentCommonModule,
        CovalentChipsModule,
        CovalentDataTableModule,
        CovalentPagingModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentMediaModule,
        CovalentNotificationsModule,
        CovalentVirtualScrollModule,
        CovalentDynamicFormsModule,
        CovalentExpansionPanelModule,
        CronExpressionPreviewModule,
        FlexLayoutModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        KyloServicesModule,
        RemoteFilesModule,
        FieldPoliciesModule,
        FeedPreconditionModule,
        MatCardModule,
        FormsModule,
        DynamicFormModule,
        MatRadioModule,
        MatAutocompleteModule,
        MatInputModule,
        MatFormFieldModule,
        MatIconModule,
        MatListModule,
        MatCheckboxModule,
        MatOptionModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatDividerModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        MatNativeDateModule,
        MatButtonModule,
        MatSnackBarModule,
        MatDialogModule,
        MatGridListModule,
        MatProgressBarModule,
        MatTooltipModule,
        MatButtonToggleModule,
        PropertyListModule,
        TranslateModule,
        VisualQueryModule,
        NvD3Module,
        MatStepperModule,
        MatExpansionModule,
        CovalentMessageModule,
        MatProgressSpinnerModule,
        CatalogDatasetPreviewModule,
        KyloFeedModule,
        CatalogApiModule,
        MatMenuModule,
        OpsManagerServicesModule,
        FeedStatsModule,
        JobsListModule,
        SlaModule,
        UIRouterModule.forChild({states: defineFeedStates})
    ]
})
export class DefineFeedModule {
}

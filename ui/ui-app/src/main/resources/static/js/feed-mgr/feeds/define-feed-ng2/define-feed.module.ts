import {CommonModule} from "@angular/common";
import {Compiler, COMPILER_OPTIONS, CompilerFactory, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from "@angular/material/dialog";
import {MatDividerModule} from "@angular/material/divider";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatGridListModule} from "@angular/material/grid-list";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatStepperModule} from "@angular/material/stepper";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {JitCompilerFactory} from '@angular/platform-browser-dynamic';
import {CovalentChipsModule} from "@covalent/core/chips";
import {CovalentCommonModule} from '@covalent/core/common';
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentExpansionPanelModule} from "@covalent/core/expansion-panel";
import {CovalentLayoutModule} from "@covalent/core/layout";
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

import {CronExpressionPreviewModule} from '../../../../lib/cron-expression-preview/cron-expression-preview2.module';
import {DynamicFormModule} from "../../../../lib/dynamic-form/dynamic-form.module";
import {KyloFeedModule} from "../../../../lib/feed/feed.module";
import {KyloCommonModule} from "../../../common/common.module";
import {FeedStatsModule} from '../../../ops-mgr/feeds/feed-stats-ng2/feed-stats.module';
import {JobsListModule} from "../../../ops-mgr/jobs/jobs-list/jobs-list.module";
import {OpsManagerServicesModule} from "../../../ops-mgr/services/ops-mgr.services.module";
import {KyloServicesModule} from "../../../services/services.module";
import {CatalogDatasetPreviewModule} from "../../catalog-dataset-preview/catalog-dataset-preview.module";
import {CatalogApiModule} from "../../catalog/api/catalog-api.module";
import {RemoteFilesModule} from "../../catalog/datasource/files/remote-files.module";
import {KyloFeedManagerModule} from "../../feed-mgr.module";
import {FeedPreconditionModule} from "../../shared/feed-precondition/feed-precondition.module";
import {FieldPoliciesModule} from "../../shared/field-policies-angular2/field-policies.module";
import {MetadataIndexingComponent} from "../../shared/metadata-indexing/metadata-indexing.component";
import {PropertyListModule} from "../../shared/property-list/property-list.module";
import {SharedModule} from "../../shared/shared.modules";
import {SlaModule} from "../../sla/sla.module";
import {VisualQueryModule} from "../../visual-query/visual-query.module";
import {ImportFeedComponent} from "../define-feed-ng2/import/import-feed.component";
import {defineFeedStates} from "./define-feed-states";
import {DefineFeedComponent} from "./define-feed.component";
import {NewFeedDialogComponent} from "./new-feed-dialog/new-feed-dialog.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {FeedNifiPropertiesService} from "./services/feed-nifi-properties.service";
import {FeedSideNavService} from "./services/feed-side-nav.service";
import {CategoryAutocompleteComponent} from "./shared/category-autocomplete.component";
import {FeedToolbarActions} from "./shared/feed-toolbar-actions";
import {CharactersPipe} from "./shared/pipes/CharactersPipe";
import {JoinPipe} from "./shared/pipes/JoinPipe";
import {VerboseTimeUnitPipe} from "./shared/pipes/VerboseTimeUnitPipe";
import {SkipHeaderComponent} from "./shared/skip-header/skip-header.component";
import {SystemFeedNameComponent} from "./shared/system-feed-name.component";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedSideNavComponent} from "./steps/define-feed-side-nav/define-feed-side-nav.component";
import {DefineFeedStepCardComponent} from "./steps/define-feed-step-card/define-feed-step-card.component";
import {DefineFeedStepCustomContentComponent} from "./steps/define-feed-step-card/define-feed-step-custom-content.component";
import {DefineFeedStepEditContentComponent} from "./steps/define-feed-step-card/define-feed-step-edit-content.component";
import {DefineFeedStepReadonlyContentComponent} from "./steps/define-feed-step-card/define-feed-step-readonly-content.component";
import {DefineFeedTableComponent, FilterPartitionFormulaPipe} from "./steps/define-table/define-feed-table.component";
import {DefineFeedSourceSampleService} from "./steps/define-table/source-sample/define-feed-source-sample.service";
import {DefineFeedStepSourceSampleComponent} from "./steps/define-table/source-sample/define-feed-step-source-sample.component";
import {ExampleStepComponent} from './steps/example-step/example-step.component';
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {FeedDetailsProcessorFieldComponent} from "./steps/feed-details/feed-details-processor-field.component";
import {FeedNifiPropertiesComponent} from "./steps/feed-details/feed-nifi-properties.component";
import {DefineFeedPermissionsDialogComponent} from "./steps/permissions/define-feed-permissions-dialog/define-feed-permissions-dialog.component";
import {DefineFeedPermissionsComponent} from "./steps/permissions/define-feed-permissions.component";
import {DefineFeedPropertiesComponent} from "./steps/properties/define-feed-properties.component";
import {DefineFeedStepSourceComponent} from "./steps/source/define-feed-step-source.component";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {FeedActivitySummaryComponent} from "./summary/feed-activity-summary/feed-activity-summary.component";
import {FeedAlertsComponent} from "./summary/feed-activity-summary/feed-alerts/feed-alerts.component";
import {FeedJobActivityComponent} from "./summary/feed-activity-summary/feed-job-activity/feed-job-activity.component";
import {FeedOperationsHealthInfoComponent} from "./summary/feed-activity-summary/feed-operations-health-info/feed-operations-health-info.component";
import {FeedUploadFileDialogComponent} from "./summary/feed-activity-summary/feed-upload-file-dialog/feed-upload-file-dialog.component";
import {FeedLineageComponment} from "./summary/feed-lineage/feed-lineage.componment";
import {SelectNetworkNodeComponent} from "./summary/feed-lineage/select-network-node.component";
import {FeedSummaryContainerComponent} from "./summary/feed-summary-container.component";
import {OverviewComponent} from './summary/overview/overview.component';
import {ProfileInvalidComponent} from './summary/profile/container/invalid/profile-invalid.component';
import {ProfileContainerComponent} from './summary/profile/container/profile-container.component';
import {ProfileStatsComponent} from './summary/profile/container/stats/profile-stats.component';
import {TruncatePipe} from './summary/profile/container/stats/truncate-pipe';
import {ProfileValidComponent} from './summary/profile/container/valid/profile-valid.component';
import {ProfileHistoryComponent} from './summary/profile/history/profile-history.component';
import {ProfileComponent} from './summary/profile/profile.component';
import {DeployFeedDialogComponent} from "./summary/setup-guide-summary/deploy-feed-dialog/deploy-feed-dialog.component";
import {FeedInfoCategoryComponent} from "./summary/setup-guide-summary/feed-info/feed-info-category/feed-info-category.component";
import {FeedInfoDescriptionComponent} from "./summary/setup-guide-summary/feed-info/feed-info-description/feed-info-description.component";
import {FeedInfoItemComponent} from "./summary/setup-guide-summary/feed-info/feed-info-item.component";
import {FeedInfoNameComponent} from "./summary/setup-guide-summary/feed-info/feed-info-name/feed-info-name.component";
import {FeedInfoScheduleComponent} from "./summary/setup-guide-summary/feed-info/feed-info-schedule/feed-info-schedule.component";
import {FeedInfoTagsComponent} from "./summary/setup-guide-summary/feed-info/feed-info-tags/feed-info-tags.component";
import {FeedItemInfoService} from "./summary/setup-guide-summary/feed-info/feed-item-info.service";
import {FeedScheduleComponent} from "./summary/setup-guide-summary/feed-schedule/feed-schedule.component";
import {FeedSetupGuideComponent} from "./summary/setup-guide-summary/feed-setup-guide/feed-setup-guide.component";
import {SetupGuideSummaryComponent} from "./summary/setup-guide-summary/setup-guide-summary.component";
import {FeedSlaComponent} from './summary/sla/feed-sla.component';
import {FeedVersionsComponent} from "./summary/versions/feed-versions.component";

export function createCompilerFn(c: CompilerFactory) {
    return c.createCompiler([{useJit: true}]);
}

@NgModule({
    declarations: [
        CategoryAutocompleteComponent,
        CharactersPipe,
        DefineFeedComponent,
        DefineFeedContainerComponent,
        DefineFeedPermissionsComponent,
        DefineFeedPermissionsDialogComponent,
        DefineFeedPropertiesComponent,
        DefineFeedSelectTemplateComponent,
        DefineFeedSideNavComponent,
        DefineFeedStepCardComponent,
        DefineFeedStepCustomContentComponent,
        DefineFeedStepEditContentComponent,
        DefineFeedStepFeedDetailsComponent,
        DefineFeedStepReadonlyContentComponent,
        DefineFeedStepSourceComponent,
        DefineFeedStepSourceSampleComponent,
        DefineFeedStepWranglerComponent,
        DefineFeedTableComponent,
        DeployFeedDialogComponent,
        ExampleStepComponent,
        FeedActivitySummaryComponent,
        FeedAlertsComponent,
        FeedDetailsProcessorFieldComponent,
        FeedInfoCategoryComponent,
        FeedInfoDescriptionComponent,
        FeedInfoItemComponent,
        FeedInfoNameComponent,
        FeedInfoScheduleComponent,
        FeedInfoTagsComponent,
        FeedInfoTagsComponent,
        FeedInfoTagsComponent,
        FeedInfoTagsComponent,
        FeedJobActivityComponent,
        FeedLineageComponment,
        FeedNifiPropertiesComponent,
        FeedOperationsHealthInfoComponent,
        FeedScheduleComponent,
        FeedSetupGuideComponent,
        FeedSlaComponent,
        FeedSummaryContainerComponent,
        FeedToolbarActions,
        FeedToolbarActions,
        FeedUploadFileDialogComponent,
        FeedVersionsComponent,
        FilterPartitionFormulaPipe,
        ImportFeedComponent,
        JoinPipe,
        MetadataIndexingComponent,
        NewFeedDialogComponent,
        OverviewComponent,
        ProfileComponent,
        ProfileContainerComponent,
        ProfileHistoryComponent,
        ProfileInvalidComponent,
        ProfileStatsComponent,
        ProfileValidComponent,
        SelectNetworkNodeComponent,
        SetupGuideSummaryComponent,
        SkipHeaderComponent,
        SystemFeedNameComponent,
        TruncatePipe,
        VerboseTimeUnitPipe,
    ],
    entryComponents: [
        DefineFeedPermissionsDialogComponent,
        DeployFeedDialogComponent,
        FeedUploadFileDialogComponent,
        NewFeedDialogComponent,
    ],
    providers: [
        DefineFeedSourceSampleService,
        FeedItemInfoService,
        FeedNifiPropertiesService,
        FeedSideNavService,
        FilterPartitionFormulaPipe,
        {provide: COMPILER_OPTIONS, useValue: {}, multi: true},
        {provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS]},
        {provide: Compiler, useFactory: createCompilerFn, deps: [CompilerFactory]},
    ],
    imports: [
        CatalogApiModule,
        CatalogDatasetPreviewModule,
        CommonModule,
        CovalentChipsModule,
        CovalentCommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentDynamicFormsModule,
        CovalentExpansionPanelModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentMediaModule,
        CovalentMessageModule,
        CovalentNotificationsModule,
        CovalentSearchModule,
        CovalentVirtualScrollModule,
        CronExpressionPreviewModule,
        DynamicFormModule,
        FeedPreconditionModule,
        FeedStatsModule,
        FieldPoliciesModule,
        FlexLayoutModule,
        FormsModule,
        JobsListModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        KyloFeedModule,
        KyloServicesModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatButtonToggleModule,
        MatCardModule,
        MatCheckboxModule,
        MatDialogModule,
        MatDividerModule,
        MatExpansionModule,
        MatFormFieldModule,
        MatFormFieldModule,
        MatGridListModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatListModule,
        MatMenuModule,
        MatNativeDateModule,
        MatOptionModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatRadioModule,
        MatSelectModule,
        MatSnackBarModule,
        MatStepperModule,
        MatTabsModule,
        MatToolbarModule,
        MatTooltipModule,
        NvD3Module,
        OpsManagerServicesModule,
        PropertyListModule,
        ReactiveFormsModule,
        RemoteFilesModule,
        SharedModule,
        SlaModule,
        TranslateModule,
        UIRouterModule.forChild({states: defineFeedStates}),
        VisualQueryModule,
    ]
})
export class DefineFeedModule {
}

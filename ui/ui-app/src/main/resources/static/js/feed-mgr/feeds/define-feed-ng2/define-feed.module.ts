import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
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
import {RemoteFilesModule} from "../../catalog/datasource/files/remote-files.module";
import {KyloFeedManagerModule} from "../../feed-mgr.module";
import {DynamicFormModule} from "../../shared/dynamic-form/dynamic-form.module";
import {FeedPreconditionModule} from "../../shared/feed-precondition/feed-precondition.module";
import {FieldPoliciesModule} from "../../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../../shared/property-list/property-list.module";
import {VisualQueryModule} from "../../visual-query/visual-query.module";
import {defineFeedStates} from "./define-feed-states";
import {DefineFeedComponent} from "./define-feed.component";
import {FeedScheduleComponent} from "./feed-schedule/feed-schedule.component";
import {NewFeedDialogComponent} from "./new-feed-dialog/new-feed-dialog.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {DefineFeedService} from "./services/define-feed.service";
import {FeedLoadingService} from "./services/feed-loading-service";
import {CategoryAutocompleteComponent} from "./shared/category-autocomplete.component";
import {FeedSideNavComponent} from "./shared/feed-side-nav.component";
import {SystemFeedNameComponent} from "./shared/system-feed-name.component";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedStepCardComponent} from "./steps/define-feed-step-card/define-feed-step-card.component";
import {DefineFeedStepEditContentComponent} from "./steps/define-feed-step-card/define-feed-step-edit-content.component";
import {DefineFeedStepReadonlyContentComponent} from "./steps/define-feed-step-card/define-feed-step-readonly-content.component";
import {DefineFeedTableComponent, FilterPartitionFormulaPipe} from "./steps/define-table/define-feed-table.component";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {FeedDetailsProcessorFieldComponent} from "./steps/feed-details/feed-details-processor-field.component";
import {DefineFeedStepGeneralInfoComponent} from "./steps/general-info/define-feed-step-general-info.component";
import {DefineFeedStepSourceSampleComponent} from "./steps/source-sample/define-feed-step-source-sample.component";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {FeedLineageComponment} from "./summary/feed-lineage/feed-lineage.componment";
import {OverviewComponent} from './summary/overview/overview.component';
import {MatExpansionModule} from "@angular/material/expansion";
import {FeedSideNavService} from "./shared/feed-side-nav.service";
import {ProfileContainerComponent} from './summary/profile/container/profile-container.component';
import {ProfileStatsComponent} from './summary/profile/container/stats/profile-stats.component';
import {ProfileHistoryComponent} from './summary/profile/history/profile-history.component';
import {ProfileComponent} from './summary/profile/profile.component';
import {ProfileInvalidComponent} from './summary/profile/container/invalid/profile-invalid.component';
import {ProfileValidComponent} from './summary/profile/container/valid/profile-valid.component';
import {DefineFeedStepCustomContentComponent} from "./steps/define-feed-step-card/define-feed-step-custom-content.component";
import {DefineFeedPermissionsComponent} from "./steps/permissions/define-feed-permissions.component";
import {DefineFeedPropertiesComponent} from "./steps/properties/define-feed-properties.component";
import {FeedInfoItemComponent} from "./summary/overview/feed-info-item.component";
import {FeedInfoScheduleComponent} from "./summary/overview/feed-info-schedule/feed-info-schedule.component";
import {FeedInfoDescriptionComponent} from "./summary/overview/feed-info-description/feed-info-description.component";
import {FeedInfoCategoryComponent} from "./summary/overview/feed-info-category/feed-info-category.component";
import {FeedInfoNameComponent} from "./summary/overview/feed-info-name/feed-info-name.component";
import {FeedItemInfoService} from "./summary/overview/feed-item-info.service";
import {FeedInfoTagsComponent} from "./summary/overview/feed-info-tags/feed-info-tags.component";
import {SlaComponent} from './summary/sla/sla.componment';
import {SlaDetailsComponent} from './summary/sla/details/sla-details.componment';
import {SlaFormComponent} from './summary/sla/details/sla-form.componment';
import {SlaListComponent} from './summary/sla/list/sla-list.componment';
import {SlaRowComponent} from './summary/sla/list/sla-row.componment';
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {FeedNifiPropertiesService} from "./services/feed-nifi-properties.service";
import {FeedNifiPropertiesComponent} from "./steps/feed-details/feed-nifi-properties.component";
import {DefineFeedSourceSampleService} from "./steps/source-sample/define-feed-source-sample.service";
import {DefineFeedStepSourceComponent} from "./steps/source-sample/define-feed-step-source.component";
import {KyloFeedModule} from "../../../../lib/feed/feed.module";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {CatalogDatasetPreviewModule} from "../../catalog-dataset-preview/catalog-dataset-preview.module";
import {CronExpressionPreviewModule} from '../../shared/cron-expression-preview/cron-expression-preview2.module';
import {SelectNetworkNodeComponent} from "./summary/feed-lineage/select-network-node.component";

import {EntityAccessControlComponent} from "../../shared/entity-access-control/entity-access-control.component";

@NgModule({
    declarations: [
        DefineFeedComponent,
        DefineFeedSelectTemplateComponent,
        DefineFeedContainerComponent,
        DefineFeedStepCardComponent,
        DefineFeedStepGeneralInfoComponent,
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
        FeedSideNavComponent,
        DefineFeedStepCustomContentComponent,
        DefineFeedPermissionsComponent,
        DefineFeedPropertiesComponent,
        FeedInfoItemComponent,
        FeedInfoNameComponent,
        FeedInfoScheduleComponent,
        FeedInfoDescriptionComponent,
        FeedInfoCategoryComponent,
        FeedInfoTagsComponent,
        SlaComponent,
        SlaListComponent,
        SlaRowComponent,
        SlaDetailsComponent,
        SlaFormComponent,
        SelectNetworkNodeComponent,
        FeedInfoTagsComponent,
        FeedNifiPropertiesComponent,
        FeedInfoTagsComponent,
        FeedDetailsProcessorFieldComponent,
        FeedInfoTagsComponent,
        EntityAccessControlComponent
    ],
    entryComponents:[
        NewFeedDialogComponent
    ],
    providers:[
      DefineFeedService,
      FeedLoadingService,
      FilterPartitionFormulaPipe,
      FeedSideNavService,
      FeedItemInfoService,
        FeedNifiPropertiesService,
        DefineFeedSourceSampleService
    ],
    imports: [
        CommonModule,
        CovalentCommonModule,
        CovalentChipsModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentMediaModule,
        CovalentNotificationsModule,
        CovalentVirtualScrollModule,
        CovalentDynamicFormsModule,
        CronExpressionPreviewModule,
        FlexLayoutModule,
        KyloCommonModule,
        KyloFeedManagerModule,
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
        UIRouterModule.forChild({states: defineFeedStates})
    ]
})
export class DefineFeedModule {
}

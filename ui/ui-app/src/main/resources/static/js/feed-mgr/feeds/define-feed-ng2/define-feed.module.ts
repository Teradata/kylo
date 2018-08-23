import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentSearchModule} from "@covalent/core/search";
import {UIRouterModule} from "@uirouter/angular";


import {KyloCommonModule} from "../../../common/common.module";
import {defineFeedStates} from "./define-feed-states";
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {DefineFeedStepGeneralInfoComponent} from "./steps/general-info/define-feed-step-general-info.component";
import {DefineFeedStepSourceSampleComponent} from "./steps/source-sample/define-feed-step-source-sample.component";
import {DefineFeedSummaryComponent} from "./summary/define-feed-summary.component";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedService} from "./services/define-feed.service";
import {FeedLoadingService} from "./services/feed-loading-service";
import {DefineFeedComponent} from "./define-feed.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {CatalogModule} from "../../catalog/catalog.module";
import {MatIconModule} from "@angular/material/icon";
import {DefineFeedStepSourceSampleDatasourceComponent} from "./steps/source-sample/define-feed-step-source-sample-datasource.component";
import {RemoteFilesModule} from "../../catalog/datasource/files/remote-files.module";
import {PreviewSchemaModule} from "../../catalog/datasource/preview-schema/preview-schema.module";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {CovalentMediaModule} from "@covalent/core/media";
import {DefineFeedStepCardComponent} from "./steps/define-feed-step-card/define-feed-step-card.component";
import {KyloFeedManagerModule} from "../../feed-mgr.module";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {DefineFeedStepReadonlyContentComponent} from "./steps/define-feed-step-card/define-feed-step-readonly-content.component";
import {DefineFeedStepEditContentComponent} from "./steps/define-feed-step-card/define-feed-step-edit-content.component";
import {DefineFeedTableComponent, FilterPartitionFormulaPipe} from "./steps/define-table/define-feed-table.component";
import {CovalentVirtualScrollModule} from "@covalent/core/virtual-scroll";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {CovalentDynamicFormsModule} from "@covalent/dynamic-forms";
import {MatRadioModule} from "@angular/material/radio";
import {DynamicFormModule} from "../../shared/dynamic-form/dynamic-form.module";
import {CovalentChipsModule} from "@covalent/core/chips";
import {FieldPoliciesModule} from "../../shared/field-policies-angular2/field-policies.module";
import {PropertyListModule} from "../../shared/property-list/property-list.module";
import {FeedScheduleComponent} from "./feed-schedule/feed-schedule.component";
import {MatDialogModule} from "@angular/material/dialog";
import {TranslateModule} from "@ngx-translate/core";
//import {VisualQueryModule} from "../../visual-query/visual-query.module";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {VisualQueryModule} from "../../visual-query/visual-query.module";
import {FeedPreconditionModule} from "../../shared/feed-precondition/feed-precondition.module";


@NgModule({
    declarations: [
        DefineFeedComponent,
        DefineFeedSelectTemplateComponent,
        DefineFeedSummaryComponent,
        DefineFeedContainerComponent,
        DefineFeedStepGeneralInfoComponent,
        DefineFeedStepSourceSampleComponent,
        DefineFeedStepSourceSampleDatasourceComponent,
        DefineFeedStepFeedDetailsComponent,
        DefineFeedStepCardComponent,
        DefineFeedStepReadonlyContentComponent,
        DefineFeedStepEditContentComponent,
        DefineFeedStepWranglerComponent,
        DefineFeedTableComponent,
        FeedScheduleComponent,
        FilterPartitionFormulaPipe
    ],
    providers:[
      DefineFeedService,
      FeedLoadingService,
      FilterPartitionFormulaPipe
    ],
    imports: [
        CommonModule,
        CovalentChipsModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentMediaModule,
        CovalentVirtualScrollModule,
        CovalentDynamicFormsModule,
        FlexLayoutModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        CatalogModule,
        RemoteFilesModule,
        FieldPoliciesModule,
        FeedPreconditionModule,
        PreviewSchemaModule,
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
        PropertyListModule,
        TranslateModule,
        VisualQueryModule,
        UIRouterModule.forChild({states: defineFeedStates})
    ]
})
export class DefineFeedModule {
    constructor(injector: Injector) {
        console.log("Loading DefineFeedModule")
     //   require("kylo-feedmgr-module");
    }
}

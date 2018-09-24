import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";

import {NvD3Module} from "ng2-nvd3";
import {MatCardModule} from "@angular/material/card";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from  "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {TranslateModule} from "@ngx-translate/core";
import {MatListModule} from "@angular/material/list";
import {KyloCommonModule} from "../common/common.module";
import {MatMenuModule} from "@angular/material/menu";
import {KyloServicesModule} from "../services/services.module";
import {FeedJobActivityComponent} from "./feed-job-activity/feed-job-activity.component";
import {FeedOperationsHealthInfoComponent} from "./feed-operations-health-info/feed-operations-health-info.component";

import {OpsManagerServicesModule} from "../ops-mgr/services/ops-manager-services.module";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {FeedAlertsComponent} from "./feed-alerts/feed-alerts.component";

//import {previewDatasetCollectionServiceProvider} from "./angular2";
@NgModule({
    imports: [
        CommonModule,
        NvD3Module,
        KyloServicesModule,
        MatCardModule,
        FlexLayoutModule,
        MatInputModule,
        MatButtonModule,
        MatButtonToggleModule,
        MatIconModule,
        MatListModule,
        TranslateModule,
        MatMenuModule,
        KyloCommonModule,
        CovalentDialogsModule,
        MatProgressSpinnerModule

    ],
    declarations: [
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent,
        FeedAlertsComponent
    ],
    providers: [

    ],
    exports:[
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent,
        FeedAlertsComponent
    ]
})
export class SharedComponentsModule {

}

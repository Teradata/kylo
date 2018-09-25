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
import {FeedUploadFileDialogComponent} from "./feed-upload-file-dialog/feed-upload-file-dialog.component";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatSnackBarModule} from "@angular/material/snack-bar";

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
        MatSnackBarModule,
        TranslateModule,
        MatMenuModule,
        MatToolbarModule,
        KyloCommonModule,
        CovalentDialogsModule,
        MatProgressSpinnerModule
    ],
    declarations: [
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent,
        FeedAlertsComponent,
        FeedUploadFileDialogComponent
    ],
    entryComponents:[
        FeedUploadFileDialogComponent
    ],
    providers: [

    ],
    exports:[
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent,
        FeedAlertsComponent,
        FeedUploadFileDialogComponent
    ]
})
export class SharedComponentsModule {

}

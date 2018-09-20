import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {Nvd3ChartService} from "../services/nvd3-chart.service";
import {FeedJobActivityComponent} from "./feed-job-activity/feed-job-activity.component";
import {KyloServicesModule} from "../services/services.module";
import {NvD3Module} from "ng2-nvd3";
import {MatCardModule} from "@angular/material/card";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from  "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {TranslateModule} from "@ngx-translate/core";
import {FeedOperationsHealthInfoComponent} from "./feed-operations-health-info/feed-operations-health-info.component";
import {MatListModule} from "@angular/material/list";
import {KyloCommonModule} from "../common/common.module";
import {MatMenuModule} from "@angular/material/menu";

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
        KyloCommonModule

    ],
    declarations: [
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent
    ],
    providers: [

    ],
    exports:[
        FeedJobActivityComponent,
        FeedOperationsHealthInfoComponent
    ]
})
export class SharedComponentsModule {

}

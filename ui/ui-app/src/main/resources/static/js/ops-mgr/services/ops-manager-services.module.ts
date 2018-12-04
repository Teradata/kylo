import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";

import {MatSnackBarModule} from "@angular/material/snack-bar";
import {OpsManagerFeedService} from "./ops-manager-feed.service";
import {OpsManagerJobService} from "./ops-manager-jobs.service";
import {OpsManagerChartJobService} from "./ops-manager-chart-job.service";
import {ChartServicesModule} from "../../services/chart-services/chart-services.module";

@NgModule({
    imports: [
        CommonModule,
        MatSnackBarModule,
        ChartServicesModule
    ],
    providers: [
        OpsManagerFeedService,
        OpsManagerJobService,
        OpsManagerChartJobService
    ]
})
export class OpsManagerServicesModule {

}

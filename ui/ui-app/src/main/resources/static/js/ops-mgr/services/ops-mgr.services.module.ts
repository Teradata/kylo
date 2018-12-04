import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UpgradeModule} from "@angular/upgrade/static";
import {KyloCommonModule} from "../../common/common.module";
import {Nvd3ChartService} from "../../services/chart-services/nvd3-chart.service";
import {KyloServicesModule} from "../../services/services.module";
import {TabService} from "../../services/tab.service";
import {AlertsService} from "./AlertsService";
import {AlertsServiceV2} from "./AlertsServiceV2";
import {IconService} from "./IconStatusService";
import {OpsManagerFeedService} from "./ops-manager-feed.service";
import {OpsManagerJobService} from "./ops-manager-jobs.service";
import {OpsManagerDashboardService} from "./OpsManagerDashboardService";
import {OpsManagerRestUrlService} from "./OpsManagerRestUrlService";
import {ProvenanceEventStatsService} from "./ProvenanceEventStatsService";
import {ServicesStatusData} from "./ServicesStatusService";

@NgModule({
    imports: [
        CommonModule,
        UpgradeModule,
        KyloServicesModule,
        KyloCommonModule
    ],
    providers: [
        AlertsService,
        AlertsServiceV2,
        IconService,
        Nvd3ChartService,
        OpsManagerFeedService,
        OpsManagerJobService,
        OpsManagerRestUrlService,
        ProvenanceEventStatsService,
        ServicesStatusData,
        OpsManagerDashboardService,
        TabService
    ]
})
export class OpsManagerServicesModule {

}

import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UpgradeModule} from "@angular/upgrade/static";
import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";
import AlertsService from "./AlertsService";
import AlertsServiceV2 from "./AlertsServiceV2";
import ChartJobStatusService from "./ChartJobStatusService";
import IconService from "./IconStatusService";
import Nvd3ChartService from "./Nvd3ChartService";
import { OpsManagerFeedService } from "./OpsManagerFeedService";
import OpsManagerJobService from "./OpsManagerJobService";
import OpsManagerRestUrlService from "./OpsManagerRestUrlService";
import ProvenanceEventStatsService from "./ProvenanceEventStatsService";
import ServicesStatusData from "./ServicesStatusService";
import TabService from "./TabService";

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
        ChartJobStatusService,
        IconService,
        Nvd3ChartService,
        OpsManagerFeedService,
        OpsManagerJobService,
        OpsManagerRestUrlService,
        ProvenanceEventStatsService,
        ServicesStatusData,
        TabService
    ]
})
export class OpsManagerServicesModule {

}

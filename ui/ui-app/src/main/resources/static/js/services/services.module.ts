import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
//import {UpgradeModule} from "@angular/upgrade/static";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";

import {
    addButtonServiceProvider,
    broadcastServiceProvider,
    notificationServiceProvider,
    sideNavServiceProvider,
    stateServiceProvider,
    fileUploadServiceProvider,
    accessControlServiceProvider
} from "./angular2";
import {TemplateService} from "../repository/services/template.service";
import {Nvd3ChartService} from "./nvd3-chart.service";
import {ChartJobService} from "./chart-job.service";
import {UpgradeModule} from "@angular/upgrade/static";
import {OpsManagerFeedService} from "./ops-manager-feed.service";
//import {previewDatasetCollectionServiceProvider} from "./angular2";
@NgModule({
    imports: [
        CommonModule
    ],
    providers: [
        notificationServiceProvider,
        PreviewDatasetCollectionService,
        addButtonServiceProvider,
        broadcastServiceProvider,
        sideNavServiceProvider,
        stateServiceProvider,
        fileUploadServiceProvider,
        accessControlServiceProvider,
        OpsManagerFeedService,
        TemplateService,
        Nvd3ChartService,
        ChartJobService
    ]
})
export class KyloServicesModule {

}

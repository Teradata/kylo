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
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import OpsManagerJobService from "./ops-manager-jobs.service";
import {DefaultTableOptionsService} from "./TableOptionsService";
import {DefaultPaginationDataService} from "./PaginationDataService";
import TabService from "./tab.service";
import {MatSnackBarModule} from "@angular/material/snack-bar";

//import {previewDatasetCollectionServiceProvider} from "./angular2";
@NgModule({
    imports: [
        CommonModule,
        CovalentDialogsModule,
        MatSnackBarModule
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
        OpsManagerJobService,
        DefaultTableOptionsService,
        DefaultPaginationDataService,
        TabService,
        TemplateService,
        Nvd3ChartService,
        ChartJobService
    ]
})
export class KyloServicesModule {

}

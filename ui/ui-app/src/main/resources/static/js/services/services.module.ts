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
import {UpgradeModule} from "@angular/upgrade/static";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {DefaultPaginationDataService} from "./PaginationDataService";
import TabService from "./tab.service";
import {OpsManagerServicesModule} from "../ops-mgr/services/ops-manager-services.module";

@NgModule({
    imports: [
        CommonModule,
        MatSnackBarModule,
        OpsManagerServicesModule
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
        DefaultPaginationDataService,
        TabService,
        TemplateService]
})
export class KyloServicesModule {

}

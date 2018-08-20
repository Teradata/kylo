import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UpgradeModule} from "@angular/upgrade/static";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";

import {addButtonServiceProvider, broadcastServiceProvider, notificationServiceProvider,
        paginationServiceProvider, tableOptionsServiceProvider, accessControlServiceProvider,
        stateServiceProvider,
        commonRestURLServiceProvider,
        userGroupServiceProvider,
        angularModuleExtensionServiceProvider,
        fileUploadServiceProvider,
        utilsServiceProvider} from "./angular2";
import {TemplateService} from "../repository/services/template.service";
//import {previewDatasetCollectionServiceProvider} from "./angular2";
@NgModule({
    imports: [
        CommonModule,
        UpgradeModule
    ],
    providers: [
        notificationServiceProvider,
        PreviewDatasetCollectionService,
        addButtonServiceProvider,
        broadcastServiceProvider,
        notificationServiceProvider,
        paginationServiceProvider,
        tableOptionsServiceProvider,
        accessControlServiceProvider,
        stateServiceProvider,
        commonRestURLServiceProvider,
        userGroupServiceProvider,
        fileUploadServiceProvider,
        utilsServiceProvider,
        angularModuleExtensionServiceProvider,
        TemplateService
    ]
})
export class KyloServicesModule {

}

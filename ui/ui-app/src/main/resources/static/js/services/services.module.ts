import {CommonModule} from "@angular/common";
import {ModuleWithProviders, NgModule} from "@angular/core";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";

import {
    addButtonServiceProvider,
    broadcastServiceProvider,
    notificationServiceProvider,
    sideNavServiceProvider,
    stateServiceProvider,
    fileUploadServiceProvider,
    accessControlServiceProvider, utilsProvider
} from "./angular2";
import {TemplateService} from "../repository/services/template.service";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {DefaultPaginationDataService} from "./PaginationDataService";
import {TabService} from "./tab.service";
import {OpsManagerServicesModule} from "../ops-mgr/services/ops-manager-services.module";
import {KyloRouterService} from "./kylo-router.service";
import {FormGroupUtil} from "./form-group-util";
import {HttpBackendClient} from "./http-backend-client";

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
        TemplateService,
        utilsProvider,
        FormGroupUtil,
        HttpBackendClient
    ]
})
export class KyloServicesModule {

    static forRoot() : ModuleWithProviders {
        return {
            ngModule:KyloServicesModule,
            providers:[KyloRouterService]
        }
    }
}

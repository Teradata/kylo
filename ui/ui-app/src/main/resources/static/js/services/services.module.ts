import {CommonModule} from "@angular/common";
import {ModuleWithProviders, NgModule} from "@angular/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import {AccessControlService} from "./AccessControlService";
import {AddButtonService} from "./AddButtonService";
import {AngularModuleExtensionService} from "./AngularModuleExtensionService";
import {BroadcastService} from "./broadcast-service";
import {CommonRestUrlService} from "./CommonRestUrlService";
import {ConfigurationService} from "./ConfigurationService";
import {FileUpload} from "./FileUploadService";
import {FormGroupUtil} from "./form-group-util";
import {HttpBackendClient} from "./http-backend-client";
import {KyloRouterService} from "./kylo-router.service";
import {LoginNotificationService} from "./LoginNotificationService";
import {NotificationService} from "./notification.service";
import {DefaultPaginationDataService} from "./PaginationDataService";
import {SearchService} from "./SearchService";
import {SideNavService} from "./SideNavService";
import {StateService} from "./StateService";
import {TabService} from "./tab.service";
import {DefaultTableOptionsService} from "./TableOptionsService";
import {TemplateService} from "./template.service";
import {UserGroupService} from "./UserGroupService";
import {Utils} from "./Utils";
import {WindowUnloadService} from "./WindowUnloadService";

@NgModule({
    imports: [
        CommonModule,
        MatSnackBarModule,
    ],
    providers: [
        AccessControlService,
        AddButtonService,
        AngularModuleExtensionService,
        BroadcastService,
        CommonRestUrlService,
        ConfigurationService,
        DefaultPaginationDataService,
        DefaultTableOptionsService,
        FileUpload,
        FormGroupUtil,
        HttpBackendClient,
        LoginNotificationService,
        NotificationService,
        PreviewDatasetCollectionService,
        SearchService,
        SideNavService,
        StateService,
        TabService,
        TemplateService,
        UserGroupService,
        Utils,
        WindowUnloadService
    ]
})
export class KyloServicesModule {
    static forRoot() : ModuleWithProviders {
        return {
            ngModule:KyloServicesModule,
            providers:[KyloRouterService,BroadcastService,AddButtonService,AccessControlService]
        }
    }
}

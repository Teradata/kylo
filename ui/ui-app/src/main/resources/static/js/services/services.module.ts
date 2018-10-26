import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import CommonRestUrlService from "./CommonRestUrlService";
import SearchService from "./SearchService";
import AccessControlService from "./AccessControlService";
import AngularModuleExtensionService from "./AngularModuleExtensionService";
import AddButtonService from "./AddButtonService";
import FileUpload from "./FileUploadService";
import { NotificationService } from "./notification.service";
import { DefaultPaginationDataService } from "./PaginationDataService";
import { DefaultTableOptionsService } from "./TableOptionsService";
import StateService from "./StateService";
import UserGroupService from "./UserGroupService";
import Utils from "./Utils";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { TemplateService } from "./template.service";
import ConfigurationService from "./ConfigurationService";
import LoginNotificationService from "./LoginNotificationService";
import SideNavService from "./SideNavService";
import { WindowUnloadService } from "./WindowUnloadService";
import * as angular from "angular";
import BroadcastService from "./broadcast-service";
// import { broadcastServiceProvider } from "./module";
@NgModule({
    imports: [
        CommonModule,
        MatSnackBarModule,
    ],
    providers: [
        NotificationService,
        PreviewDatasetCollectionService,
        // broadcastServiceProvider,
        BroadcastService,
        DefaultPaginationDataService,
        DefaultTableOptionsService,
        StateService,
        UserGroupService,
        Utils,
        TemplateService,
        CommonRestUrlService,
        ConfigurationService,
        SearchService,
        SideNavService,
        AccessControlService,
        AngularModuleExtensionService,
        AddButtonService,
        FileUpload,
        WindowUnloadService,
        LoginNotificationService,
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()},
    ]
})
export class KyloServicesModule {
    constructor(){
        require("./module");
    }
}

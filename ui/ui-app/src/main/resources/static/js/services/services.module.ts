import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UpgradeModule} from "@angular/upgrade/static";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import CommonRestUrlService from "./CommonRestUrlService";
import SearchService from "./SearchService";
import AccessControlService from "./AccessControlService";
import AngularModuleExtensionService from "./AngularModuleExtensionService";
import AddButtonService from "./AddButtonService";
import FileUpload from "./FileUploadService";
import { NotificationService } from "./notification.service";
import BroadcastService from "./broadcast-service";
import { DefaultPaginationDataService } from "./PaginationDataService";
import { DefaultTableOptionsService } from "./TableOptionsService";
import StateService from "./StateService";
import UserGroupService from "./UserGroupService";
import Utils from "./Utils";
import HttpService from "./HttpService";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { TranslateModule } from "@ngx-translate/core";
import { TemplateService } from "./template.service";
import ConfigurationService from "./ConfigurationService";
import LoginNotificationService from "./LoginNotificationService";
import SideNavService from "./SideNavService";
import { WindowUnloadService } from "./WindowUnloadService";

@NgModule({
    imports: [
        CommonModule,
        UpgradeModule,
        MatSnackBarModule,
        TranslateModule,
    ],
    providers: [
        NotificationService,
        PreviewDatasetCollectionService,
        BroadcastService,
        DefaultPaginationDataService,
        DefaultTableOptionsService,
        StateService,
        UserGroupService,
        Utils,
        HttpService,
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
        LoginNotificationService
    ]
})
export class KyloServicesModule {
}

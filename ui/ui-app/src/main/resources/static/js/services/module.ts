import * as angular from 'angular';
import {downgradeInjectable} from "@angular/upgrade/static";
import SearchService from './SearchService';
import AccessControlService from './AccessControlService';
import { PreviewDatasetCollectionService } from '../feed-mgr/catalog/api/services/preview-dataset-collection.service';
import AddButtonService from './AddButtonService';
import AngularModuleExtensionService from './AngularModuleExtensionService';
import BroadcastService from './broadcast-service';
import FileUpload from './FileUploadService';
import LoginNotificationService from './LoginNotificationService';
import { NotificationService } from './notification.service';
import { DefaultPaginationDataService } from './PaginationDataService';
import SideNavService from './SideNavService';
import StateService from './StateService';
import { DefaultTableOptionsService } from './TableOptionsService';
import Utils from './Utils';
import { WindowUnloadService } from './WindowUnloadService';

import {moduleName} from "./module-name"; 

export let module: ng.IModule= angular.module(moduleName, []);

module.service("BroadcastService",BroadcastService);

export const broadcastServiceProvider = {
    provide: BroadcastService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("BroadcastService"),
    deps: ['$injector']
};

module.service("SearchService", downgradeInjectable(SearchService) as any);
module.service("AccessControlService",downgradeInjectable(AccessControlService) as any);
module.service("AddButtonService",downgradeInjectable(AddButtonService) as any);
module.service("AngularModuleExtensionService",downgradeInjectable(AngularModuleExtensionService) as any);
module.service("FileUpload",downgradeInjectable(FileUpload) as any);
module.service("NotificationService",downgradeInjectable(NotificationService) as any);
module.service("DefaultPaginationDataService",downgradeInjectable(DefaultPaginationDataService) as any);
module.service("SideNavService",downgradeInjectable(SideNavService) as any);
module.service("StateService",downgradeInjectable(StateService) as any);
module.service("DefaultTableOptionsService",downgradeInjectable(DefaultTableOptionsService) as any);
module.service("Utils",downgradeInjectable(Utils) as any);
module.service("WindowUnloadService",downgradeInjectable(WindowUnloadService) as any);
module.service("PreviewDatasetCollectionService", downgradeInjectable(PreviewDatasetCollectionService));
module.service("LoginNotificationService",downgradeInjectable(LoginNotificationService) as any);

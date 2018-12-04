import * as angular from 'angular';
import {downgradeInjectable} from "@angular/upgrade/static";
import {SearchService} from './SearchService';
import {AccessControlService} from './AccessControlService';
import { PreviewDatasetCollectionService } from '../feed-mgr/catalog/api/services/preview-dataset-collection.service';
import {AddButtonService} from './AddButtonService';
import {AngularModuleExtensionService} from './AngularModuleExtensionService';
import {BroadcastService} from './broadcast-service';
import {FileUpload} from './FileUploadService';
import {LoginNotificationService} from './LoginNotificationService';
import { NotificationService } from './notification.service';
import {SideNavService} from './SideNavService';
import {StateService} from './StateService';
import { DefaultTableOptionsService } from './TableOptionsService';
import {Utils} from './Utils';
import { WindowUnloadService } from './WindowUnloadService';

let moduleName= "kylo.services";
import {DefaultPaginationDataService} from "./PaginationDataService";
import {TabService} from "./tab.service";
import {Nvd3ChartService} from "./chart-services/nvd3-chart.service";
import {KyloRouterService} from "./kylo-router.service";

export let module= angular.module(moduleName, []);
angular.module(moduleName).service("SearchService", downgradeInjectable(SearchService) as any);
angular.module(moduleName).service("AccessControlService",downgradeInjectable(AccessControlService) as any);
angular.module(moduleName).service("AddButtonService",downgradeInjectable(AddButtonService) as any);
angular.module(moduleName).service("AngularModuleExtensionService",downgradeInjectable(AngularModuleExtensionService) as any);
angular.module(moduleName).service("BroadcastService",downgradeInjectable(BroadcastService) as any);
angular.module(moduleName).service("FileUpload",downgradeInjectable(FileUpload) as any);
angular.module(moduleName).service("NotificationService",downgradeInjectable(NotificationService) as any);
angular.module(moduleName).service("DefaultPaginationDataService",downgradeInjectable(DefaultPaginationDataService) as any);
angular.module(moduleName).service("SideNavService",downgradeInjectable(SideNavService) as any);
angular.module(moduleName).service("StateService",downgradeInjectable(StateService) as any);
angular.module(moduleName).service("DefaultTableOptionsService",downgradeInjectable(DefaultTableOptionsService) as any);
angular.module(moduleName).service("Utils",downgradeInjectable(Utils) as any);
angular.module(moduleName).service("WindowUnloadService",downgradeInjectable(WindowUnloadService) as any);
angular.module(moduleName).service("PreviewDatasetCollectionService", downgradeInjectable(PreviewDatasetCollectionService));
angular.module(moduleName).service("LoginNotificationService",downgradeInjectable(LoginNotificationService) as any);

module.service('PaginationDataService', downgradeInjectable(DefaultPaginationDataService));

module.service('TabService', downgradeInjectable(TabService));

module.service('Nvd3ChartService', downgradeInjectable(Nvd3ChartService));

module.service('KyloRouterService',downgradeInjectable(KyloRouterService))

export default module;

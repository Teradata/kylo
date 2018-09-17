import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import "./module"; // ensure module is loaded
import "./module-require";
import AddButtonService from "./AddButtonService";
import BroadcastService from "./broadcast-service";
import {NotificationService} from "./notification.service";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import {TemplateService} from "../repository/services/template.service";
import {AngularServiceUpgrader} from "../kylo-utils/angular-service-upgrader"
import StateService from "./StateService";
import SideNavService from "./SideNavService";
import FileUpload from "./FileUploadService";
import AccessControlService from "./AccessControlService";

export const addButtonServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(AddButtonService);

export const broadcastServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(BroadcastService);

export const notificationServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(NotificationService);


export const templateServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(TemplateService);

export const stateServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(StateService);

export const sideNavServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(SideNavService);


export const fileUploadServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FileUpload);

export const accessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(AccessControlService);

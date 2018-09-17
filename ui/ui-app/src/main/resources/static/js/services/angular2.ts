import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import "./module"; // ensure module is loaded
import "./module-require";
import AddButtonService from "./AddButtonService";
import BroadcastService from "./broadcast-service";
import {NotificationService} from "./notification.service";
import {DefaultPaginationDataService} from "./PaginationDataService";
import {DefaultTableOptionsService} from "./TableOptionsService";
import AccessControlService from "./AccessControlService";
import StateService from "./StateService";
import CommonRestUrlService from "./CommonRestUrlService";
import UserGroupService from "./UserGroupService";
import FileUpload from "./FileUploadService";
import AngularModuleExtensionService from "./AngularModuleExtensionService";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import {TemplateService} from "../repository/services/template.service";
import Utils from "./Utils";
import HttpService from "./HttpService";

export const addButtonServiceProvider: FactoryProvider = {
    provide: AddButtonService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("AddButtonService"),
    deps: ["$injector"]
};

export const broadcastServiceProvider: FactoryProvider = {
    provide: BroadcastService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("BroadcastService"),
    deps: ["$injector"]
};

export const notificationServiceProvider: FactoryProvider = {
    provide: NotificationService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("NotificationService"),
    deps: ["$injector"]
};


export function previewDatasetCollectionServiceFactory(i: any) {
    return i.get("PreviewDatasetCollectionService");
}

export const previewDatasetCollectionServiceProvider: FactoryProvider = {
    provide: PreviewDatasetCollectionService,
    useFactory: previewDatasetCollectionServiceFactory,
    deps: ["$injector"]
}

export const paginationServiceProvider: FactoryProvider = {
    provide: DefaultPaginationDataService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("DefaultPaginationDataService"),
    deps: ["$injector"]
};

export const tableOptionsServiceProvider: FactoryProvider = {
    provide: DefaultTableOptionsService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("DefaultTableOptionsService"),
    deps: ["$injector"]
};

export const accessControlServiceProvider: FactoryProvider = {
    provide: AccessControlService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("AccessControlService"),
    deps: ["$injector"]
};

export const stateServiceProvider: FactoryProvider = {
    provide: StateService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("StateService"),
    deps: ["$injector"]
};

export const userGroupServiceProvider : FactoryProvider = {
    provide : UserGroupService,
    useFactory : (i: angular.auto.IInjectorService) => i.get("UserGroupService"),
    deps : ["$injector"]
};

export const fileUploadServiceProvider : FactoryProvider = {
    provide : FileUpload,
    useFactory : (i: angular.auto.IInjectorService) => i.get("FileUpload"),
    deps : ["$injector"]
};

export const angularModuleExtensionServiceProvider : FactoryProvider = {
    provide : AngularModuleExtensionService,
    useFactory : (i: angular.auto.IInjectorService) => i.get("AngularModuleExtensionService"),
    deps : ["$injector"]
};

export const utilsServiceProvider : FactoryProvider = {
    provide : Utils,
    useFactory : (i: angular.auto.IInjectorService) => i.get("Utils"),
    deps : ["$injector"]
};

export const httpServiceProvider : FactoryProvider = {
    provide : HttpService,
    useFactory : (i: angular.auto.IInjectorService) => i.get("HttpService"),
    deps : ["$injector"]
};



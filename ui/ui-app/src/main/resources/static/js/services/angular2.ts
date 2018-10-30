import {FactoryProvider} from "@angular/core";

import "./module"; // ensure module is loaded
import {AddButtonService} from "./AddButtonService";
import {BroadcastService} from "./broadcast-service";
import {NotificationService} from "./notification.service";
import {TemplateService} from "../repository/services/template.service";
import {AngularServiceUpgrader} from "../kylo-utils/angular-service-upgrader"
import {StateService} from "./StateService";
import {SideNavService} from "./SideNavService";
import {FileUpload} from "./FileUploadService";
import {AccessControlService} from "./AccessControlService";
import {Utils} from "./Utils";

export const addButtonServiceProvider: FactoryProvider = {
    provide: AddButtonService,
    useFactory: AddButtonServiceFactoryProvider,
    deps: ["$injector"]
};

export const broadcastServiceProvider: FactoryProvider = {
    provide: BroadcastService,
    useFactory: BroadcastServiceFactoryProvider,
    deps: ["$injector"]
};

export const notificationServiceProvider: FactoryProvider = {
    provide: NotificationService,
    useFactory: NotificationServiceFactoryProvider,
    deps: ["$injector"]
};

export const templateServiceProvider: FactoryProvider = {
    provide: TemplateService,
    useFactory: TemplateServiceFactoryProvider,
    deps: ["$injector"]
};

export const stateServiceProvider: FactoryProvider = {
    provide: StateService,
    useFactory: StateServiceFactoryProvider,
    deps: ["$injector"]
};

export const sideNavServiceProvider: FactoryProvider = {
    provide: SideNavService,
    useFactory: SideNavServiceFactoryProvider,
    deps: ["$injector"]
};


export const fileUploadServiceProvider: FactoryProvider = {
    provide: FileUpload,
    useFactory: FileUploadFactoryProvider,
    deps: ["$injector"]
};

export const accessControlServiceProvider: FactoryProvider = {
    provide: AccessControlService,
    useFactory: AccessControlServiceFactoryProvider,
    deps: ["$injector"]
};

export const utilsProvider: FactoryProvider = {
    provide: Utils,
    useFactory: UtilsFactoryProvider,
    deps: ["$injector"]
};

export function AddButtonServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('AddButtonService');
}
export function BroadcastServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('BroadcastService');
}
export function NotificationServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('NotificationService');
}
export function TemplateServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('TemplateService');
}
export function StateServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('StateService');
}
export function SideNavServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('SideNavService');
}
export function FileUploadFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('FileUpload');
}
export function AccessControlServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('AccessControlService');
}
export function UtilsFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('Utils');
}


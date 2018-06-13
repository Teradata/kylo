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


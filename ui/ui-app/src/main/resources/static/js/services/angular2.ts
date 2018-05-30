import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import "./module"; // ensure module is loaded
import "./module-require";
import AddButtonService from "./AddButtonService";
import BroadcastService from "./broadcast-service";
import {NotificationService} from "./notification.service";

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

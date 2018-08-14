import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import "./module"; // ensure module is loaded
import "./module-require";
import AddButtonService from "./AddButtonService";
import BroadcastService from "./broadcast-service";
import {NotificationService} from "./notification.service";
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import {TemplateService} from "../repository/services/template.service";

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

export function templateServiceFactory(i: any) {
    return i.get("templateService");
}

export const templateServiceProvider: FactoryProvider = {
    provide: TemplateService,
    useFactory: templateServiceFactory,
    deps: ["$injector"]
}

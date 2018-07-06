import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import CategoriesService from "./CategoriesService";
import {EntityAccessControlService} from "../shared/entity-access-control/EntityAccessControlService";
import {FeedService} from "./FeedService";

export const entityAccessControlServiceProvider: FactoryProvider = {
    provide: EntityAccessControlService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("EntityAccessControlService"),
    deps: ["$injector"]
};

export const categoriesServiceProvider: FactoryProvider = {
    provide: CategoriesService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("CategoriesService"),
    deps: ["$injector"]
};

export const feedServiceProvider: FactoryProvider = {
    provide: FeedService,
    useFactory: (i: angular.auto.IInjectorService) => i.get("FeedService"),
    deps: ["$injector"]
};

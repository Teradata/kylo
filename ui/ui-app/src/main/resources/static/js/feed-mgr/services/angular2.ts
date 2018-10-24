import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import CategoriesService from "./CategoriesService";
import {EntityAccessControlService} from "../shared/entity-access-control/EntityAccessControlService";
import {FeedService} from "./FeedService";
import {DomainTypesService} from "./DomainTypesService";
import {DefaultFeedPropertyService} from "./DefaultFeedPropertyService";
import {RegisterTemplatePropertyService} from "./RegisterTemplatePropertyService";
import {UiComponentsService} from "./UiComponentsService";
import {FeedInputProcessorPropertiesTemplateService} from "./FeedInputProcessorPropertiesTemplateService";
import {FeedDetailsProcessorRenderingHelper} from "./FeedDetailsProcessorRenderingHelper";

import {AngularServiceUpgrader} from "../../kylo-utils/angular-service-upgrader"
import {HiveService} from "./HiveService";
import {VisualQueryService} from "./VisualQueryService";
import {DatasourcesService} from "./DatasourcesService";
import UserGroupService from "../../services/UserGroupService";
import AccessControlService from "../../services/AccessControlService";
import AddButtonService from '../../services/AddButtonService';
import {AddButtonServiceFactoryProvider} from '../../services/angular2';

// export const entityAccessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(EntityAccessControlService);
export const entityAccessControlServiceProvider: FactoryProvider = {
    provide: EntityAccessControlService,
    useFactory: EntityAccessControlServiceFactoryProvider,
    deps: ["$injector"]
};
export function EntityAccessControlServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('EntityAccessControlService');
}

// export const datasourcesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DatasourcesService);
export const datasourcesServiceProvider: FactoryProvider = {
    provide: DatasourcesService,
    useFactory: DatasourcesServiceFactoryProvider,
    deps: ["$injector"]
};
export function DatasourcesServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('DatasourcesService');
}

// export const categoriesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(CategoriesService);
export const categoriesServiceProvider: FactoryProvider = {
    provide: CategoriesService,
    useFactory: CategoriesServiceFactoryProvider,
    deps: ["$injector"]
};
export function CategoriesServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('CategoriesService');
}

// export const feedServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedService);
export const feedServiceProvider: FactoryProvider = {
    provide: FeedService,
    useFactory: FeedServiceFactoryProvider,
    deps: ["$injector"]
};
export function FeedServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('FeedService');
}

// export const hiveServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(HiveService);
export const hiveServiceProvider: FactoryProvider = {
    provide: HiveService,
    useFactory: HiveServiceFactoryProvider,
    deps: ["$injector"]
};
export function HiveServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('HiveService');
}

// export const visualQueryServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(VisualQueryService);
export const visualQueryServiceProvider: FactoryProvider = {
    provide: VisualQueryService,
    useFactory: VisualQueryServiceFactoryProvider,
    deps: ["$injector"]
};
export function VisualQueryServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('VisualQueryService');
}

// export const domainTypesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DomainTypesService);
export const domainTypesServiceProvider: FactoryProvider = {
    provide: DomainTypesService,
    useFactory: DomainTypesServiceFactoryProvider,
    deps: ["$injector"]
};
export function DomainTypesServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('DomainTypesService');
}

// export const feedPropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DefaultFeedPropertyService,"FeedPropertyService");
export const feedPropertyServiceProvider: FactoryProvider = {
    provide: DefaultFeedPropertyService,
    useFactory: FeedPropertyServiceFactoryProvider,
    deps: ["$injector"]
};
export function FeedPropertyServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('FeedPropertyService');
}

// export const registerTemplatePropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(RegisterTemplatePropertyService);
export const registerTemplatePropertyServiceProvider: FactoryProvider = {
    provide: RegisterTemplatePropertyService,
    useFactory: RegisterTemplatePropertyServiceFactoryProvider,
    deps: ["$injector"]
};
export function RegisterTemplatePropertyServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('RegisterTemplatePropertyService');
}

// export const uiComponentsServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(UiComponentsService);
export const uiComponentsServiceProvider: FactoryProvider = {
    provide: UiComponentsService,
    useFactory: UiComponentsServiceFactoryProvider,
    deps: ["$injector"]
};
export function UiComponentsServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('UiComponentsService');
}

// export const feedInputProcessorPropertiesTemplateServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedInputProcessorPropertiesTemplateService);
export const feedInputProcessorPropertiesTemplateServiceProvider: FactoryProvider = {
    provide: FeedInputProcessorPropertiesTemplateService,
    useFactory: FeedInputProcessorPropertiesTemplateServiceFactoryProvider,
    deps: ["$injector"]
};
export function FeedInputProcessorPropertiesTemplateServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('FeedInputProcessorPropertiesTemplateService');
}

// export const feedDetailsProcessorRenderingHelperProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedDetailsProcessorRenderingHelper);
export const feedDetailsProcessorRenderingHelperProvider: FactoryProvider = {
    provide: FeedDetailsProcessorRenderingHelper,
    useFactory: FeedDetailsProcessorRenderingHelperFactoryProvider,
    deps: ["$injector"]
};
export function FeedDetailsProcessorRenderingHelperFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('FeedDetailsProcessorRenderingHelper');
}

// export const userGroupServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(UserGroupService);
export const userGroupServiceProvider: FactoryProvider = {
    provide: UserGroupService,
    useFactory: UserGroupServiceFactoryProvider,
    deps: ["$injector"]
};
export function UserGroupServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('UserGroupService');
}

// export const accessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(AccessControlService);
export const accessControlServiceProvider: FactoryProvider = {
    provide: AccessControlService,
    useFactory: AccessControlServiceFactoryProvider,
    deps: ["$injector"]
};
export function AccessControlServiceFactoryProvider(i: angular.auto.IInjectorService) {
    return i.get('AccessControlService');
}

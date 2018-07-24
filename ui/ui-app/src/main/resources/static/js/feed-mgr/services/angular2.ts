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

export class AngularServiceUpgrader {
    constructor(){

    }

    static upgrade(service:Function,name:string = service.name) :FactoryProvider{
        return {
            provide: service,
            useFactory: (i: angular.auto.IInjectorService) => i.get(name),
            deps: ["$injector"]
        }
    }
}

export const entityAccessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(EntityAccessControlService);

export const feedServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedService);

export const domainTypesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DomainTypesService);

export const feedPropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DefaultFeedPropertyService,"FeedPropertyService");

export const registerTemplatePropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(RegisterTemplatePropertyService);

export const uiComponentsServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(UiComponentsService);

export const feedInputProcessorPropertiesTemplateServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedInputProcessorPropertiesTemplateService);

export const feedDetailsProcessorRenderingHelperProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedDetailsProcessorRenderingHelper);



import {FactoryProvider} from "@angular/core";
import * as angular from "angular";

import CategoriesService from "./CategoriesService";
import {EntityAccessControlService} from "../shared/entity-access-control/EntityAccessControlService";
import {DomainTypesService} from "./DomainTypesService";
import {DefaultFeedPropertyService} from "./DefaultFeedPropertyService";
import {RegisterTemplatePropertyService} from "./RegisterTemplatePropertyService";
import {FeedInputProcessorPropertiesTemplateService} from "./FeedInputProcessorPropertiesTemplateService";
import {FeedDetailsProcessorRenderingHelper} from "./FeedDetailsProcessorRenderingHelper";
import { EntityAccessControlDialogService } from "../shared/entity-access-control/EntityAccessControlDialogService";

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

export const entityAccessControlDialogServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(EntityAccessControlDialogService);
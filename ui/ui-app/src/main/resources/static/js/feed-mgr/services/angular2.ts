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

export const entityAccessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(EntityAccessControlService);

export const datasourcesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DatasourcesService);

export const categoriesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(CategoriesService);

export const feedServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedService);

export const hiveServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(HiveService);

export const visualQueryServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(VisualQueryService);

export const domainTypesServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DomainTypesService);

export const feedPropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(DefaultFeedPropertyService,"FeedPropertyService");

export const registerTemplatePropertyServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(RegisterTemplatePropertyService);

export const uiComponentsServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(UiComponentsService);

export const feedInputProcessorPropertiesTemplateServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedInputProcessorPropertiesTemplateService);

export const feedDetailsProcessorRenderingHelperProvider: FactoryProvider = AngularServiceUpgrader.upgrade(FeedDetailsProcessorRenderingHelper);

export const userGroupServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(UserGroupService);

export const accessControlServiceProvider: FactoryProvider = AngularServiceUpgrader.upgrade(AccessControlService);

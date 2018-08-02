import * as angular from 'angular';
import {moduleName} from './module-name';

import "./shared/policy-input-form/policy-input-form.component";
import "./shared/hql-editor/hql-editor";
import "./shared/properties-admin/properties-admin";
import "./shared/property-list/property-list";
import "./shared/feed-field-policy-rules/FeedFieldPolicyRuleDialog";
import "./shared/feed-field-policy-rules/inline-field-policy-form";
import "./shared/nifi-property-input/nifi-property-timunit-input";
import "./shared/nifi-property-input/nifi-property-input";
import "./shared/cron-expression-validator/cron-expression-validator";
import "./shared/cron-expression-preview/cron-expression-preview.component";
import "./shared/entity-access-control/entity-access";
import "./shared/profile-stats/ProfileStats";
import "./visual-query/transform-data/profile-stats/column-analysis";
import "./shared/apply-domain-type/ApplyDomainTypeDialog";
import "./shared/apply-domain-type/apply-table-domain-types.component";
import "./shared/apply-domain-type/domain-type-conflict.component";

import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import CategoriesService from "./services/CategoriesService";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";
import {DomainTypesService} from "./services/DomainTypesService";
import {DefaultFeedPropertyService} from "./services/DefaultFeedPropertyService";
import {FeedInputProcessorPropertiesTemplateService} from "./services/FeedInputProcessorPropertiesTemplateService";
import {FeedDetailsProcessorRenderingHelper} from "./services/FeedDetailsProcessorRenderingHelper";
import {RegisterTemplatePropertyService} from "./services/RegisterTemplatePropertyService";
import {UiComponentsService} from "./services/UiComponentsService";
import {RestUrlService} from "./services/RestUrlService";
import {SlaService} from "./services/SlaService";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import {DatasourcesService} from "./services/DatasourcesService";
import {downgradeInjectable} from "@angular/upgrade/static";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";

import { FeedCreationErrorService } from './services/FeedCreationErrorService';
import { DBCPTableSchemaService } from './services/DBCPTableSchemaService';
import { DefaultImportService } from './services/ImportService';
import { VisualQueryService } from './services/VisualQueryService';
import { FeedTagService } from './services/FeedTagService';
import { EditFeedNifiPropertiesService } from './services/EditFeedNifiPropertiesService';
import { RegisterTemplateServiceFactory } from './services/RegisterTemplateServiceFactory';
import {CodeMirrorService} from './services/CodeMirrorService';
import { EntityAccessControlDialogService, EntityAccessControlDialogController } from './shared/entity-access-control/EntityAccessControlDialogService';
import { HiveService } from './services/HiveService';
import { FattableService } from './services/fattable/FattableService';


angular.module(moduleName).service('EntityAccessControlDialogService', EntityAccessControlDialogService)
    .controller('EntityAccessControlDialogController',EntityAccessControlDialogController);

angular.module(moduleName).service('EntityAccessControlService', downgradeInjectable(EntityAccessControlService));

angular.module(moduleName) .service('FeedService',downgradeInjectable(FeedService));

angular.module(moduleName).service('RestUrlService', downgradeInjectable(RestUrlService));

angular.module(moduleName).service('DomainTypesService', downgradeInjectable(DomainTypesService));

angular.module(moduleName).service('CodeMirrorService', downgradeInjectable(CodeMirrorService));

angular.module(moduleName).service('ImportService', downgradeInjectable(DefaultImportService));

angular.module(moduleName).service("VisualQueryService", downgradeInjectable(VisualQueryService));

angular.module(moduleName).service('FeedTagService', downgradeInjectable(FeedTagService));

angular.module(moduleName).service('FeedInputProcessorPropertiesTemplateService',downgradeInjectable(FeedInputProcessorPropertiesTemplateService));

angular.module(moduleName).service('FeedDetailsProcessorRenderingHelper', downgradeInjectable(FeedDetailsProcessorRenderingHelper));

angular.module(moduleName).service('EditFeedNifiPropertiesService', downgradeInjectable(EditFeedNifiPropertiesService)); 

angular.module(moduleName).service('HiveService',downgradeInjectable(HiveService));

angular.module(moduleName).service('RegisterTemplateService', downgradeInjectable(RegisterTemplateServiceFactory));

angular.module(moduleName).service('RegisterTemplatePropertyService', downgradeInjectable(RegisterTemplatePropertyService));

angular.module(moduleName).service('FeedPropertyService', downgradeInjectable(DefaultFeedPropertyService));

angular.module(moduleName).service('UiComponentsService', downgradeInjectable(UiComponentsService));

angular.module(moduleName).service('SlaService', downgradeInjectable(SlaService));

angular.module(moduleName).service('CategoriesService', downgradeInjectable(CategoriesService));

angular.module(moduleName).service('DatasourcesService', downgradeInjectable(DatasourcesService));

angular.module(moduleName).service('PolicyInputFormService', downgradeInjectable(PolicyInputFormService));

angular.module(moduleName).service('FeedSecurityGroups', downgradeInjectable(FeedSecurityGroups));

angular.module(moduleName).service('FeedCreationErrorService', downgradeInjectable(FeedCreationErrorService));

angular.module(moduleName).service('DBCPTableSchemaService', downgradeInjectable(DBCPTableSchemaService));

angular.module(moduleName).service('FattableService', downgradeInjectable(FattableService));
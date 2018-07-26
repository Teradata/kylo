import * as angular from 'angular';
import {moduleName} from './module-name';
import "./services/VisualQueryService";
import "./services/FeedCreationErrorService";
import "./services/RegisterTemplateServiceFactory";

import "./services/FeedInputProcessorPropertiesTemplateService";
import "./services/FeedDetailsProcessorRenderingHelper";
import "./services/ImportService";
import "./shared/policy-input-form/policy-input-form.component";
import "./services/HiveService";
import "./shared/hql-editor/hql-editor";
import "./services/DBCPTableSchemaService";
import "./services/EditFeedNifiPropertiesService";
import "./services/FeedTagService";
import "./services/fattable/FattableService";
import "./services/CodeMirrorService";
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
import CodeMirrorService from './services/CodeMirrorService';
import { EntityAccessControlDialogService, EntityAccessControlDialogController } from './shared/entity-access-control/EntityAccessControlDialogService';


angular.module(moduleName)
    .service('EntityAccessControlDialogService', EntityAccessControlDialogService)
    .controller('EntityAccessControlDialogController',EntityAccessControlDialogController);

angular.module(moduleName).service('EntityAccessControlService', downgradeInjectable(EntityAccessControlService));

angular.module(moduleName)
    .service('FeedService',downgradeInjectable(FeedService));
//     .controller('FeedSavingDialogController', FeedSavingDialogController);

angular.module(moduleName).factory('RestUrlService', downgradeInjectable(RestUrlService));

angular.module(moduleName).factory("DomainTypesService", ["$http", "$q", "RestUrlService",
    function($http: any,$q: any,RestUrlService:any){
        return new DomainTypesService($http , $q, RestUrlService);
    }
]);

angular.module(moduleName).factory('CodeMirrorService', ["$q", 
($q:any) => new CodeMirrorService($q)
]);

angular.module(moduleName).factory('ImportService', downgradeInjectable(DefaultImportService));

angular.module(moduleName).factory("VisualQueryService", downgradeInjectable(VisualQueryService));

angular.module(moduleName).factory('FeedTagService', FeedTagService);

angular.module(moduleName).service('FeedInputProcessorPropertiesTemplateService',FeedInputProcessorPropertiesTemplateService);

angular.module(moduleName).service('FeedDetailsProcessorRenderingHelper', FeedDetailsProcessorRenderingHelper);

angular.module(moduleName).service('EditFeedNifiPropertiesService', EditFeedNifiPropertiesService); 

angular.module(moduleName).service('RegisterTemplateService', RegisterTemplateServiceFactory);

angular.module(moduleName).service('RegisterTemplatePropertyService', RegisterTemplatePropertyService);

angular.module(moduleName).factory('FeedPropertyService', downgradeInjectable(DefaultFeedPropertyService));

angular.module(moduleName).factory('UiComponentsService', downgradeInjectable(UiComponentsService));

angular.module(moduleName).factory('SlaService', downgradeInjectable(SlaService));

angular.module(moduleName).factory('CategoriesService', downgradeInjectable(CategoriesService));

angular.module(moduleName).factory('DatasourcesService', downgradeInjectable(DatasourcesService));

angular.module(moduleName).factory('PolicyInputFormService', downgradeInjectable(PolicyInputFormService));

angular.module(moduleName).factory('FeedSecurityGroups', downgradeInjectable(FeedSecurityGroups));

angular.module(moduleName).service('FeedCreationErrorService', downgradeInjectable(FeedCreationErrorService));

angular.module(moduleName).service('DBCPTableSchemaService', DBCPTableSchemaService);

angular.module(moduleName).service("DatasourcesService",DatasourcesService);


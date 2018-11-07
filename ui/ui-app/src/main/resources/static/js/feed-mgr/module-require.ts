import {downgradeInjectable} from "@angular/upgrade/static";
import * as angular from 'angular';
import {moduleName} from './module-name';

import "./services/CategoriesService";
import CategoriesService from "./services/CategoriesService";
import "./services/CodeMirrorService";
import {CodeMirrorService} from './services/CodeMirrorService';
import "./services/DatasourcesService";
import {DatasourcesService} from "./services/DatasourcesService";
import "./services/DBCPTableSchemaService";
import {DBCPTableSchemaService} from './services/DBCPTableSchemaService';
import "./services/DefaultFeedPropertyService";
import {DefaultFeedPropertyService} from "./services/DefaultFeedPropertyService";
import "./services/DomainTypesService";
import {DomainTypesService} from "./services/DomainTypesService";
import "./services/EditFeedNifiPropertiesService";
import {EditFeedNifiPropertiesService} from './services/EditFeedNifiPropertiesService';
import "./services/fattable/FattableService";
import {FattableService} from './services/fattable/FattableService';
import "./services/FeedCreationErrorService";
import {FeedCreationErrorService} from './services/FeedCreationErrorService';
import "./services/FeedDetailsProcessorRenderingHelper";
import {FeedDetailsProcessorRenderingHelper} from "./services/FeedDetailsProcessorRenderingHelper";
import {FeedInputProcessorPropertiesTemplateService} from "./services/FeedInputProcessorPropertiesTemplateService";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";
import "./services/FeedService";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";
import "./services/FeedTagService";
import {FeedTagService} from './services/FeedTagService';
import "./services/HiveService";
import {HiveService} from './services/HiveService';
import "./services/ImportService";
import {DefaultImportService} from './services/ImportService';
import {RegisterTemplatePropertyService} from "./services/RegisterTemplatePropertyService";
import "./services/RegisterTemplateServiceFactory";
import {RegisterTemplateServiceFactory} from './services/RegisterTemplateServiceFactory';
import "./services/RestUrlService";
import {RestUrlService} from "./services/RestUrlService";
import "./services/SlaService";
import {SlaService} from "./services/SlaService";
import "./services/UiComponentsService";
import {UiComponentsService} from "./services/UiComponentsService";
import "./services/VisualQueryService";
import {VisualQueryService} from './services/VisualQueryService';
import "./shared/apply-domain-type/apply-table-domain-types.component";
import "./shared/apply-domain-type/ApplyDomainTypeDialog";
import "./shared/apply-domain-type/domain-type-conflict.component"
import "./shared/cron-expression-preview/cron-expression-preview";
import "./shared/cron-expression-preview/cron-expression-preview.component";
import "./shared/cron-expression-validator/cron-expression-validator";
import "./shared/entity-access-control/EntityAccessControlDialogService";
import {EntityAccessControlDialogController, EntityAccessControlDialogService} from './shared/entity-access-control/EntityAccessControlDialogService';
import "./shared/entity-access-control/EntityAccessControlService";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import "./shared/feed-field-policy-rules/FeedFieldPolicyRuleDialog";
import "./shared/feed-field-policy-rules/inline-field-policy-form";
import "./shared/hql-editor/hql-editor";
import "./shared/nifi-property-input/nifi-property-input";
import "./shared/nifi-property-input/nifi-property-timunit-input";
import "./shared/policy-input-form/PolicyInputFormService";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import "./shared/profile-stats/ProfileStats";
// import "./shared/properties-admin/properties-admin";
// import "./shared/property-list/property-list";
import "./visual-query/transform-data/profile-stats/column-analysis";

angular.module(moduleName).service('CategoriesService', downgradeInjectable(CategoriesService));

angular.module(moduleName).service('CodeMirrorService', downgradeInjectable(CodeMirrorService));

angular.module(moduleName).service('DatasourcesService', downgradeInjectable(DatasourcesService));

angular.module(moduleName).service('DBCPTableSchemaService', downgradeInjectable(DBCPTableSchemaService));

angular.module(moduleName).service('DomainTypesService', downgradeInjectable(DomainTypesService));

angular.module(moduleName).service('EditFeedNifiPropertiesService', downgradeInjectable(EditFeedNifiPropertiesService));

angular.module(moduleName).service('EntityAccessControlDialogService', EntityAccessControlDialogService)
    .controller('EntityAccessControlDialogController', EntityAccessControlDialogController);

angular.module(moduleName).service('EntityAccessControlService', downgradeInjectable(EntityAccessControlService));

angular.module(moduleName).service('FattableService', downgradeInjectable(FattableService));

angular.module(moduleName).service('FeedCreationErrorService', downgradeInjectable(FeedCreationErrorService));

angular.module(moduleName).service('FeedDetailsProcessorRenderingHelper', downgradeInjectable(FeedDetailsProcessorRenderingHelper));

angular.module(moduleName).service('FeedInputProcessorPropertiesTemplateService', downgradeInjectable(FeedInputProcessorPropertiesTemplateService));

angular.module(moduleName).service('FeedPropertyService', downgradeInjectable(DefaultFeedPropertyService));

angular.module(moduleName)
    .service('FeedService', FeedService)
    .controller('FeedSavingDialogController', FeedSavingDialogController);

angular.module(moduleName).service('FeedSecurityGroups', downgradeInjectable(FeedSecurityGroups));

angular.module(moduleName).service('FeedService', downgradeInjectable(FeedService));

angular.module(moduleName).service('FeedTagService', downgradeInjectable(FeedTagService));

angular.module(moduleName).service('HiveService', downgradeInjectable(HiveService));

angular.module(moduleName).service('ImportService', downgradeInjectable(DefaultImportService));

angular.module(moduleName).service('PolicyInputFormService', downgradeInjectable(PolicyInputFormService));

angular.module(moduleName).service('RegisterTemplatePropertyService', downgradeInjectable(RegisterTemplatePropertyService));

angular.module(moduleName).service('RegisterTemplateService', downgradeInjectable(RegisterTemplateServiceFactory));

angular.module(moduleName).service('RestUrlService', downgradeInjectable(RestUrlService));

angular.module(moduleName).service('SlaService', downgradeInjectable(SlaService));

angular.module(moduleName).service('UiComponentsService', downgradeInjectable(UiComponentsService));

angular.module(moduleName).service("VisualQueryService", downgradeInjectable(VisualQueryService));

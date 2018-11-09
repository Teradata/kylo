import * as angular from 'angular';
import {moduleName} from './module-name';

import "./services/RestUrlService";
import "./services/FeedCreationErrorService";
import "./services/FeedService";
import "./services/RegisterTemplateServiceFactory";

import "./services/FeedSecurityGroupsService";
import "./services/FeedDetailsProcessorRenderingHelper";
import "./services/ImportService";
import "./services/SlaService";
import "./services/DefaultFeedPropertyService";
import "./shared/policy-input-form/policy-input-form";
import "./shared/policy-input-form/PolicyInputFormService";
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
import "../../lib/cron-expression-preview/cron-expression-preview";
import "./services/DatasourcesService";
import "./shared/entity-access-control/entity-access";
import "./shared/entity-access-control/EntityAccessControlDialogService";

import "./services/UiComponentsService";
import "./services/DomainTypesService";

import "./shared/apply-domain-type/ApplyDomainTypeDialog";
import "./shared/apply-domain-type/apply-table-domain-types.component";
import "./shared/apply-domain-type/domain-type-conflict.component"
import "./services/CategoriesService";
import "./shared/entity-access-control/EntityAccessControlService";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import {CategoriesService} from "./services/CategoriesService";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";
import {DomainTypesService} from "./services/DomainTypesService";
import {DefaultFeedPropertyService} from "./services/DefaultFeedPropertyService";
import {FeedInputProcessorPropertiesTemplateService} from "./services/FeedInputProcessorPropertiesTemplateService";
import {FeedDetailsProcessorRenderingHelper} from "./services/FeedDetailsProcessorRenderingHelper";
import {RegisterTemplatePropertyService} from "./services/RegisterTemplatePropertyService";
import {UiComponentsService} from "./services/UiComponentsService";
import {RestUrlService} from "./services/RestUrlService";
import {PreviewDatasetCollectionService} from "./catalog/api/services/preview-dataset-collection.service";
import {HiveService} from "./services/HiveService";
import {downgradeInjectable} from "@angular/upgrade/static";
import {VisualQueryService} from "./services/VisualQueryService";
import {DatasourcesService} from "./services/DatasourcesService";


angular.module(moduleName).service('CategoriesService',CategoriesService);
angular.module(moduleName).service('EntityAccessControlService', EntityAccessControlService);

angular.module(moduleName)
    .service('FeedService',FeedService)
    .controller('FeedSavingDialogController', FeedSavingDialogController);

angular.module(moduleName).factory("DomainTypesService", ["$http", "$q", "RestUrlService",
    function($http: any,$q: any,RestUrlService:any){
        return new DomainTypesService($http , $q, RestUrlService);
    }
]);

angular.module(moduleName).service("UiComponentsService", UiComponentsService);

angular.module(moduleName).service('FeedPropertyService', DefaultFeedPropertyService);

angular.module(moduleName).service('FeedInputProcessorPropertiesTemplateService',FeedInputProcessorPropertiesTemplateService);

angular.module(moduleName).service('FeedDetailsProcessorRenderingHelper', FeedDetailsProcessorRenderingHelper);


angular.module(moduleName).service('RegisterTemplatePropertyService', RegisterTemplatePropertyService);

angular.module(moduleName).service('RestUrlService', RestUrlService);

angular.module(moduleName).service('HiveService', HiveService);

angular.module(moduleName)
    .service("VisualQueryService", VisualQueryService);

const module = angular.module(moduleName).service("DatasourcesService",DatasourcesService);
export default module;

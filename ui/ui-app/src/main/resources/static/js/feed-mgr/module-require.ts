import * as angular from 'angular';
import {moduleName} from './module-name';
import "./services/VisualQueryService";
import "./services/RestUrlService";
import "./services/FeedCreationErrorService";
import "./services/FeedService";
import "./services/RegisterTemplateServiceFactory";

import "./services/FeedSecurityGroupsService";
import "./services/FeedInputProcessorPropertiesTemplateFactory";
import "./services/FeedDetailsProcessorRenderingHelper";
import "./services/ImportService";
import "./services/SlaService";
import "./services/DefaultFeedPropertyService";
import "./shared/policy-input-form/policy-input-form";
import "./shared/policy-input-form/PolicyInputFormService";
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
import "./shared/cron-expression-preview/cron-expression-preview";
import "./services/DatasourcesService";
import "./shared/entity-access-control/entity-access";
import "./shared/entity-access-control/EntityAccessControlDialogService";
import "./shared/profile-stats/ProfileStats";
import "./services/UiComponentsService";
import "./services/DomainTypesService";
import "./visual-query/transform-data/profile-stats/column-analysis";
import "./shared/apply-domain-type/ApplyDomainTypeDialog";
import "./shared/apply-domain-type/apply-table-domain-types.component";
import "./shared/apply-domain-type/domain-type-conflict.component"
import "./services/CategoriesService";
import "./shared/entity-access-control/EntityAccessControlService";
import {EntityAccessControlService} from "./shared/entity-access-control/EntityAccessControlService";
import CategoriesService from "./services/CategoriesService";
import {FeedSavingDialogController, FeedService} from "./services/FeedService";


angular.module(moduleName).service('CategoriesService',CategoriesService);
angular.module(moduleName).service('EntityAccessControlService', EntityAccessControlService);

angular.module(moduleName)
    .service('FeedService',FeedService)
    .controller('FeedSavingDialogController', FeedSavingDialogController);
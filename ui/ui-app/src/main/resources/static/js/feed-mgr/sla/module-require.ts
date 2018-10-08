import 'angular';
import './module-name';
import './service-level-agreement';
import './ServiceLevelAgreementInitController';
import './sla-email-templates/SlaEmailTemplateController';
import './sla-email-templates/SlaEmailTemplatesController';
import SlaEmailTemplateService from './sla-email-templates/SlaEmailTemplateService';
import * as angular from 'angular';
import {moduleName} from './module-name';


angular.module(moduleName).service("SlaEmailTemplateService", SlaEmailTemplateService);

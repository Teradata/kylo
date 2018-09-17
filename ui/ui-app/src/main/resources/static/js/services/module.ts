import * as angular from 'angular';
import {moduleName} from './module-name';
import {TemplateService} from "../repository/services/template.service";
import {downgradeInjectable} from "@angular/upgrade/static";
import SearchService from './SearchService';
import CommonRestUrlService from './CommonRestUrlService';

export let module= angular.module(moduleName, []);
module.factory("templateService", downgradeInjectable(TemplateService) as any);
angular.module(moduleName).service("CommonRestUrlService", downgradeInjectable(CommonRestUrlService) as any);
angular.module(moduleName).service("SearchService", downgradeInjectable(SearchService) as any);


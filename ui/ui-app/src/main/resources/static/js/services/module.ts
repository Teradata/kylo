import * as angular from 'angular';
import {moduleName} from './module-name';
import {TemplateService} from "../repository/services/template.service";
import {downgradeInjectable} from "@angular/upgrade/static";

export let module= angular.module(moduleName, []);
module.factory("templateService", downgradeInjectable(TemplateService) as any);


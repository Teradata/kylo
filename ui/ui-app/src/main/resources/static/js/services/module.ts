import * as angular from 'angular';
import {moduleName} from './module-name';
import {TemplateService} from "../repository/services/template.service";
import {downgradeInjectable} from "@angular/upgrade/static";
import {DefaultPaginationDataService} from "./PaginationDataService";
import {TabService} from "./tab.service";
import {Nvd3ChartService} from "./chart-services/nvd3-chart.service";
import {KyloRouterService} from "./kylo-router.service";

export let module= angular.module(moduleName, []);
module.factory("templateService", downgradeInjectable(TemplateService) as any);


module.service('PaginationDataService', downgradeInjectable(DefaultPaginationDataService));

module.service('TabService', downgradeInjectable(TabService));

module.service('Nvd3ChartService', downgradeInjectable(Nvd3ChartService));

module.service('KyloRouterService',downgradeInjectable(KyloRouterService))

export default module;
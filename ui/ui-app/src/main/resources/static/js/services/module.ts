import * as angular from 'angular';
import {moduleName} from './module-name';
import {TemplateService} from "../repository/services/template.service";
import {downgradeInjectable} from "@angular/upgrade/static";
import OpsManagerJobService from "./ops-manager-jobs.service";
import {DefaultTableOptionsService} from "./TableOptionsService";
import {DefaultPaginationDataService} from "./PaginationDataService";
import TabService from "./tab.service";
import {OpsManagerFeedService} from "./ops-manager-feed.service";
import {Nvd3ChartService} from "./nvd3-chart.service";
import {ChartJobService} from "./chart-job.service";

export let module= angular.module(moduleName, []);
module.factory("templateService", downgradeInjectable(TemplateService) as any);
//downgrade OpsManagerJobService
module.service("OpsManagerJobService", downgradeInjectable(OpsManagerJobService))

module.service("OpsManagerFeedService",downgradeInjectable(OpsManagerFeedService))

angular.module(moduleName).service('Nvd3ChartService',downgradeInjectable(Nvd3ChartService));

angular.module(moduleName)
    .service('ChartJobStatusService', downgradeInjectable(ChartJobService));


angular.module(moduleName).service('PaginationDataService', downgradeInjectable(DefaultPaginationDataService));

angular.module(moduleName).service('TableOptionsService', downgradeInjectable(DefaultTableOptionsService));

angular.module(moduleName).service('TabService', downgradeInjectable(TabService));

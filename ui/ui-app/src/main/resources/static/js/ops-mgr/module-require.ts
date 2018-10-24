import * as angular from 'angular';
import {moduleName} from './module-name';
import "./services/AlertsService";
import "./services/AlertsServiceV2";
import "./services/EventService";
//import "./services/ChartJobStatusService";
import "./services/IconStatusService";
import "./services/OpsManagerRestUrlService";
import "./services/ProvenanceEventStatsService";
import "./services/ServicesStatusService";
//import "./services/TabService";
import "./services/OpsManagerDashboardService";
import {downgradeInjectable} from "@angular/upgrade/static";
import {OpsManagerChartJobService} from "./services/ops-manager-chart-job.service";
import {OpsManagerFeedService} from "./services/ops-manager-feed.service";
import {OpsManagerJobService} from "./services/ops-manager-jobs.service";



angular.module(moduleName).service('OpsManagerChartJobService', downgradeInjectable(OpsManagerChartJobService));

angular.module(moduleName).service('OpsManagerFeedService', downgradeInjectable(OpsManagerFeedService));

const module = angular.module(moduleName).service('OpsManagerJobService', downgradeInjectable(OpsManagerJobService));
export default module;
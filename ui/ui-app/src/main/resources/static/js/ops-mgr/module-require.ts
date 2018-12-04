import {downgradeInjectable} from '@angular/upgrade/static';
import * as angular from 'angular';
import {Nvd3ChartService} from "../services/chart-services/nvd3-chart.service";
import {moduleName} from './module-name';
import "./services/AlertsService";
import {AlertsService} from './services/AlertsService';
import "./services/AlertsServiceV2";
import {AlertsServiceV2} from './services/AlertsServiceV2';
import "./services/EventService";
//import "./services/ChartJobStatusService";
import "./services/IconStatusService";
import {IconService} from './services/IconStatusService';
import {OpsManagerChartJobService} from "./services/ops-manager-chart-job.service";
import {OpsManagerFeedService} from "./services/ops-manager-feed.service";
import {OpsManagerJobService} from "./services/ops-manager-jobs.service";
//import "./services/TabService";
import "./services/OpsManagerDashboardService";
import {OpsManagerDashboardService} from './services/OpsManagerDashboardService';
import "./services/OpsManagerRestUrlService";
import {OpsManagerRestUrlService} from './services/OpsManagerRestUrlService';
import "./services/ProvenanceEventStatsService";
import {ProvenanceEventStatsService} from './services/ProvenanceEventStatsService';
import "./services/ServicesStatusService";
import {ServicesStatusData} from './services/ServicesStatusService';
import {TabService} from '../services/tab.service';


angular.module(moduleName) .service('AlertsService',downgradeInjectable(AlertsService));
angular.module(moduleName) .service('AlertsServiceV2',downgradeInjectable(AlertsServiceV2));
angular.module(moduleName) .service('IconService',downgradeInjectable(IconService));
angular.module(moduleName) .service('Nvd3ChartService',downgradeInjectable(Nvd3ChartService));
angular.module(moduleName) .service('OpsManagerDashboardService',downgradeInjectable(OpsManagerDashboardService));
angular.module(moduleName) .service('OpsManagerFeedService',downgradeInjectable(OpsManagerFeedService));
angular.module(moduleName) .service('OpsManagerRestUrlService',downgradeInjectable(OpsManagerRestUrlService));
angular.module(moduleName) .service('ProvenanceEventStatsService',downgradeInjectable(ProvenanceEventStatsService));
angular.module(moduleName) .service('ServicesStatusData',downgradeInjectable(ServicesStatusData));
angular.module(moduleName) .service('TabService',downgradeInjectable(TabService));


angular.module(moduleName).service('OpsManagerChartJobService', downgradeInjectable(OpsManagerChartJobService));

angular.module(moduleName).service('OpsManagerFeedService', downgradeInjectable(OpsManagerFeedService));

const module = angular.module(moduleName).service('OpsManagerJobService', downgradeInjectable(OpsManagerJobService));
export default module;

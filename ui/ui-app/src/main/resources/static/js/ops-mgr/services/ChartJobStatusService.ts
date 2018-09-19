import * as angular from "angular";
import {moduleName} from "../module-name";
import {ChartJobService} from "../../services/chart-job.service";


  angular.module(moduleName)
  .service('ChartJobStatusService',["Nvd3ChartService", ChartJobService]);

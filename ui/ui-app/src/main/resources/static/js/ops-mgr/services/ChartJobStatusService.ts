import * as angular from "angular";
import {moduleName} from "../module-name";
import IconService from "./IconStatusService";
import Nvd3ChartService from "./Nvd3ChartService";

export default class ChartJobStatusService{;;
    renderEndUpdated: any = {};
    toChartData = function (jobStatusCountResponse: any) {
            return this.Nvd3ChartService.toLineChartData(jobStatusCountResponse, [{label: 'status', value: 'count'}], 'date', this.IconService.colorForJobStatus);
        }

    shouldManualUpdate = function (chart: any) {
            if (this.renderEndUpdated[chart] == undefined) {
                this.renderEndUpdated[chart] = chart;
                return true;
            }
            else {
                return false;
            }
        }
   
    constructor(private IconService: any, private Nvd3ChartService: any){}

}

  angular.module(moduleName)
  .service('IconService',[IconService])
  .service('Nvd3ChartService',["$timeout","$filter", Nvd3ChartService])
  .service('ChartJobStatusService',["IconService", "Nvd3ChartService", ChartJobStatusService]);
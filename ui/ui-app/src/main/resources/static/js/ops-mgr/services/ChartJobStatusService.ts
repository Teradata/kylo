import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ChartJobStatusService{
    renderEndUpdated: any = {};
    toChartData = (jobStatusCountResponse: any)=>{
            return this.Nvd3ChartService.toLineChartData(jobStatusCountResponse,
             [{label: 'status', value: 'count'}], 'date', this.IconService.colorForJobStatus);
        }

    shouldManualUpdate = (chart: any)=> {
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
  .service('ChartJobStatusService',["IconService", "Nvd3ChartService", ChartJobStatusService]);

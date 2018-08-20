import IconService from "./IconStatusService";
import Nvd3ChartService from "./Nvd3ChartService";
import { Injectable } from "@angular/core";

@Injectable()
export default class ChartJobStatusService {
    
    renderEndUpdated: any = {};

    toChartData = (jobStatusCountResponse: any) => {
        return this.Nvd3ChartService.toLineChartData(jobStatusCountResponse,
            [{ label: 'status', value: 'count' }], 'date', this.IconService.colorForJobStatus);
    }

    shouldManualUpdate = (chart: any) => {
        if (this.renderEndUpdated[chart] == undefined) {
            this.renderEndUpdated[chart] = chart;
            return true;
        }
        else {
            return false;
        }
    }

    constructor(private IconService: IconService, private Nvd3ChartService: Nvd3ChartService) { }
}
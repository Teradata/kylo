import {Nvd3ChartService} from "./nvd3-chart.service";
import { Injectable } from "@angular/core";
import IconUtil from "./icon-util";

@Injectable()
export class ChartJobService {

    renderEndUpdated: any = {};

    toChartData(jobStatusCountResponse: any) {
        return this.nvd3ChartService.toLineChartData(jobStatusCountResponse,
            [{ label: 'status', value: 'count' }], 'date',
            (status : any) => {IconUtil.colorForJobStatus(status)});
    }

    shouldManualUpdate(chart: any) {
        if (this.renderEndUpdated[chart] == undefined) {
            this.renderEndUpdated[chart] = chart;
            return true;
        }
        else {
            return false;
        }
    }

    constructor(private nvd3ChartService: Nvd3ChartService) { }
}
import {Component, Input, OnInit} from "@angular/core";
import * as d3 from "d3";
import {ColumnProfile} from "../../wrangler/api/column-profile";

@Component({
    selector: "column-analysis",
    styleUrls: ["./column-analysis.scss"],
    templateUrl: "./column-analysis.html"
})
export class ColumnAnalysisController implements OnInit {

    data: Array<any> = [];
    @Input() profile: ColumnProfile;
    @Input() field: string;
    showValid = false;
    emptyCount = null;
    isInteger = false;

    ngOnInit(): void {
        this.show();
        this.isInteger = (["byte", "integer", "long", "short"].indexOf(this.profile.columnDataTypePlain) > -1);
    }

    show(): void {

        let data = this.data = this.profile.topN;

        // populate metrics
        if (data && data.length > 0) {

            // rescale bar
            let total: number = parseInt(this.profile.totalCount);
            let scaleFactor: number = (1 / (data[0].count / total));
            let cummCount: number = 0;
            data.forEach((item : any) => {
                let frequency = (item.count / total);
                item.frequency = frequency * 100;
                cummCount += item.frequency;
                item.cumm = cummCount;
                item.width = item.frequency * scaleFactor;
            });
        }
    }
}

@Component(({
    selector: "column-histogram",
    template: `
      <nvd3 [options]="chartOptions" [data]="chartData"></nvd3>`
}))
export class HistogramController implements OnInit {

    tooltipData: any;
    @Input("chart-data") chartData: any;
    chartApi: any;
    chartOptions: any = {
        "chart": {
            "type": "multiBarChart",
            "height": 315,
            "width": 700,
            "margin": {
                "top": 20,
                "right": 20,
                "bottom": 45,
                "left": 45
            },
            "clipEdge": true,
            "duration": 250,
            "stacked": false,
            "xAxis": {
                "axisLabel": "Value",
                "showMaxMin": true,
                "tickFormat": function (d: number) {
                    return d3.format('0f')(d)
                }
            },
            "yAxis": {
                "axisLabel": "Count",
                "axisLabelDistance": -20,
                "tickFormat": function (d: number) {
                    return d3.format('0f')(d)
                }
            },
            "showControls": false,
            "showLegend": false,
            tooltip: {
                contentGenerator: function (e: any) {
                    const data = e.data;
                    if (data === null) return;
                    return `<table><tbody>` +
                        `<tr><td class="key">Min</td><td>${data.min}</td></tr>` +
                        `<tr><td class="key">Max</td><td>${data.max}</td></tr>` +
                        `<tr><td class="key">Count</td><td>${data.y}</td></tr>` +
                        `</tbody></table>`;
                }
            }


        }
    };

    formatData() {
        const xAxis = this.chartData._1;
        const yAxis = this.chartData._2;

        const data = [];
        for (let i = 0; i < xAxis.length - 1; i++) {
            data.push({min: xAxis[i], max: xAxis[i + 1], x: (xAxis[i] + xAxis[i + 1]) / 2, y: yAxis[i]});
        }
        this.tooltipData = data;
        return [{
            key: 'Count',
            color: '#bcbd22',
            values: data
        }];
    }

    ngOnInit() {
        this.chartData = this.formatData();
    }
}

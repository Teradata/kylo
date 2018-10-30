import {Component, Input, OnInit} from "@angular/core";
import * as d3 from "d3";
import {ColumnProfile} from "../../wrangler/api/column-profile";

@Component({
    selector: "mini-categorical",
    template: `<nvd3 [options]="chartOptions" [data]="chartData"></nvd3>`
})
export class MiniCategoricalComponent implements OnInit {

    @Input() profile: ColumnProfile;
    chartData : any;
    tooltipData: any;
    chartApi: any;
    chartOptions: any = {
        "chart": {
            "type": "multiBarChart",
            "height": 50,
            "width": 320,
            "margin": {
                "top": 10,
                "right": 5,
                "bottom": 5,
                "left": 5
            },
            "clipEdge": true,
            "duration": 250,
            "stacked": false,
            "xAxis": {
                "axisLabel": null,
                "showMaxMin": false,
                "tickFormat": function (d: string) {
                    return d;
                }
            },
            "yAxis": {
                "axisLabel": null,
                "axisLabelDistance": -20,
                "tickFormat": function (d: number) {
                    return d3.format('0f')(d)
                }
            },
            "showControls": false,
            "showLegend": false,
            "showYAxis": false,
            "showXAxis": false,
            tooltip: {
                contentGenerator: function (e: any) {
                    const data = e.data;
                    if (data === null) return;
                    return `<table><tbody>` +
                        `<tr><td class="key">Value</td><td>${data.x}</td></tr>` +
                        `<tr><td class="key">Count</td><td>${data.y}</td></tr>` +
                        `<tr><td class="key" colspan="2">${data.n} of ${data.total} categories</td></tr>` +
                        `</tbody></table>`;
                }
            }


        }
    };

    formatData() {
        let categories : any = <any>this.profile.topN;

        const data = [];
        let i = 0;
        for (let category of categories) {
            i++;
            data.push({ x:category.domain, y:category.count, n:i, total:this.profile.unique});
        }
        this.tooltipData = data;
        return [{
            key: 'Count',
            color: '#80B7DE', //'#bcbd22',
            values: data
        }];
    }

    ngOnInit() {
        this.chartData = this.formatData();
    }

    show(): void {

        let data = this.profile.topN;

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
    selector: "mini-histogram",
    template: `
      <nvd3 [options]="chartOptions" [data]="chartData"></nvd3>`
}))
export class MiniHistogramComponent implements OnInit {
    @Input() profile: ColumnProfile;
    chartData : any;
    tooltipData: any;
    chartApi: any;
    chartOptions: any = {
        "chart": {
            "type": "multiBarChart",
            "height": 45,
            "width": 320,
            "margin": {
                "top": 10,
                "right": 5,
                "bottom": 5,
                "left": 0
            },
            "clipEdge": true,
            "duration": 250,
            "stacked": false,
            "xAxis": {
                "axisLabel": null,
                "showMaxMin": true,
                "tickFormat": function (d: number) {
                    return d3.format('0f')(d)
                }
            },
            "yAxis": {
                "axisLabel": null,
                "axisLabelDistance": -20,
                "tickFormat": function (d: number) {
                    return d3.format('0f')(d)
                }
            },
            "showControls": false,
            "showLegend": false,
            "showYAxis": false,
            "showXAxis": false,
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
        let histo : any = <any>this.profile.histo;
        const xAxis = histo._1;
        const yAxis = histo._2;

        const data = [];
        for (let i = 0; i < xAxis.length - 1; i++) {
            data.push({min: xAxis[i], max: xAxis[i + 1], x: (xAxis[i] + xAxis[i + 1]) / 2, y: yAxis[i]});
        }
        this.tooltipData = data;
        return [{
            key: 'Count',
            color: '#80B7DE', //color: '#bcbd22',//'''#D07600', //'#80B7DE'
            values: data
        }];
    }

    ngOnInit() {
        this.chartData = this.formatData();
    }
}

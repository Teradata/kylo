import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {NvD3Module} from "ng2-nvd3";
import {Nvd3ChartService} from "./nvd3-chart.service";

@NgModule({
    imports: [
       CommonModule,
       NvD3Module
    ],
    providers: [
        Nvd3ChartService
    ]
})
export class ChartServicesModule {

}

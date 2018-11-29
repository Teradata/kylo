import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {Draggable} from "./draggable.directive";
import {FlowChartComponent} from "./flow-chart.component";

@NgModule({
    declarations: [
        FlowChartComponent,
        Draggable
    ],
    providers:[
    ],
    imports: [
        CommonModule
    ],
    exports:[
        FlowChartComponent,
        Draggable
    ]
})
export class FlowChartModule {
    constructor(injector: Injector) {

    }
}

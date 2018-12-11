import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import { UIRouterModule } from "@uirouter/angular";
import { OpsManagerServicesModule } from "../services/ops-mgr.services.module";
import * as angular from "angular";


import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from '@angular/material/card';


import { CommonModule } from '@angular/common';  
import { MatListModule } from "@angular/material/list";
import {FlexLayoutModule} from "@angular/flex-layout";

import { chartStates } from "./ops-mgr-charts.states";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { ChartsComponent } from "./ChartsComponet";
import {KyloCommonModule} from "../../common/common.module";
import { MatDividerModule } from "@angular/material/divider";
import { MatInputModule } from "@angular/material/input";
import {MatDatepickerModule} from '@angular/material/datepicker';
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { TranslateModule } from "@ngx-translate/core";


@NgModule({
    declarations: [
        ChartsComponent
    ],
    entryComponents: [
        ChartsComponent
    ],
    imports: [
        CommonModule,
        KyloCommonModule,
        OpsManagerServicesModule,
        MatProgressBarModule,
        MatCardModule,
        MatButtonModule,
        MatListModule,
        MatDividerModule,
        MatInputModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        FlexLayoutModule,
        TranslateModule.forChild(),
        UIRouterModule.forChild({states: chartStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers : [{provide: "$injector", useFactory: () => angular.element(document.body).injector()}]
})
export class OpsManagerChartsModule {
    constructor() {
    }
}